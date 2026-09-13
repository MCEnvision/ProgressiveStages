package com.enviouse.progressivestages.server.integration;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class FtbLegacyRestartGameTests {
    private FtbLegacyRestartGameTests() {}

    private static boolean enabled(GameTestHelper helper, String step) {
        if (!ModList.get().isLoaded("ftbquests")
            || !step.equals(System.getProperty("progressivestages.legacyFtbFixtureStep"))) {
            helper.succeed();
            return false;
        }
        helper.assertTrue(helper.getLevel().getServer().isDedicatedServer()
            && helper.getLevel().getServer().getPlayerList().getPlayers().isEmpty(),
            "The restart fixture requires an isolated dedicated server.");
        return true;
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void prepareLegacyFtbRestart(GameTestHelper helper) throws Exception {
        if (!enabled(helper, "prepare")) return;
        Fixture.prepare(helper);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void verifyLegacyFtbRestartAndRevoke(GameTestHelper helper) {
        if (!enabled(helper, "import")) return;
        Fixture.verify(helper, true);
        helper.succeed();
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void verifyLegacyFtbRevokeSurvivesRestart(GameTestHelper helper) {
        if (!enabled(helper, "revoked")) return;
        Fixture.verify(helper, false);
        helper.succeed();
    }

    private static final class Fixture {
        private static final java.util.UUID TEAM = new java.util.UUID(0x5731, 0x70);
        private static final com.enviouse.progressivestages.common.api.StageId STAGE =
            com.enviouse.progressivestages.common.api.StageId.parse("progressivestages:legacy_restart");

        static void prepare(GameTestHelper helper) throws Exception {
            var server = helper.getLevel().getServer();
            var teams = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
            helper.assertTrue(teams.getTeamByID(TEAM).isEmpty()
                && com.enviouse.progressivestages.common.stage.StageOrder.getInstance().stageExists(STAGE),
                "The restart fixture requires its isolated stage definition and unused team identity.");
            var enabled = com.enviouse.progressivestages.common.config.StageConfig.class.getDeclaredField("ftbQuestsIntegration");
            enabled.setAccessible(true);
            enabled.setBoolean(null, false);
            var team = teams.createServerTeam(server.createCommandSourceStack(), "legacy restart fixture", "",
                dev.ftb.mods.ftblibrary.icon.Color4I.WHITE, TEAM);
            dev.ftb.mods.ftbteams.api.TeamStagesHelper.addTeamStage(team, STAGE.toString());
            server.overworld().setData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES,
                new com.enviouse.progressivestages.common.data.TeamStageData());
            ((dev.ftb.mods.ftbteams.data.TeamManagerImpl) teams).saveNow();
            server.saveEverything(false, true, true);
            helper.assertTrue(!com.enviouse.progressivestages.common.stage.StageManager.getInstance().hasImportedFtbHelperStages(),
                "The prepared world must retain an unimported legacy helper record until restart.");
        }

        static void verify(GameTestHelper helper, boolean imported) {
            var server = helper.getLevel().getServer();
            var manager = com.enviouse.progressivestages.common.stage.StageManager.getInstance();
            var teams = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
            helper.assertTrue(teams.getTeamForPlayerID(new java.util.UUID(0x5731, 1)).isEmpty()
                && teams.getTeamForPlayerID(new java.util.UUID(0x5731, 2)).isEmpty(),
                "Earlier membership fixtures must not leave provider player files across restart.");
            var team = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager().getTeamByID(TEAM).orElseThrow();
            helper.assertTrue(manager.hasImportedFtbHelperStages() && manager.hasStage(TEAM, STAGE) == imported
                && dev.ftb.mods.ftbteams.api.TeamStagesHelper.hasTeamStage(team, STAGE.toString()),
                "Startup must respect the persisted import receipt while preserving the old helper record.");
            if (imported) {
                var data = server.overworld().getData(com.enviouse.progressivestages.common.data.StageAttachments.TEAM_STAGES);
                helper.assertTrue(data.getSources(TEAM, STAGE).equals(java.util.Set.of("independent")),
                    "Startup import must retain independent team provenance.");
                manager.revokeStageFromTeam(TEAM, STAGE);
                helper.assertTrue(!manager.hasStage(TEAM, STAGE), "The legacy team revoke must remove the imported stage.");
                server.saveEverything(false, true, true);
            }
            helper.assertTrue(!FTBTeamsIntegration.importLegacyStages(server) && !manager.hasStage(TEAM, STAGE),
                "Another import attempt must not resurrect the revoked stage.");
        }
    }
}
