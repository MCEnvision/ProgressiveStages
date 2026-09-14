package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class DiagnosticCaptureGameTests {
    private DiagnosticCaptureGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 100, batch = "progressivestages_diagnostic_capture")
    public static void captureUsesServerTicksInAnExistingWorld(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        var debug = server.getCommands().getDispatcher().getRoot().getChild("stage").getChild("debug");
        for (String category : java.util.List.of("interactions", "progression", "permissions", "editor")) {
            var command = debug.getChild(category);
            helper.assertTrue(command != null && command.getChild("status") != null
                    && command.getChild("on") != null && command.getChild("off") != null,
                "Every capture category must register beneath stage debug.");
            helper.assertTrue(!command.canUse(server.createCommandSourceStack().withPermission(0)),
                "Capture commands must reject sources without operator permission.");
        }
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "capture-test"), false);
        var player = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
        String id = UUID.randomUUID().toString();
        var capture = new InteractionCaptureManager.Capture(id, player.getUUID(), "permissions",
            server.getServerDirectory().resolve("logs/progressivestages/permissions").resolve(id + ".log"),
            server.getTickCount(), CaptureIdentity.snapshot());
        long originalTime = level.getGameTime();
        try {
            helper.assertTrue(InteractionCaptureManager.start(server, player).invalidTarget(),
                "An unconnected player must not satisfy the online capture target requirement.");
            server.getWorldData().overworldData().setGameTime(server.getTickCount() + 100000L);
            capture.recordPermission(player, StageId.parse("test:clock"), "clock", true,
                "reconciled", "absent");
            helper.assertTrue(capture.status().records() == 1 && capture.status().active(),
                "An existing world's age must not expire a newly started capture.");
        } finally {
            server.getWorldData().overworldData().setGameTime(originalTime);
            capture.stop(InteractionCaptureManager.StopReason.MANUAL);
            capture.startWriter();
            player.discard();
        }
        helper.succeedWhen(() -> {
            Thread.yield();
            try { Thread.sleep(1L); } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Capture verification was interrupted.", interrupted);
            }
            helper.assertTrue(capture.canReplace(), "The stopped capture writer must finish.");
            helper.assertTrue(capture.status().outputState().equals("drained"),
                "The accepted record must drain successfully.");
            helper.assertTrue(capture.status().queued() == 0, "No capture record may remain queued.");
            try {
                String first = java.nio.file.Files.readAllLines(capture.status().output()).getFirst();
                var header = com.google.gson.JsonParser.parseString(first).getAsJsonObject();
                helper.assertTrue(header.get("configuration_sha256").getAsString().length() == 64,
                    "A runtime capture must fingerprint the loaded configuration.");
                helper.assertTrue(header.getAsJsonArray("artifacts").size() >= 3,
                    "A runtime capture must identify the mod, game and loader.");
            } catch (java.io.IOException failure) {
                helper.fail("The capture header could not be read.");
            }
        });
    }
}
