package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.server.triggers.StageRegressionData;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class OnlinePermissionGameTests {
    private OnlinePermissionGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void staleOnlineQueriesCannotPartiallyGrantOrWithdrawStages(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The query fixture requires an isolated server.");
        var manager = StageManager.getInstance();
        var order = StageOrder.getInstance();
        var originalDefinitions = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var original = server.overworld().getData(StageAttachments.TEAM_STAGES);
        UUID subject = new UUID(0x5730, 1);
        var owner = new OwnerRef(OwnerKind.PERSONAL, subject);
        var first = StageId.parse("progressivestages:online_input_first");
        var second = StageId.parse("progressivestages:online_input_second");
        var clocks = StageRegressionData.get(server);
        long firstClock = clocks.getGrantTime(owner, first), secondClock = clocks.getGrantTime(owner, second);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(subject, "online-input-test"));
        var adapter = new QueryAdapter();
        int[] publications = {0};
        try (var subscription = manager.subscribeCommittedStageChanges(result -> publications[0]++)) {
            LuckPermsBridge.getInstance().setAdapterForTests(adapter);
            for (var mode : LuckPermsStageOptions.InboundMode.values()) {
                for (String change : List.of("membership", "definitions", "provider", "all_subjects")) {
                    server.overworld().setData(StageAttachments.TEAM_STAGES, original.copy());
                    order.clear();
                    order.registerStage(definition(first, mode));
                    order.registerStage(definition(second, mode));
                    adapter.value = LuckPermsAdapter.PermissionValue.TRUE;
                    adapter.interrupt = () -> {
                        switch (change) {
                            case "membership" -> TeamProvider.getInstance().invalidateMembership();
                            case "definitions" -> {
                                order.clear();
                                order.registerStage(definition(first, mode));
                                order.registerStage(definition(second, mode));
                            }
                            case "provider" -> adapter.subjectChanged.accept(subject);
                            case "all_subjects" -> adapter.allChanged.run();
                        }
                    };
                    long revision = manager.getMutationRevision();
                    int before = publications[0];
                    adapter.calls = 0;
                    LuckPermsBridge.reconcile(player);
                    helper.assertTrue(!manager.hasStage(player, first) && !manager.hasStage(player, second)
                        && server.overworld().getData(StageAttachments.TEAM_STAGES).getPermissionEpisodes(subject).isEmpty()
                        && manager.getMutationRevision() == revision && publications[0] == before,
                        change + " must reject the complete input before any source, history or publication changes.");
                    adapter.calls = 0;
                    LuckPermsBridge.reconcile(player);
                    helper.assertTrue(manager.hasStage(player, first) && manager.hasStage(player, second),
                        "A fresh observation must recover both stages.");
                    adapter.value = LuckPermsAdapter.PermissionValue.FALSE;
                    adapter.interrupt = () -> TeamProvider.getInstance().invalidateMembership();
                    adapter.calls = 0;
                    revision = manager.getMutationRevision();
                    before = publications[0];
                    LuckPermsBridge.reconcile(player);
                    helper.assertTrue(manager.hasStage(player, first) && manager.hasStage(player, second)
                        && manager.getMutationRevision() == revision && publications[0] == before,
                        "A stale negative observation must not partially withdraw sources or alter episode history.");
                    adapter.calls = 0;
                    LuckPermsBridge.reconcile(player);
                    boolean retained = mode == LuckPermsStageOptions.InboundMode.PERMANENT;
                    helper.assertTrue(manager.hasStage(player, first) == retained && manager.hasStage(player, second) == retained,
                        "Fresh negative observations must apply the configured retention to both stages.");
                }
            }
            helper.succeed();
        } finally {
            LuckPermsBridge.disconnect(player);
            LuckPermsBridge.getInstance().setAdapterForTests(null);
            server.overworld().setData(StageAttachments.TEAM_STAGES, original);
            order.clear();
            originalDefinitions.forEach(order::registerStage);
            if (firstClock <= 0) clocks.clear(owner, first); else clocks.markGranted(owner, first, firstClock);
            if (secondClock <= 0) clocks.clear(owner, second); else clocks.markGranted(owner, second, secondClock);
            player.discard();
        }
    }

    private static StageDefinition definition(StageId stage, LuckPermsStageOptions.InboundMode mode) {
        var row = new LuckPermsStageOptions.InboundRule("input", List.of(), List.of(stage.getPath()),
            LuckPermsStageOptions.Match.ALL, Map.of());
        return StageDefinition.builder(stage).teamStage(false).luckPerms(new LuckPermsStageOptions(true, true,
            mode, List.of(row), List.of(), List.of())).build();
    }

    private static final class QueryAdapter implements LuckPermsAdapter {
        private Consumer<UUID> subjectChanged;
        private Runnable allChanged;
        private Runnable interrupt;
        private int calls;
        private PermissionValue value = PermissionValue.TRUE;
        @Override public State state() { return State.READY; }
        @Override public SubjectSnapshot snapshot(UUID player) { return new SubjectSnapshot(true, Set.of(), Map.of(), Map.of()); }
        @Override public PermissionResult permissionResult(UUID player, String permission) {
            if (++calls == 2 && interrupt != null) {
                Runnable action = interrupt;
                interrupt = null;
                action.run();
            }
            return new PermissionResult(true, value);
        }
        @Override public boolean subscribeChanges(Consumer<UUID> subjectChanged, Runnable allChanged) {
            this.subjectChanged = subjectChanged;
            this.allChanged = allChanged;
            return true;
        }
        @Override public boolean groupExists(String group) { return false; }
        @Override public MutationResult addTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts, String owner) { return MutationResult.APPLIED; }
        @Override public MutationResult removeTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts, String owner) { return MutationResult.APPLIED; }
    }
}
