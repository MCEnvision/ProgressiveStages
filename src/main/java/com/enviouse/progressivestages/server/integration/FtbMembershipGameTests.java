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
        Fixture.run(helper, false, false);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeQuestRewardsAndChecksRespectPersonalOwnership(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbquests")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, true, false);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void nativeTeamQuestSettingsRespectStageOwnership(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("ftbquests")) {
            helper.succeed();
            return;
        }
        Fixture.run(helper, true, true);
        helper.succeed();
    }

    private static final class Fixture {
        static void run(GameTestHelper helper, boolean verifyQuests, boolean teamStorage) throws Exception {
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
                if (verifyQuests) QuestFixture.run(helper, first, second, personal, shared, teamStorage);
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
                    teams.getTeamMap().remove(firstId);
                    teams.getTeamMap().remove(secondId);
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
