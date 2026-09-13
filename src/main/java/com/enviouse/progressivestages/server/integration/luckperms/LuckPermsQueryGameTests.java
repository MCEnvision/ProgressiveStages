package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class LuckPermsQueryGameTests {
    private LuckPermsQueryGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void permissionQueriesWithdrawDeniedAndUnavailableOutput(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var profile = new GameProfile(UUID.fromString("2ee3a78a-4f09-46a6-b869-5e1b114bba77"), "permission-test");
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(server, helper.getLevel(), profile, cookie.clientInformation());
        var order = StageOrder.getInstance();
        var previous = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var stage = StageId.parse("progressivestages:permission_query_regression");
        var stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        var owner = TeamProvider.getInstance().getTeamId(player);
        var adapter = new CountingAdapter();
        var options = new LuckPermsStageOptions(true, true, LuckPermsStageOptions.InboundMode.SYNCHRONIZED,
            List.of(), List.of(new LuckPermsStageOptions.OutboundRule("home", LuckPermsStageOptions.OutboundKind.PERMISSION,
                "home.set", Map.of())), List.of());
        LuckPermsBridge.getInstance().setAdapterForTests(adapter);
        order.registerStage(StageDefinition.builder(stage).luckPerms(options).build());
        try {
            stages.grantStage(owner, stage);
            adapter.delegate.permission(profile.getId(), "home.set", PermissionValue.FALSE);
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 0, "An independent false result must prevent the provider mutation.");

            adapter.delegate.permission(profile.getId(), "home.set", PermissionValue.UNDEFINED);
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 1, "An authoritative undefined result permits configured positive output.");
            helper.assertTrue(adapter.delegate.effectivePermission(profile.getId(), "home.set") == PermissionValue.TRUE,
                "The effective stage must publish its configured output.");

            adapter.delegate.permission(profile.getId(), "home.set", PermissionValue.FALSE);
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 1 && adapter.removals == 1,
                "A new independent negative must withdraw existing output without another addition.");

            adapter.delegate.permission(profile.getId(), "home.set", PermissionValue.UNDEFINED);
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 2, "A current eligible result must recover output.");
            adapter.queryUnavailable = true;
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 2 && adapter.removals == 2,
                "An unavailable query must withdraw output instead of acting like undefined.");
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 2, "Unavailable data must not publish new output.");
            adapter.queryUnavailable = false;
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.additions == 3, "Query recovery must rebuild from current eligibility.");
            stages.revokeStage(owner, stage);
            LuckPermsBridge.reconcile(player);
            helper.assertTrue(adapter.removals == 3, "Stage loss must withdraw the recovered output.");
            helper.succeed();
        } finally {
            stages.revokeStage(owner, stage);
            LuckPermsBridge.getInstance().setAdapterForTests(null);
            order.clear();
            previous.forEach(order::registerStage);
            player.discard();
        }
    }

    private static final class CountingAdapter implements LuckPermsAdapter {
        private final InMemoryLuckPermsAdapter delegate = new InMemoryLuckPermsAdapter();
        private int additions;
        private int removals;
        private boolean queryUnavailable;
        @Override public State state() { return State.READY; }
        @Override public SubjectSnapshot snapshot(UUID subject) { return delegate.snapshot(subject); }
        @Override public PermissionResult permissionResult(UUID subject, String node) {
            return queryUnavailable ? PermissionResult.unavailable() : delegate.permissionResult(subject, node);
        }
        @Override public boolean groupExists(String group) { return delegate.groupExists(group); }
        @Override public MutationResult addTransient(UUID subject, NodeKind kind, String value,
                                                      Map<String, String> contexts, String owner) {
            additions++;
            return delegate.addTransient(subject, kind, value, contexts, owner);
        }
        @Override public MutationResult removeTransient(UUID subject, NodeKind kind, String value,
                                                         Map<String, String> contexts, String owner) {
            removals++;
            return delegate.removeTransient(subject, kind, value, contexts, owner);
        }
        @Override public boolean cleanupTransientNodes() { return delegate.cleanupTransientNodes(); }
    }
}
