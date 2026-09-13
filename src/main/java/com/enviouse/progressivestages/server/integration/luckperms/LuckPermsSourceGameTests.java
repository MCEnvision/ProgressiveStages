package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.stage.StageSourceKind;
import com.enviouse.progressivestages.server.triggers.StageRegressionData;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.PermissionValue.*;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class LuckPermsSourceGameTests {
    private LuckPermsSourceGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void permissionSourcesPreserveOtherSubjectsAndIndependentEarnings(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The source fixture requires an isolated server.");
        var first = player(helper, new UUID(0x5720L, 1));
        var second = player(helper, new UUID(0x5720L, 2));
        var stage = StageId.parse("progressivestages:permission_source_regression");
        var dependency = StageId.parse("progressivestages:permission_source_dependency");
        var order = StageOrder.getInstance();
        var previous = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var data = server.overworld().getData(StageAttachments.TEAM_STAGES);
        var manager = StageManager.getInstance();
        var bridge = LuckPermsBridge.getInstance();
        var adapter = new InMemoryLuckPermsAdapter();
        bridge.setAdapterForTests(adapter);
        order.clear();
        try {
            replaceDefinitions(definition(stage, false, "chef", "backup"));
            adapter.permission(first.getUUID(), "professions.chef", TRUE);
            adapter.permission(second.getUUID(), "professions.chef", TRUE);
            LuckPermsBridge.reconcile(first);
            String firstSource = new PermissionStageSource(first.getUUID(), "chef", false).label();
            String secondSource = new PermissionStageSource(second.getUUID(), "chef", false).label();
            helper.assertTrue(manager.getStageSources(first, stage).equals(Set.of(firstSource)),
                "A first derived grant must not create independent ownership.");
            helper.assertTrue(manager.getEffectiveSnapshot(first).sources().get(stage)
                .equals(Set.of(StageSourceKind.LUCKPERMS_SYNCHRONIZED)), "Source kinds must identify derived access.");
            long revision = manager.getMutationRevision();
            long grantedAt = StageRegressionData.get(server).getGrantTime(StageManager.SERVER_TEAM, stage);
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(manager.getMutationRevision() == revision
                && StageRegressionData.get(server).getGrantTime(StageManager.SERVER_TEAM, stage) == grantedAt,
                "Repeating an unchanged source must not mutate state or refresh its grant time.");
            LuckPermsBridge.reconcile(second);
            helper.assertTrue(manager.getStageSources(first, stage).equals(Set.of(firstSource, secondSource)),
                "Two subjects using one row must remain separate contributors.");
            adapter.permission(first.getUUID(), "professions.chef", FALSE);
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(manager.getStageSources(first, stage).equals(Set.of(secondSource))
                && manager.hasStage(first, stage), "One subject losing eligibility must preserve another subject's source.");

            adapter.permission(second.getUUID(), "professions.backup", TRUE);
            LuckPermsBridge.reconcile(second);
            adapter.permission(second.getUUID(), "professions.chef", FALSE);
            LuckPermsBridge.reconcile(second);
            helper.assertTrue(manager.getStageSources(second, stage).equals(Set.of(
                new PermissionStageSource(second.getUUID(), "backup", false).label())),
                "Losing one row must preserve another eligible row.");
            replaceDefinitions(definition(stage, false, "chef"));
            LuckPermsBridge.reconcile(second);
            helper.assertTrue(!manager.hasStage(first, stage), "Removing the last configured row must withdraw its source.");

            adapter.permission(first.getUUID(), "professions.chef", TRUE);
            LuckPermsBridge.reconcile(first);
            manager.grantStageWithCause(first, stage, StageCause.COMMAND);
            adapter.permission(first.getUUID(), "professions.chef", FALSE);
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(manager.getStageSources(first, stage).equals(Set.of("independent"))
                && manager.hasStage(second, stage), "Independent earnings must survive the last rank contribution.");
            manager.revokeStageFromSource(first, stage, "independent", StageCause.COMMAND);

            replaceDefinitions(definition(stage, true, "chef"));
            adapter.permission(first.getUUID(), "professions.chef", TRUE);
            LuckPermsBridge.reconcile(first);
            String permanent = new PermissionStageSource(first.getUUID(), "chef", true).label();
            adapter.permission(first.getUUID(), "professions.chef", FALSE);
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(manager.getStageSources(first, stage).equals(Set.of(permanent)),
                "Permanent sources must survive authoritative permission loss.");
            replaceDefinitions(definition(stage, false, "chef"));
            adapter.permission(first.getUUID(), "professions.chef", TRUE);
            LuckPermsBridge.reconcile(first);
            replaceDefinitions(StageDefinition.builder(stage).scope("server").build());
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(manager.getStageSources(first, stage).equals(Set.of(permanent)),
                "Removing mappings must withdraw synchronized rows and preserve retained permanent history.");
            manager.revokeStageFromSource(first, stage, permanent, StageCause.COMMAND);

            var options = definition(stage, false, "chef").getLuckPerms();
            replaceDefinitions(StageDefinition.builder(dependency).scope("server").build(),
                StageDefinition.builder(stage).scope("server").luckPerms(options)
                    .dependencies(List.of(dependency)).build());
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(!manager.hasStage(first, stage) && !manager.hasStage(first, dependency),
                "Reconciliation must not manufacture missing prerequisites.");
            data.grantStage(StageManager.SERVER_TEAM, dependency);
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(manager.hasStage(first, stage), "An eligible source must pass current dependency checks.");
            data.revokeStage(StageManager.SERVER_TEAM, dependency);
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(!manager.hasStage(first, stage), "Loss of qualification must withdraw synchronized access.");

            first.experienceLevel = 10;
            replaceDefinitions(StageDefinition.builder(stage).scope("server").luckPerms(options)
                .cost(new StageCost(5, List.of(), true, 0, 100)).build());
            LuckPermsBridge.reconcile(first);
            helper.assertTrue(!manager.hasStage(first, stage) && first.experienceLevel == 10,
                "Reconciliation must neither bypass a purchase nor charge the player.");
            helper.succeed();
        } finally {
            data.revokeStage(StageManager.SERVER_TEAM, stage);
            data.revokeStage(StageManager.SERVER_TEAM, dependency);
            StageRegressionData.get(server).clear(StageManager.SERVER_TEAM, stage);
            StageRegressionData.get(server).clear(StageManager.SERVER_TEAM, dependency);
            LuckPermsBridge.disconnect(first);
            LuckPermsBridge.disconnect(second);
            bridge.setAdapterForTests(null);
            order.clear();
            previous.forEach(order::registerStage);
            first.discard();
            second.discard();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void permissionOwnerChangesWithdrawStaleContributions(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The owner fixture requires an isolated server.");
        var actor = player(helper, new UUID(0x5721L, 1));
        UUID teammate = new UUID(0x5721L, 2);
        UUID oldTeam = new UUID(0x5721L, 3);
        var stage = StageId.parse("progressivestages:permission_owner_regression");
        var order = StageOrder.getInstance();
        var previous = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var data = server.overworld().getData(StageAttachments.TEAM_STAGES);
        var manager = StageManager.getInstance();
        var bridge = LuckPermsBridge.getInstance();
        var adapter = new InMemoryLuckPermsAdapter();
        String ownSource = new PermissionStageSource(actor.getUUID(), "chef", false).label();
        String retained = new PermissionStageSource(actor.getUUID(), "chef", true).label();
        String otherSource = new PermissionStageSource(teammate, "chef", false).label();
        var personal = new com.enviouse.progressivestages.common.stage.OwnerRef(
            com.enviouse.progressivestages.common.stage.OwnerKind.PERSONAL, actor.getUUID());
        var events = new java.util.ArrayList<com.enviouse.progressivestages.common.stage.StageMutationResult>();
        AutoCloseable subscription = manager.subscribeCommittedStageChanges(events::add);
        bridge.setAdapterForTests(adapter);
        try {
            replaceDefinitions(definition(stage, false, "chef"));
            data.grantPersonalStage(actor.getUUID(), stage);
            data.grantPersonalStageFromSource(actor.getUUID(), stage, ownSource);
            data.grantPersonalStageFromSource(actor.getUUID(), stage, retained);
            data.grantPersonalStageFromSource(actor.getUUID(), stage, otherSource);
            data.grantStageFromSource(oldTeam, stage, ownSource);
            adapter.permission(actor.getUUID(), "professions.chef", TRUE);
            LuckPermsBridge.reconcile(actor);
            helper.assertTrue(data.getSources(personal, stage).equals(Set.of("independent", retained, otherSource)),
                "Owner changes must preserve independent, permanent and other subject contributions.");
            helper.assertTrue(!data.hasStage(oldTeam, stage), "The last old team contribution must be withdrawn.");
            helper.assertTrue(data.getSources(StageManager.SERVER_TEAM, stage).equals(Set.of(ownSource)),
                "The qualified source must resolve to the new owner.");
            helper.assertTrue(events.size() == 2 && events.get(0).reason().equals("source_owner_changed")
                && events.get(1).reason().equals("source_added"),
                "Obsolete owner withdrawal must commit before the new owner grant.");
            helper.assertTrue(events.get(0).affectedOwners().contains(personal)
                && events.get(0).affectedOwners().stream().anyMatch(owner -> owner.id().equals(oldTeam)),
                "Committed invalidation must identify the original owners.");
            long revision = manager.getMutationRevision();
            helper.assertTrue(!manager.withdrawObsoletePermissionOwners(actor)
                && manager.getMutationRevision() == revision, "Repeated owner cleanup must be a no op.");

            order.clear();
            LuckPermsBridge.reconcile(actor);
            helper.assertTrue(!data.hasStage(StageManager.SERVER_TEAM, stage),
                "Deleted definitions must withdraw this subject's synchronized contributions.");
            helper.assertTrue(data.getSources(personal, stage).equals(Set.of("independent", retained, otherSource)),
                "Deleting a definition must preserve independent and permanent history and other subjects.");
            adapter.permission(actor.getUUID(), "professions.chef", FALSE);
            replaceDefinitions(definition(stage, false, "chef"));
            LuckPermsBridge.reconcile(actor);
            helper.assertTrue(!manager.hasStage(actor, stage),
                "Restoring a definition must not resurrect a withdrawn ineligible contribution.");
            helper.succeed();
        } finally {
            subscription.close();
            data.revokePersonalStage(actor.getUUID(), stage);
            data.revokeStage(oldTeam, stage);
            data.revokeStage(StageManager.SERVER_TEAM, stage);
            StageRegressionData.get(server).clear(StageManager.SERVER_TEAM, stage);
            LuckPermsBridge.disconnect(actor);
            bridge.setAdapterForTests(null);
            order.clear();
            previous.forEach(order::registerStage);
            actor.discard();
        }
    }

    private static void replaceDefinitions(StageDefinition... definitions) {
        var order = StageOrder.getInstance();
        order.clear();
        for (StageDefinition definition : definitions) order.registerStage(definition);
    }

    private static StageDefinition definition(StageId stage, boolean permanent, String... rows) {
        var inbound = java.util.Arrays.stream(rows).map(row -> new LuckPermsStageOptions.InboundRule(
            row, List.of(), List.of("professions." + row), LuckPermsStageOptions.Match.ALL, Map.of())).toList();
        return StageDefinition.builder(stage).scope("server").luckPerms(new LuckPermsStageOptions(true, true,
            permanent ? LuckPermsStageOptions.InboundMode.PERMANENT : LuckPermsStageOptions.InboundMode.SYNCHRONIZED,
            inbound, List.of(), List.of())).build();
    }

    private static ServerPlayer player(GameTestHelper helper, UUID id) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(id, "source-test"), false);
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
    }
}
