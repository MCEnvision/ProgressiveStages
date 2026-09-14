package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.ProgressiveStagesAPI;
import com.enviouse.progressivestages.common.api.StageActorChangeEvent;
import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.RevokeRule;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.data.TeamStageData;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.server.triggers.StagePurchaseData;
import com.enviouse.progressivestages.server.triggers.StageRegressionData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class StageOfflineMutationGameTests {
    private StageOfflineMutationGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlinePersonalRevokePreservesSharedMilestones(GameTestHelper helper) throws Exception {
        verifyRevoke(helper, OwnerKind.PERSONAL);
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlineTeamRevokePreservesPersonalRecords(GameTestHelper helper) throws Exception {
        verifyRevoke(helper, OwnerKind.TEAM);
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void offlineServerRevokeCascadesAcrossTeams(GameTestHelper helper) throws Exception {
        verifyRevoke(helper, OwnerKind.SERVER);
    }

    private static void verifyRevoke(GameTestHelper helper, OwnerKind kind) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "Offline mutations require an isolated server.");
        UUID actor = new UUID(0x5736, 1), other = new UUID(0x5736, 2);
        helper.assertTrue(server.getPlayerList().getPlayer(actor) == null, "The actor must have no connected player entity.");
        var order = StageOrder.getInstance();
        var definitions = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var original = server.overworld().getData(StageAttachments.TEAM_STAGES);
        var originalPurchases = StagePurchaseData.get(server);
        var provider = TeamProvider.getInstance();
        var field = TeamProvider.class.getDeclaredField("ftbIntegration");
        field.setAccessible(true);
        var integration = field.get(provider);
        var unavailable = new AtomicBoolean();
        var manager = StageManager.getInstance();
        var root = StageId.parse("progressivestages:offline_root");
        var dependent = StageId.parse("progressivestages:offline_dependent");
        var shared = StageId.parse("progressivestages:offline_shared");
        var rootOwner = new OwnerRef(kind, kind == OwnerKind.SERVER ? StageManager.SERVER_TEAM : actor);
        var childOwner = new OwnerRef(kind == OwnerKind.PERSONAL ? OwnerKind.PERSONAL : OwnerKind.TEAM, actor);
        var otherTeam = new OwnerRef(OwnerKind.TEAM, other);
        var clock = StageRegressionData.get(server);
        var committed = new ArrayList<StageMutationResult>();
        var events = new ArrayList<StageActorChangeEvent>();
        Consumer<StageActorChangeEvent> listener = (StageActorChangeEvent event) -> events.add(event);
        NeoForge.EVENT_BUS.addListener(listener);
        try (var subscription = manager.subscribeCommittedStageChanges(committed::add)) {
            field.set(provider, new TeamProvider.ITeamIntegration() {
                @Override public UUID getTeamId(ServerPlayer player) { return actor; }
                @Override public Set<ServerPlayer> getTeamMembers(UUID owner, ServerPlayer player) { return Set.of(); }
                @Override public Optional<UUID> getOfflineTeamId(UUID subject) {
                    return unavailable.get() ? Optional.empty() : Optional.of(actor);
                }
            });
            order.clear();
            var rootDefinition = StageDefinition.builder(root).revoke(new RevokeRule(false, -1, true));
            if (kind == OwnerKind.SERVER) rootDefinition.scope("server");
            else rootDefinition.teamStage(kind == OwnerKind.TEAM);
            order.registerStage(rootDefinition.build());
            order.registerStage(StageDefinition.builder(dependent).teamStage(kind != OwnerKind.PERSONAL)
                .dependencies(List.of(root)).build());
            order.registerStage(StageDefinition.builder(shared).teamStage(true).dependencies(List.of(root)).build());
            var data = new TeamStageData();
            server.overworld().setData(StageAttachments.TEAM_STAGES, data);
            if (kind == OwnerKind.PERSONAL) {
                data.grantPersonalStage(actor, root);
                data.grantPersonalStage(actor, dependent);
                data.grantStage(actor, root);
            } else {
                data.grantStage(rootOwner.id(), root);
                data.grantStage(actor, dependent);
                data.grantPersonalStage(actor, root);
                data.grantPersonalStage(actor, dependent);
            }
            data.grantPersonalStage(other, root);
            data.grantStage(actor, shared);
            data.grantStage(other, dependent);
            clock.markGranted(rootOwner, root, 1000);
            clock.markGranted(childOwner, dependent, 1000);
            clock.markGranted(otherTeam, dependent, 1000);
            var purchases = new StagePurchaseData();
            server.overworld().getDataStorage().set("progressivestages_purchases", purchases);
            var cost = new StageCost(10, List.of(), false, 0, 50);
            purchases.markActorPurchase(rootOwner, root, actor, cost);
            var context = ProgressiveStagesAPI.resolveActorOwner(actor, root);
            var encoded = TeamStageData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
            unavailable.set(true);
            long before = manager.getMutationRevision();
            var denied = ProgressiveStagesAPI.mutateStage(context, root, StageOperation.REVOKE, StageCause.COMMAND);
            helper.assertTrue(!denied.changed() && denied.reason().equals("owner_unavailable")
                && manager.getMutationRevision() == before && committed.isEmpty() && events.isEmpty()
                && TeamStageData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow().equals(encoded)
                && purchases.getActorPurchase(rootOwner, root).isPresent(),
                "Unresolved ownership must reject before changing stages receipts clocks or notifications.");
            unavailable.set(false);
            var result = ProgressiveStagesAPI.mutateStage(context, root, StageOperation.REVOKE, StageCause.COMMAND);
            helper.assertTrue(result.changed() && result.reason().equals("committed")
                && result.affectedOwners().contains(rootOwner) && result.affectedPlayers().contains(actor),
                "An explicit offline actor must revoke and publish its concrete owner and actor identity.");
            helper.assertTrue(!data.hasEffectiveStage(rootOwner, root) && !data.hasEffectiveStage(childOwner, dependent)
                && data.hasPersonalStage(other, root), "Only the root and qualifying cascade owners may be revoked.");
            helper.assertTrue(clock.getGrantTime(rootOwner, root) < 0 && clock.getGrantTime(childOwner, dependent) < 0,
                "Every removed concrete owner must lose its grant clock immediately.");
            if (kind == OwnerKind.PERSONAL) {
                helper.assertTrue(data.hasStage(actor, root) && data.hasStage(actor, shared),
                    "A personal revoke must preserve colliding team history and independently earned shared milestones.");
            } else {
                helper.assertTrue(data.hasPersonalStage(actor, root) && data.hasPersonalStage(actor, dependent)
                    && !data.hasStage(actor, shared), "A shared cascade must preserve personal history.");
            }
            helper.assertTrue(data.hasStage(other, dependent) == (kind != OwnerKind.SERVER)
                && (clock.getGrantTime(otherTeam, dependent) < 0) == (kind == OwnerKind.SERVER),
                "A server prerequisite must cascade across teams while a local prerequisite preserves other teams.");
            helper.assertTrue(committed.size() == 1 && committed.getFirst().equals(result)
                && events.stream().allMatch(event -> event.getContext().actorId().equals(actor)
                    && event.getCause() == StageCause.COMMAND && event.getChangeType().name().equals("REVOKED"))
                && events.stream().anyMatch(event -> event.getContext().owner().equals(rootOwner)
                    && event.getStageId().equals(root)), "Committed events must retain the explicit actor owner stage and cause.");
            helper.assertTrue(purchases.getActorPurchase(rootOwner, root).isEmpty()
                && purchases.getPendingActorRefunds(actor).size() == 1
                && purchases.getPendingActorRefunds(other).isEmpty(), "The offline payer retains exactly one attributable refund.");
            int eventCount = events.size();
            long revision = manager.getMutationRevision();
            var repeated = ProgressiveStagesAPI.mutateStage(context, root, StageOperation.REVOKE, StageCause.COMMAND);
            helper.assertTrue(!repeated.changed() && manager.getMutationRevision() == revision
                && committed.size() == 1 && events.size() == eventCount
                && purchases.getPendingActorRefunds(actor).size() == 1, "A repeated offline revoke must have no effects.");
            var restored = StagePurchaseData.load(purchases.save(new net.minecraft.nbt.CompoundTag(), server.registryAccess()), server.registryAccess());
            var receipt = restored.getPendingActorRefunds(actor).getFirst();
            helper.assertTrue(receipt.owner().equals(rootOwner) && receipt.cost().xpLevels() == 10
                && receipt.cost().refundPercent() == 50, "An offline refund must preserve its owner and original terms after persistence.");
            provider.invalidateMembership();
            var stale = ProgressiveStagesAPI.mutateStage(context, root, StageOperation.REVOKE, StageCause.COMMAND);
            helper.assertTrue(!stale.changed() && stale.reason().equals("stale_membership") && events.size() == eventCount,
                "Offline requests must retain the same membership revision guard as connected requests.");
            helper.succeed();
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            for (StageId stage : List.of(root, dependent, shared)) {
                for (OwnerRef owner : new java.util.LinkedHashSet<>(List.of(rootOwner, childOwner, otherTeam, new OwnerRef(OwnerKind.TEAM, actor)))) {
                    clock.clear(owner, stage);
                }
            }
            field.set(provider, integration);
            server.overworld().getDataStorage().set("progressivestages_purchases", originalPurchases);
            server.overworld().setData(StageAttachments.TEAM_STAGES, original);
            order.clear();
            definitions.forEach(order::registerStage);
        }
    }
}
