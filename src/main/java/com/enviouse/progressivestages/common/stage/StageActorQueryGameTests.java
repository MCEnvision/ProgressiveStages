package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.ProgressiveStagesAPI;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.data.TeamStageData;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class StageActorQueryGameTests {
    private StageActorQueryGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    @SuppressWarnings("unchecked")
    public static void explicitOfflineQueriesPreserveSourcesAndRejectUnknownOwners(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "Actor queries require an isolated fixture server.");
        var subject = new UUID(0x5734, 1);
        var other = new UUID(0x5734, 2);
        var personal = StageId.parse("progressivestages:query_personal");
        var shared = StageId.parse("progressivestages:query_shared");
        var global = StageId.parse("progressivestages:query_global");
        var waiting = StageId.parse("progressivestages:query_waiting");
        var order = StageOrder.getInstance();
        var definitions = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var original = server.overworld().getData(StageAttachments.TEAM_STAGES);
        var provider = TeamProvider.getInstance();
        var integrationField = TeamProvider.class.getDeclaredField("ftbIntegration");
        integrationField.setAccessible(true);
        var integration = integrationField.get(provider);
        var playerField = PlayerList.class.getDeclaredField("playersByUUID");
        playerField.setAccessible(true);
        var players = (Map<UUID, ServerPlayer>) playerField.get(server.getPlayerList());
        helper.assertTrue(!players.containsKey(subject), "The fixture actor must not replace an existing player.");
        var player = new FakePlayer(helper.getLevel(), new GameProfile(subject, "actor-query"));
        var unavailable = new AtomicBoolean();
        var publications = new java.util.ArrayList<StageMutationResult>();
        var manager = StageManager.getInstance();
        long revision = manager.getMutationRevision();
        try (var subscription = manager.subscribeCommittedStageChanges(publications::add)) {
            integrationField.set(provider, new TeamProvider.ITeamIntegration() {
                @Override public UUID getTeamId(ServerPlayer actor) { return subject; }
                @Override public Set<ServerPlayer> getTeamMembers(UUID team, ServerPlayer requester) { return Set.of(); }
                @Override public Optional<UUID> getOfflineTeamId(UUID actor) {
                    return unavailable.get() ? Optional.empty() : Optional.of(subject);
                }
            });
            order.clear();
            order.registerStage(StageDefinition.builder(personal).teamStage(false).build());
            order.registerStage(StageDefinition.builder(shared).teamStage(true).build());
            order.registerStage(StageDefinition.builder(global).scope("server").build());
            order.registerStage(StageDefinition.builder(waiting).teamStage(false).build());
            var stored = new TeamStageData();
            stored.grantPersonalStage(subject, personal);
            stored.grantStage(subject, personal);
            stored.grantStage(subject, shared);
            stored.grantStage(StageManager.SERVER_TEAM, global);
            String source = new PermissionStageSource(subject, "waiting", false).label();
            stored.grantPersonalStageFromSource(subject, waiting, source);
            var encoded = TeamStageData.CODEC.encodeStart(NbtOps.INSTANCE, stored).getOrThrow();
            var data = TeamStageData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
            server.overworld().setData(StageAttachments.TEAM_STAGES, data);

            var snapshot = ProgressiveStagesAPI.getActorSnapshot(subject);
            helper.assertTrue(snapshot.actorId().equals(subject) && snapshot.revision() == revision
                && snapshot.stages().equals(Set.of(personal, shared, global)),
                "An offline actor view must include eligible personal team and server ownership only.");
            helper.assertTrue(snapshot.sources().values().stream().allMatch(kinds -> kinds.equals(Set.of(StageSourceKind.INDEPENDENT)))
                && data.hasPersonalStage(subject, waiting) && !snapshot.contains(waiting),
                "A stored synchronized source must remain inactive until authoritative revalidation.");
            helper.assertTrue(ProgressiveStagesAPI.getActorSnapshot(other).stages().equals(Set.of(shared, global))
                && manager.getStages(subject).contains(personal),
                "The explicit actor view must not inherit another member's personal or colliding legacy team record.");
            for (StageId stage : snapshot.stages()) {
                var context = ProgressiveStagesAPI.resolveActorOwner(subject, stage);
                helper.assertTrue(context.equals(StageOwnership.context(player, stage)),
                    "Offline and player object contexts must resolve the same current owner and revisions.");
            }
            players.put(subject, player);
            helper.assertTrue(ProgressiveStagesAPI.getActorSnapshot(subject).equals(ProgressiveStagesAPI.getEffectiveSnapshot(player)),
                "An online actor query must use the existing individual snapshot contract.");
            players.remove(subject, player);
            helper.assertTrue(TeamStageData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow().equals(encoded),
                "Successful actor queries must not rewrite stored ownership or source records.");
            data.revokePersonalStage(subject, personal);
            helper.assertTrue(snapshot.contains(personal) && !ProgressiveStagesAPI.getActorSnapshot(subject).contains(personal),
                "A returned snapshot must remain immutable when authoritative ownership changes.");
            data.grantPersonalStage(subject, personal);
            var restored = TeamStageData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
            unavailable.set(true);
            assertUnavailable(helper, () -> ProgressiveStagesAPI.resolveActorOwner(subject, shared));
            assertUnavailable(helper, () -> ProgressiveStagesAPI.getActorSnapshot(subject));
            helper.assertTrue(ProgressiveStagesAPI.resolveActorOwner(subject, personal).owner().kind() == OwnerKind.PERSONAL,
                "Personal owner resolution must not depend on the team provider.");
            try {
                ProgressiveStagesAPI.resolveActorOwner(subject, StageId.parse("progressivestages:query_unknown"));
                helper.fail("An unknown definition must not acquire an inferred owner.");
            } catch (IllegalArgumentException expected) {
                helper.assertTrue(expected.getMessage().startsWith("Unknown stage."), "Unknown stages must identify the query error.");
            }
            helper.assertTrue(TeamStageData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow().equals(restored)
                && manager.getMutationRevision() == revision && publications.isEmpty(),
                "Actor queries and rejected resolution must not rewrite stored data or publish mutations.");
            helper.succeed();
        } finally {
            players.remove(subject, player);
            player.discard();
            integrationField.set(provider, integration);
            server.overworld().setData(StageAttachments.TEAM_STAGES, original);
            order.clear();
            definitions.forEach(order::registerStage);
        }
    }

    private static void assertUnavailable(GameTestHelper helper, Runnable query) {
        try {
            query.run();
            helper.fail("An unresolved offline owner must fail instead of choosing a fallback record.");
        } catch (IllegalStateException expected) {
            helper.assertTrue(expected.getMessage().startsWith("The offline actor owner is unavailable"),
                "The unavailable owner must have an explicit query error.");
        }
    }
}
