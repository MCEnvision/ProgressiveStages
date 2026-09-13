package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.ProgressiveStagesAPI;
import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.server.ServerEventHandler;
import com.enviouse.progressivestages.server.triggers.StageRegressionData;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class StageActorContextGameTests {
    private StageActorContextGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    @SuppressWarnings("unchecked")
    public static void staleMembershipCannotGrantOrRevokeWithTheSameOwner(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The context fixture requires an isolated server.");
        var field = PlayerList.class.getDeclaredField("playersByUUID");
        field.setAccessible(true);
        var players = (Map<UUID, ServerPlayer>) field.get(server.getPlayerList());
        UUID subject = new UUID(0x5729L, 1);
        helper.assertTrue(!players.containsKey(subject), "The fixture identity must be unused.");
        var player = new FakePlayer(helper.getLevel(), new GameProfile(subject, "context-test"));
        var order = StageOrder.getInstance();
        var previous = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var original = server.overworld().getData(StageAttachments.TEAM_STAGES);
        var manager = StageManager.getInstance();
        var provider = TeamProvider.getInstance();
        var results = new ArrayList<StageMutationResult>();
        var stages = List.of(StageId.parse("progressivestages:context_personal"),
            StageId.parse("progressivestages:context_team"), StageId.parse("progressivestages:context_server"));
        helper.assertTrue(stages.stream().noneMatch(order::stageExists), "Fixture definitions must be unused.");
        try (var subscription = manager.subscribeCommittedStageChanges(results::add)) {
            server.overworld().setData(StageAttachments.TEAM_STAGES, original.copy());
            order.clear();
            order.registerStage(StageDefinition.builder(stages.get(0)).teamStage(false).build());
            order.registerStage(StageDefinition.builder(stages.get(1)).teamStage(true).build());
            order.registerStage(StageDefinition.builder(stages.get(2)).scope("server").build());
            players.put(subject, player);
            for (StageId stage : stages) {
                var stale = ProgressiveStagesAPI.resolveStageOwner(player, stage);
                provider.invalidateMembership();
                provider.invalidateMembership();
                helper.assertTrue(stale.owner().equals(manager.getStageOwner(player, stage)),
                    "Returning to the same owner must not make an old context current.");
                assertRejected(helper, results, stale, stage, StageOperation.GRANT, "stale_membership");
                helper.assertTrue(!manager.hasIndependentStage(player, stage), "A stale grant must not add ownership.");
                var fresh = ProgressiveStagesAPI.resolveStageOwner(player, stage);
                var grant = ProgressiveStagesAPI.mutateStage(fresh, stage, StageOperation.GRANT, StageCause.COMMAND);
                helper.assertTrue(grant.changed() && manager.hasIndependentStage(player, stage),
                    "A freshly resolved context must grant independent ownership.");
                helper.assertTrue(grant.affectedOwners().contains(fresh.owner()), "The grant must report its real owner.");
                ServerEventHandler.onPlayerLogin(new PlayerEvent.PlayerLoggedInEvent(player));
                assertRejected(helper, results, fresh, stage, StageOperation.REVOKE, "stale_membership");
                helper.assertTrue(manager.hasIndependentStage(player, stage), "A stale revoke must preserve ownership.");
                var current = ProgressiveStagesAPI.resolveStageOwner(player, stage);
                var staleDefinition = new StageActorContext(subject, current.owner(), current.definitionRevision() + 1,
                    current.membershipRevision());
                assertRejected(helper, results, staleDefinition, stage, StageOperation.REVOKE, "stale_revision");
                var wrongOwner = new StageActorContext(subject, new OwnerRef(OwnerKind.TEAM, new UUID(0x5729L, 2)),
                    current.definitionRevision(), current.membershipRevision());
                assertRejected(helper, results, wrongOwner, stage, StageOperation.REVOKE, "owner_mismatch");
                players.remove(subject, player);
                assertRejected(helper, results, current, stage, StageOperation.REVOKE, "actor_offline");
                players.put(subject, player);
                provider.initialize();
                assertRejected(helper, results, current, stage, StageOperation.REVOKE, "stale_membership");
                var revoke = ProgressiveStagesAPI.mutateStage(ProgressiveStagesAPI.resolveStageOwner(player, stage),
                    stage, StageOperation.REVOKE, StageCause.COMMAND);
                helper.assertTrue(revoke.changed() && !manager.hasIndependentStage(player, stage),
                    "A current context must revoke the addressed ownership.");
            }
            helper.succeed();
        } finally {
            players.remove(subject, player);
            var clocks = StageRegressionData.get(server);
            for (StageId stage : stages) {
                for (OwnerRef owner : Set.of(new OwnerRef(OwnerKind.PERSONAL, subject),
                    new OwnerRef(OwnerKind.TEAM, subject), new OwnerRef(OwnerKind.SERVER, StageManager.SERVER_TEAM))) {
                    clocks.clear(owner, stage);
                }
            }
            server.overworld().setData(StageAttachments.TEAM_STAGES, original);
            order.clear();
            previous.forEach(order::registerStage);
            player.discard();
        }
    }

    private static void assertRejected(GameTestHelper helper, List<StageMutationResult> publications,
                                       StageActorContext context, StageId stage, StageOperation operation, String reason) {
        long revision = StageManager.getInstance().getMutationRevision();
        int published = publications.size();
        var result = ProgressiveStagesAPI.mutateStage(context, stage, operation, StageCause.COMMAND);
        helper.assertTrue(!result.changed() && reason.equals(result.reason()), "The mutation must reject " + reason + ".");
        helper.assertTrue(result.revision() == revision && publications.size() == published,
            "Rejected context must not change the mutation revision or publish a committed change.");
    }
}
