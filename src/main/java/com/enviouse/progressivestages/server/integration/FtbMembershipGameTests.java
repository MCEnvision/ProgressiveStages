package com.enviouse.progressivestages.server.integration;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class FtbMembershipGameTests {
    private FtbMembershipGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeFtbMembershipPreservesPersonalAndSharedOwnership(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbteams")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, FixtureKind.MEMBERSHIP);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeQuestRewardsAndChecksRespectPersonalOwnership(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbquests")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, FixtureKind.PLAYER_QUEST);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeTeamQuestSettingsRespectStageOwnership(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbquests")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, FixtureKind.TEAM_QUEST);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void legacyFtbImportPreservesOwnersWithoutReplayingRewards(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbquests")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, FixtureKind.LEGACY_IMPORT);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeQuestClaimTrackingPreservesRewardDistribution(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbquests")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, FixtureKind.CLAIM_TRACKING);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeTeamLeaveWithdrawsOnlyMovedSynchronizedContributions(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbteams")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, FixtureKind.SOURCE_MOVE);
        helper.succeed();
    }

    private enum FixtureKind { MEMBERSHIP, PLAYER_QUEST, TEAM_QUEST, LEGACY_IMPORT, CLAIM_TRACKING, SOURCE_MOVE }

    private static final class Fixture {
        static void run(GameTestHelper helper, FixtureKind kind) throws Exception {
            var server = helper.getLevel().getServer();
            helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The membership fixture requires an isolated server.");
            var teams = (dev.ftb.mods.ftbteams.data.TeamManagerImpl) dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
            var provider = com.enviouse.progressivestages.common.team.TeamProvider.getInstance();
            var manager = com.enviouse.progressivestages.common.stage.StageManager.getInstance();
            var order = com.enviouse.progressivestages.common.stage.StageOrder.getInstance();
            var firstId = new java.util.UUID(0x5731, 1);
            var secondId = new java.util.UUID(0x5731, 2);
            var firstProfile = new com.mojang.authlib.GameProfile(firstId, "membership-first");
            var secondProfile = new com.mojang.authlib.GameProfile(secondId, "membership-second");
            helper.assertTrue(teams.getTeamForPlayerID(firstId).isEmpty() && teams.getTeamForPlayerID(secondId).isEmpty(),
                "Fixture player identities must not already exist.");
            var first = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(), firstProfile);
            var second = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(), secondProfile);
            var personal = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:native_personal");
            var shared = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:native_shared");
            var definitions = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
            var original = server.overworld().getData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES);
            var originalTeams = java.util.Set.copyOf(teams.getTeamMap().keySet());
            var clocks = com.enviouse.progressivestages.server.triggers.StageRegressionData.get(server);
            var personalOwner = new com.enviouse.progressivestages.common.stage.OwnerRef(
                com.enviouse.progressivestages.common.stage.OwnerKind.PERSONAL, firstId);
            long personalClock = clocks.getGrantTime(personalOwner, personal);
            var questTeams = ModList.get().isLoaded("ftbquests") ? QuestFixture.captureTeamData() : null;
            dev.ftb.mods.ftbteams.data.PartyTeam party = null;
            try {
                FTBTeamsIntegration.registerIfAvailable();
                FTBTeamsIntegration.registerIfAvailable();
                long revision = provider.membershipRevision();
                teams.playerLoggedIn(null, firstId, firstProfile.getName());
                helper.assertTrue(provider.membershipRevision() == revision + 1,
                    "The actual provider event must invalidate membership exactly once after repeated registration.");
                teams.playerLoggedIn(null, secondId, secondProfile.getName());
                revision = provider.membershipRevision();
                party = teams.createParty(firstId, null, "membership fixture", "", dev.ftb.mods.ftblibrary.icon.Color4I.WHITE);
                helper.assertTrue(provider.membershipRevision() == revision + 1,
                    "Actual party creation must invalidate captured membership before returning.");
                revision = provider.membershipRevision();
                party.join(null, secondProfile);
                helper.assertTrue(provider.membershipRevision() == revision + 1
                    && provider.getFtbTeamId(first).equals(party.getId()) && provider.getFtbTeamId(second).equals(party.getId())
                    && provider.getOfflineTeamId(secondId, true).orElseThrow().equals(party.getId()),
                    "The provider must resolve the real joined party for online and offline owner queries.");
                server.overworld().setData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES,
                    new com.enviouse.progressivestages.common.data.TeamStageData());
                order.clear();
                order.registerStage(com.enviouse.progressivestages.common.config.StageDefinition.builder(personal).teamStage(false).build());
                order.registerStage(com.enviouse.progressivestages.common.config.StageDefinition.builder(shared).teamStage(true).build());
                manager.grantStage(first, personal);
                manager.grantStage(first, shared);
                helper.assertTrue(manager.hasStage(first, personal) && !manager.hasStage(second, personal)
                    && manager.hasStage(first, shared) && manager.hasStage(second, shared),
                    "Real teammates must share the team stage while keeping a profession personal.");
                var captured = manager.captureOfflinePermissionContext(secondId);
                revision = provider.membershipRevision();
                party.leave(secondId);
                helper.assertTrue(provider.membershipRevision() == revision + 1
                    && provider.getFtbTeamId(second).equals(secondId)
                    && provider.getOfflineTeamId(secondId, true).orElseThrow().equals(secondId)
                    && !captured.equals(manager.captureOfflinePermissionContext(secondId))
                    && !manager.hasStage(second, shared) && manager.hasStage(first, shared)
                    && manager.hasStage(first, personal) && !manager.hasStage(second, personal),
                    "Leaving the real party must invalidate the captured owner and preserve the remaining member's stages.");
                revision = provider.membershipRevision();
                party.join(null, secondProfile);
                helper.assertTrue(provider.membershipRevision() == revision + 1 && manager.hasStage(second, shared)
                    && !manager.hasStage(second, personal), "Rejoining must recover shared access without copying a personal stage.");
                if (kind == FixtureKind.MEMBERSHIP) {
                    var formerTeam = party.getId();
                    captured = manager.captureOfflinePermissionContext(firstId);
                    revision = provider.membershipRevision();
                    party.forceDisband(server.createCommandSourceStack());
                    helper.assertTrue(provider.membershipRevision() > revision && teams.getTeamByID(formerTeam).isEmpty()
                        && provider.getFtbTeamId(first).equals(firstId) && provider.getFtbTeamId(second).equals(secondId)
                        && !captured.equals(manager.captureOfflinePermissionContext(firstId))
                        && !manager.hasStage(first, shared) && !manager.hasStage(second, shared)
                        && manager.hasStage(first, personal) && !manager.hasStage(second, personal)
                        && manager.hasStage(formerTeam, shared),
                        "Disband must invalidate membership and mask former team grants while preserving personal ownership.");
                    var replacement = teams.createParty(secondId, null, "replacement fixture", "",
                        dev.ftb.mods.ftblibrary.icon.Color4I.WHITE);
                    try {
                        replacement.join(null, firstProfile);
                        helper.assertTrue(!replacement.getId().equals(formerTeam)
                            && !manager.hasStage(first, shared) && !manager.hasStage(second, shared)
                            && manager.hasStage(first, personal) && !manager.hasStage(second, personal),
                            "A replacement party must not inherit the former party grants or another player's profession.");
                    } finally {
                        replacement.forceDisband(server.createCommandSourceStack());
                    }
                } else if (kind == FixtureKind.LEGACY_IMPORT) LegacyFixture.run(helper, first, second, personal, shared);
                else if (kind == FixtureKind.CLAIM_TRACKING) ClaimFixture.run(helper, first, second, personal);
                else if (kind == FixtureKind.SOURCE_MOVE) SourceMoveFixture.run(helper, first, second, personal, shared);
                else if (kind != FixtureKind.MEMBERSHIP) {
                    QuestFixture.run(helper, first, second, personal, shared, kind == FixtureKind.TEAM_QUEST);
                }
            } finally {
                try {
                    if (party != null) {
                        clocks.clear(new com.enviouse.progressivestages.common.stage.OwnerRef(
                            com.enviouse.progressivestages.common.stage.OwnerKind.TEAM, party.getId()), shared);
                        if (teams.getTeamByID(party.getId()).isPresent()) party.forceDisband(server.createCommandSourceStack());
                    }
                    var knownPlayersField = teams.getClass().getDeclaredField("knownPlayers");
                    knownPlayersField.setAccessible(true);
                    var knownPlayers = (java.util.Map<?, ?>) knownPlayersField.get(teams);
                    knownPlayers.remove(firstId);
                    knownPlayers.remove(secondId);
                    var deleteTeam = teams.getClass().getDeclaredMethod("deleteTeam", dev.ftb.mods.ftbteams.data.AbstractTeam.class);
                    deleteTeam.setAccessible(true);
                    for (var id : java.util.List.of(firstId, secondId)) {
                        var fixtureTeam = teams.getTeamMap().get(id);
                        if (fixtureTeam != null) deleteTeam.invoke(teams, fixtureTeam);
                    }
                    teams.getTeamNameMap().entrySet().removeIf(entry -> !originalTeams.contains(entry.getValue().getId()));
                    teams.markDirty();
                    helper.assertTrue(teams.getTeamMap().keySet().equals(originalTeams)
                        && !teams.getKnownPlayerTeams().containsKey(firstId) && !teams.getKnownPlayerTeams().containsKey(secondId),
                        "Fixture teams and player records must be removed after the test.");
                } finally {
                    server.overworld().setData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES, original);
                    order.clear();
                    definitions.forEach(order::registerStage);
                    if (personalClock <= 0) clocks.clear(personalOwner, personal); else clocks.markGranted(personalOwner, personal, personalClock);
                    if (questTeams != null) QuestFixture.restoreTeamData(questTeams);
                    first.discard();
                    second.discard();
                }
            }
        }
    }

    private static final class LegacyFixture {
        static void run(GameTestHelper helper, net.minecraft.server.level.ServerPlayer first,
                        net.minecraft.server.level.ServerPlayer second,
                        com.enviouse.progressivestages.common.api.StageId personal,
                        com.enviouse.progressivestages.common.api.StageId shared) throws Exception {
            var server = helper.getLevel().getServer();
            var manager = com.enviouse.progressivestages.common.stage.StageManager.getInstance();
            var order = com.enviouse.progressivestages.common.stage.StageOrder.getInstance();
            var global = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:legacy_global");
            var foreign = com.enviouse.progressivestages.common.api.StageId.parse("external:legacy_stage");
            order.registerStage(com.enviouse.progressivestages.common.config.StageDefinition.builder(global).scope("server").build());
            replaceDefinition(com.enviouse.progressivestages.common.config.StageDefinition.builder(shared).teamStage(true)
                .rewards(new com.enviouse.progressivestages.common.config.StageRewards(
                    java.util.List.of(), java.util.List.of(), java.util.List.of(), "", 7, 13)).build());
            helper.assertTrue(order.getStageDefinition(shared).orElseThrow().getRewards().xpLevels() == 7,
                "The import fixture must install its acquisition reward before testing that it does not replay.");
            var party = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager().getTeamForPlayer(first).orElseThrow();
            var raw = java.util.Set.of(personal.toString(), shared.toString(), global.toString(), foreign.toString());
            dev.ftb.mods.ftbteams.api.TeamStagesHelper.addTeamStages(party, raw);
            server.overworld().setData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES,
                new com.enviouse.progressivestages.common.data.TeamStageData());
            int firstLevels = first.experienceLevel, secondLevels = second.experienceLevel;
            int firstPoints = first.totalExperience, secondPoints = second.totalExperience;
            var clocks = com.enviouse.progressivestages.server.triggers.StageRegressionData.get(server);
            var teamOwner = new com.enviouse.progressivestages.common.stage.OwnerRef(
                com.enviouse.progressivestages.common.stage.OwnerKind.TEAM, party.getId());
            long clock = clocks.getGrantTime(teamOwner, shared);
            var committed = new java.util.ArrayList<com.enviouse.progressivestages.common.stage.StageMutationResult>();
            try (var listener = manager.subscribeCommittedStageChanges(committed::add)) {
                helper.assertTrue(FTBTeamsIntegration.importLegacyStages(server), "Actual helper records must import once.");
                var data = server.overworld().getData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES);
                helper.assertTrue(manager.hasStage(first, shared) && manager.hasStage(second, shared)
                    && !manager.hasStage(first, personal) && !manager.hasStage(second, personal)
                    && !manager.hasStage(first, global) && !manager.hasStage(second, global),
                    "Legacy shared records must remain in their team namespace without becoming personal or global.");
                helper.assertTrue(data.hasStage(party.getId(), personal) && data.hasStage(party.getId(), global)
                    && !data.hasStage(party.getId(), foreign) && data.getSources(party.getId(), shared).equals(java.util.Set.of("independent")),
                    "Known legacy records must retain independent team provenance while foreign stages remain native.");
                helper.assertTrue(first.experienceLevel == firstLevels && second.experienceLevel == secondLevels
                    && first.totalExperience == firstPoints && second.totalExperience == secondPoints
                    && clocks.getGrantTime(teamOwner, shared) == clock,
                    "Import must not replay configured experience rewards or restart the grant clock.");
                helper.assertTrue(committed.size() == 1 && committed.getFirst().reason().equals("legacy_ftb_imported"),
                    "Import must publish one committed owner change.");
                replaceDefinition(com.enviouse.progressivestages.common.config.StageDefinition.builder(personal).teamStage(true).build());
                helper.assertTrue(manager.hasStage(first, personal) && manager.hasStage(second, personal),
                    "Restoring team scope must recover the preserved legacy team record.");
                replaceDefinition(com.enviouse.progressivestages.common.config.StageDefinition.builder(personal).teamStage(false).build());
                helper.assertTrue(!manager.hasStage(first, personal), "Personal scope must mask the legacy team record again.");
                manager.revokeStage(first, shared);
                long revision = manager.getMutationRevision();
                int notifications = committed.size();
                var encoded = com.enviouse.progressivestages.common.data.TeamStageData.CODEC.encodeStart(
                    net.minecraft.nbt.NbtOps.INSTANCE, data).getOrThrow();
                var loaded = com.enviouse.progressivestages.common.data.TeamStageData.CODEC.parse(
                    net.minecraft.nbt.NbtOps.INSTANCE, encoded).getOrThrow();
                server.overworld().setData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES, loaded);
                helper.assertTrue(!FTBTeamsIntegration.importLegacyStages(server) && !manager.hasStage(first, shared)
                    && manager.getMutationRevision() == revision && committed.size() == notifications,
                    "A persisted import receipt must prevent revoke resurrection and duplicate notifications.");
                helper.assertTrue(java.util.Set.copyOf(dev.ftb.mods.ftbteams.api.TeamStagesHelper.getStages(party)).equals(raw),
                    "Migration must preserve the native helper records for rollback.");
            }
        }
        private static void replaceDefinition(com.enviouse.progressivestages.common.config.StageDefinition definition) {
            var order = com.enviouse.progressivestages.common.stage.StageOrder.getInstance();
            var definitions = new java.util.LinkedHashMap<com.enviouse.progressivestages.common.api.StageId,
                com.enviouse.progressivestages.common.config.StageDefinition>();
            order.getOrderedStages().forEach(id -> definitions.put(id, order.getStageDefinition(id).orElseThrow()));
            definitions.put(definition.getId(), definition);
            order.clear();
            definitions.values().forEach(order::registerStage);
        }
    }

    private static final class SourceMoveFixture {
        static void run(GameTestHelper helper, net.minecraft.server.level.ServerPlayer first,
                        net.minecraft.server.level.ServerPlayer second,
                        com.enviouse.progressivestages.common.api.StageId personal,
                        com.enviouse.progressivestages.common.api.StageId shared) throws Exception {
            var server = helper.getLevel().getServer();
            var manager = com.enviouse.progressivestages.common.stage.StageManager.getInstance();
            var data = server.overworld().getData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES);
            var order = com.enviouse.progressivestages.common.stage.StageOrder.getInstance();
            var party = (dev.ftb.mods.ftbteams.data.PartyTeam) dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api()
                .getManager().getTeamForPlayer(first).orElseThrow();
            var multiple = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:membership_multiple");
            var retained = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:membership_retained");
            var independent = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:membership_independent");
            for (var id : java.util.List.of(multiple, retained, independent)) {
                order.registerStage(com.enviouse.progressivestages.common.config.StageDefinition.builder(id).teamStage(true).build());
            }
            manager.revokeStage(first, shared);
            var departing = new com.enviouse.progressivestages.common.stage.PermissionStageSource(second.getUUID(), "membership", false);
            var remaining = new com.enviouse.progressivestages.common.stage.PermissionStageSource(first.getUUID(), "membership", false);
            var permanent = new com.enviouse.progressivestages.common.stage.PermissionStageSource(second.getUUID(), "retained", true);
            data.grantStageFromSource(party.getId(), shared, departing.label());
            data.grantStageFromSource(party.getId(), multiple, departing.label());
            data.grantStageFromSource(party.getId(), multiple, remaining.label());
            data.grantStageFromSource(party.getId(), retained, permanent.label());
            data.grantStage(party.getId(), independent);
            data.grantStageFromSource(party.getId(), independent, departing.label());
            data.grantPersonalStageFromSource(second.getUUID(), personal, departing.label());
            helper.assertTrue(manager.hasStage(first, shared) && manager.hasStage(second, shared),
                "The departing member must initially contribute effective access to the actual party.");
            var changes = new java.util.ArrayList<com.enviouse.progressivestages.common.stage.StageMutationResult>();
            try (var listener = manager.subscribeCommittedStageChanges(changes::add)) {
                party.leave(second.getUUID());
                helper.assertTrue(!manager.hasStage(first, shared) && !manager.hasStage(second, shared),
                    "A native team leave must withdraw the old synchronized contribution before returning.");
                helper.assertTrue(manager.hasStage(first, multiple) && manager.hasStage(first, retained)
                    && manager.hasStage(first, independent) && manager.hasStage(first, personal)
                    && manager.hasStage(second, personal),
                    "A membership change must preserve other contributors, permanent and independent grants, and personal sources.");
                helper.assertTrue(data.getSources(party.getId(), multiple).equals(java.util.Set.of(remaining.label()))
                    && data.getSources(party.getId(), retained).equals(java.util.Set.of(permanent.label()))
                    && data.getSources(party.getId(), independent).equals(java.util.Set.of("independent"))
                    && changes.size() == 1 && changes.getFirst().reason().equals("source_owner_changed"),
                    "The native event must remove only moved synchronized sources and publish one committed owner change.");
                party.join(null, second.getGameProfile());
                helper.assertTrue(!manager.hasStage(first, shared) && !manager.hasStage(second, shared),
                    "Rejoining alone must not restore a contribution before current provider qualification.");
            }
        }
    }

    private static final class ClaimFixture {
        static void run(GameTestHelper helper, net.minecraft.server.level.ServerPlayer first,
                        net.minecraft.server.level.ServerPlayer second,
                        com.enviouse.progressivestages.common.api.StageId personal) {
            var manager = com.enviouse.progressivestages.common.stage.StageManager.getInstance();
            var file = dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
            var chapter = new dev.ftb.mods.ftbquests.quest.Chapter(0x573110, file, file.getDefaultChapterGroup());
            var quest = new dev.ftb.mods.ftbquests.quest.Quest(0x573111, chapter);
            var data = file.getTeamData(first).orElseThrow();
            helper.assertTrue(data == file.getTeamData(second).orElseThrow(),
                "Both claimants must use the same actual quest team data.");
            var clocks = com.enviouse.progressivestages.server.triggers.StageRegressionData.get(helper.getLevel().getServer());
            var secondOwner = new com.enviouse.progressivestages.common.stage.OwnerRef(
                com.enviouse.progressivestages.common.stage.OwnerKind.PERSONAL, second.getUUID());
            long secondClock = clocks.getGrantTime(secondOwner, personal);
            try {
                for (boolean teamReward : new boolean[] { true, false }) {
                    manager.revokeStage(first, personal);
                    manager.revokeStage(second, personal);
                    var reward = new dev.ftb.mods.ftbquests.quest.reward.StageReward(teamReward ? 0x573112 : 0x573113, quest);
                    var config = new net.minecraft.nbt.CompoundTag();
                    config.putString("stage", personal.toString());
                    config.putBoolean("team_reward", teamReward);
                    reward.readData(config, helper.getLevel().registryAccess());
                    quest.addReward(reward);
                    helper.assertTrue(!data.isRewardClaimed(first.getUUID(), reward)
                        && !data.isRewardClaimed(second.getUUID(), reward), "Each native reward must start unclaimed.");
                    data.claimReward(first, reward, false, 1000L);
                    helper.assertTrue(manager.hasStage(first, personal) && !manager.hasStage(second, personal)
                        && data.isRewardClaimed(first.getUUID(), reward)
                        && data.isRewardClaimed(second.getUUID(), reward) == teamReward,
                        "The first claim must preserve the native claim key while granting only the claimant's profession.");
                    data.claimReward(second, reward, false, 2000L);
                    helper.assertTrue(manager.hasStage(second, personal) == !teamReward,
                        "Only a separately configured per player reward may grant the second claimant's profession.");
                    manager.revokeStage(first, personal);
                    long revision = manager.getMutationRevision();
                    data.claimReward(first, reward, false, 3000L);
                    helper.assertTrue(!manager.hasStage(first, personal) && manager.getMutationRevision() == revision
                        && data.getRewardClaimTime(first.getUUID(), reward).orElseThrow().getTime() == 1000L,
                        "A duplicate native claim must not restore a revoked stage or replace its original claim receipt.");
                    helper.assertTrue(data.resetReward(first.getUUID(), reward), "An explicit native claim reset must succeed.");
                    data.claimReward(first, reward, false, 4000L);
                    helper.assertTrue(manager.hasStage(first, personal)
                        && data.getRewardClaimTime(first.getUUID(), reward).orElseThrow().getTime() == 4000L,
                        "An explicit native claim reset must allow a new legitimate reward claim.");
                    var removal = new dev.ftb.mods.ftbquests.quest.reward.StageReward(teamReward ? 0x573114 : 0x573115, quest);
                    config.putBoolean("remove", true);
                    removal.readData(config, helper.getLevel().registryAccess());
                    quest.addReward(removal);
                    data.claimReward(first, removal, false, 5000L);
                    helper.assertTrue(!manager.hasStage(first, personal)
                        && manager.hasStage(second, personal) == !teamReward,
                        "A native removal claim must preserve other players' personal entitlements.");
                }
            } finally {
                if (secondClock <= 0) clocks.clear(secondOwner, personal); else clocks.markGranted(secondOwner, personal, secondClock);
            }
        }
    }

    private static final class QuestFixture {
        static java.util.Map<java.util.UUID, dev.ftb.mods.ftbquests.quest.TeamData> captureTeamData() throws Exception {
            return new java.util.HashMap<>(teamData());
        }

        static void restoreTeamData(java.util.Map<java.util.UUID, dev.ftb.mods.ftbquests.quest.TeamData> original) throws Exception {
            var data = teamData();
            data.clear();
            data.putAll(original);
        }

        @SuppressWarnings("unchecked")
        private static java.util.Map<java.util.UUID, dev.ftb.mods.ftbquests.quest.TeamData> teamData() throws Exception {
            var field = dev.ftb.mods.ftbquests.quest.BaseQuestFile.class.getDeclaredField("teamDataMap");
            field.setAccessible(true);
            return (java.util.Map<java.util.UUID, dev.ftb.mods.ftbquests.quest.TeamData>) field.get(
                dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE);
        }

        static void run(GameTestHelper helper, net.minecraft.server.level.ServerPlayer first,
                        net.minecraft.server.level.ServerPlayer second,
                        com.enviouse.progressivestages.common.api.StageId personal,
                        com.enviouse.progressivestages.common.api.StageId shared, boolean teamStorage) throws Exception {
            var mode = com.enviouse.progressivestages.common.config.StageConfig.class.getDeclaredField("ftbquestsTeamMode");
            mode.setAccessible(true);
            boolean originalMode = mode.getBoolean(null);
            var manager = com.enviouse.progressivestages.common.stage.StageManager.getInstance();
            var global = com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:native_quest_server");
            var clocks = com.enviouse.progressivestages.server.triggers.StageRegressionData.get(helper.getLevel().getServer());
            var globalOwner = new com.enviouse.progressivestages.common.stage.OwnerRef(
                com.enviouse.progressivestages.common.stage.OwnerKind.SERVER,
                com.enviouse.progressivestages.common.stage.StageManager.SERVER_TEAM);
            long globalClock = clocks.getGrantTime(globalOwner, global);
            var secondOwner = new com.enviouse.progressivestages.common.stage.OwnerRef(
                com.enviouse.progressivestages.common.stage.OwnerKind.PERSONAL, second.getUUID());
            long secondClock = clocks.getGrantTime(secondOwner, personal);
            try {
                mode.setBoolean(null, true);
                com.enviouse.progressivestages.common.stage.StageOrder.getInstance().registerStage(
                    com.enviouse.progressivestages.common.config.StageDefinition.builder(global).scope("server").build());
                var file = dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
                var chapter = new dev.ftb.mods.ftbquests.quest.Chapter(0x573100, file, file.getDefaultChapterGroup());
                var quest = new dev.ftb.mods.ftbquests.quest.Quest(0x573101, chapter);
                var stageProvider = dev.ftb.mods.ftblibrary.integration.stages.StageHelper.INSTANCE.getProvider();
                helper.assertTrue(stageProvider.getName().equals("ProgressiveStagesStageProvider"),
                    "The actual library provider must be registered before quest assertions.");
                stageProvider.remove(first, personal.toString());
                helper.assertTrue(!manager.hasStage(first, personal), "The reward fixture must start without its profession.");
                var reward = new dev.ftb.mods.ftbquests.quest.reward.StageReward(0x573102, quest);
                var task = new dev.ftb.mods.ftbquests.quest.task.StageTask(0x573103, quest);
                var data = new net.minecraft.nbt.CompoundTag();
                data.putString("stage", personal.toString());
                data.putBoolean("team_reward", teamStorage);
                data.putBoolean("team_stage", teamStorage);
                var questTeam = file.getTeamData(first).orElseThrow();
                reward.readData(data, helper.getLevel().registryAccess());
                task.readData(data, helper.getLevel().registryAccess());
                helper.assertTrue(reward.isTeamReward() == teamStorage, "The fixture must retain its configured reward distribution.");
                reward.claim(first, false);
                helper.assertTrue(manager.hasStage(first, personal) && !manager.hasStage(second, personal),
                    "The actual quest reward must grant only the claiming player's profession.");
                helper.assertTrue(stageProvider.has(first, personal.toString()) && task.canSubmit(questTeam, first)
                    && !stageProvider.has(second, personal.toString()) && !task.canSubmit(questTeam, second),
                    "Quest stage checks must use personal ownership even with quest team mode enabled.");
                reward.claim(second, false);
                helper.assertTrue(task.canSubmit(questTeam, first) && task.canSubmit(questTeam, second),
                    "Separately claiming the reward must create each player's own profession.");
                data.putBoolean("remove", true);
                reward.readData(data, helper.getLevel().registryAccess());
                reward.claim(first, false);
                helper.assertTrue(!manager.hasStage(first, personal) && manager.hasStage(second, personal)
                    && !task.canSubmit(questTeam, first) && task.canSubmit(questTeam, second),
                    "A removal reward must remove only its claimant's personal entitlement.");
                var party = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager().getTeamForPlayer(first).orElseThrow();
                dev.ftb.mods.ftbteams.api.TeamStagesHelper.addTeamStage(party, personal.toString());
                helper.assertTrue(!stageProvider.has(first, personal.toString()) && !task.canSubmit(questTeam, first),
                    "A stale shared helper stage must not authorize a personal profession.");
                dev.ftb.mods.ftbteams.api.TeamStagesHelper.removeTeamStage(party, personal.toString());
                data.putString("stage", global.toString());
                data.putBoolean("remove", false);
                reward.readData(data, helper.getLevel().registryAccess());
                task.readData(data, helper.getLevel().registryAccess());
                reward.claim(first, false);
                helper.assertTrue(manager.hasStage(first, global) && manager.hasStage(second, global)
                    && task.canSubmit(questTeam, first) && task.canSubmit(questTeam, second),
                    "Server scope must precede quest helper delegation for both reward and check.");
                data.putBoolean("remove", true);
                reward.readData(data, helper.getLevel().registryAccess());
                reward.claim(second, false);
                helper.assertTrue(!manager.hasStage(first, global) && !task.canSubmit(questTeam, second),
                    "Removing a server stage through a quest reward must revoke the shared entitlement.");
                manager.revokeStage(first, shared);
                data.putString("stage", shared.toString());
                data.putBoolean("remove", false);
                reward.readData(data, helper.getLevel().registryAccess());
                task.readData(data, helper.getLevel().registryAccess());
                reward.claim(first, false);
                helper.assertTrue(manager.hasStage(first, shared) && manager.hasStage(second, shared)
                    && task.canSubmit(questTeam, first) && task.canSubmit(questTeam, second),
                    "Shared quest rewards must update authoritative stage ownership.");
                data.putBoolean("remove", true);
                reward.readData(data, helper.getLevel().registryAccess());
                reward.claim(second, false);
                helper.assertTrue(!manager.hasStage(first, shared) && !task.canSubmit(questTeam, first),
                    "Shared quest removal must update authoritative stage ownership.");
                var savedReward = new net.minecraft.nbt.CompoundTag();
                var savedTask = new net.minecraft.nbt.CompoundTag();
                reward.writeData(savedReward, helper.getLevel().registryAccess());
                task.writeData(savedTask, helper.getLevel().registryAccess());
                helper.assertTrue(savedReward.getBoolean("team_reward") == teamStorage
                    && savedTask.getBoolean("team_stage") == teamStorage,
                    "Storage routing must preserve saved reward and task settings.");
                if (teamStorage) {
                    String foreignStage = "external:quest_fixture";
                    data.putString("stage", foreignStage);
                    data.putBoolean("remove", false);
                    reward.readData(data, helper.getLevel().registryAccess());
                    task.readData(data, helper.getLevel().registryAccess());
                    reward.claim(first, false);
                    helper.assertTrue(dev.ftb.mods.ftbteams.api.TeamStagesHelper.hasTeamStage(party, foreignStage)
                        && task.canSubmit(questTeam, second), "Undefined external stages must retain native team storage.");
                    data.putBoolean("remove", true);
                    reward.readData(data, helper.getLevel().registryAccess());
                    reward.claim(second, false);
                    helper.assertTrue(!dev.ftb.mods.ftbteams.api.TeamStagesHelper.hasTeamStage(party, foreignStage)
                        && !task.canSubmit(questTeam, first), "Undefined external stage removal must remain native.");
                    var enabled = com.enviouse.progressivestages.common.config.StageConfig.class.getDeclaredField("ftbQuestsIntegration");
                    enabled.setAccessible(true);
                    boolean originalEnabled = enabled.getBoolean(null);
                    try {
                        enabled.setBoolean(null, false);
                        data.putString("stage", personal.toString());
                        data.putBoolean("remove", false);
                        reward.readData(data, helper.getLevel().registryAccess());
                        task.readData(data, helper.getLevel().registryAccess());
                        reward.claim(first, false);
                        helper.assertTrue(dev.ftb.mods.ftbteams.api.TeamStagesHelper.hasTeamStage(party, personal.toString())
                            && !manager.hasStage(first, personal) && task.canSubmit(questTeam, first),
                            "Disabled integration must preserve the native team storage route.");
                    } finally {
                        enabled.setBoolean(null, originalEnabled);
                        dev.ftb.mods.ftbteams.api.TeamStagesHelper.removeTeamStage(party, personal.toString());
                    }
                }
            } finally {
                mode.setBoolean(null, originalMode);
                if (globalClock <= 0) clocks.clear(globalOwner, global); else clocks.markGranted(globalOwner, global, globalClock);
                if (secondClock <= 0) clocks.clear(secondOwner, personal); else clocks.markGranted(secondOwner, personal, secondClock);
            }
        }
    }
}
