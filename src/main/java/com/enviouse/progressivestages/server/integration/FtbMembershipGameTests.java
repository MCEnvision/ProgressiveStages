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
        Fixture.run(helper);
        helper.succeed();
    }

    private static final class Fixture {
        static void run(GameTestHelper helper) throws Exception {
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
                    first.discard();
                    second.discard();
                }
            }
        }
    }
}
