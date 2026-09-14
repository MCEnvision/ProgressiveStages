package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.config.ConfigPaths;
import com.enviouse.progressivestages.server.editor.EditorSessionOpen;
import com.enviouse.progressivestages.server.editor.EditorSessionService;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class EditorCaptureGameTests {
    private EditorCaptureGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 200)
    public static void editorCaptureExplainsRejectedAndCommittedApply(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var server = level.getServer();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "editor-capture"), false);
        var operator = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override public boolean hasPermissions(int permissionLevel) { return permissionLevel <= 3; }
        };
        var sessions = EditorSessionService.get();
        var loader = StageFileLoader.getInstance();
        String id = UUID.randomUUID().toString();
        String folder = "capture_test_" + id.replace("-", "");
        Path directory = ConfigPaths.stagesDirectory().resolve(folder);
        Path output = server.getServerDirectory().resolve("logs/progressivestages/editor").resolve(id + ".log");
        var capture = new InteractionCaptureManager.Capture(id, operator.getUUID(), "editor", output,
            server.getTickCount(), CaptureIdentity.snapshot());
        var active = InteractionCaptureManager.class.getDeclaredField("active");
        active.setAccessible(true);
        helper.assertTrue(active.get(null) == null, "The fixture requires no existing capture.");
        EditorSessionOpen session = null;
        String transaction = "";
        long initialDefinition = loader.getCompiledSnapshot().revision();
        var baselineWarnings = new java.util.concurrent.atomic.AtomicInteger();
        try {
            session = sessions.open(operator);
            var initial = request(operator, session, "{\"action\":\"bootstrap\"}");
            helper.assertTrue(initial.get("capabilities").isJsonArray()
                    && initial.getAsJsonObject("stageCapabilities").has("luckPerms")
                    && initial.getAsJsonObject("validation").has("validatedRevision"),
                "Bootstrap must preserve legacy capabilities and add typed draft capabilities.");
            var baseline = initial.getAsJsonObject("validation");
            helper.assertTrue(baseline.get("valid").getAsBoolean()
                    && baseline.getAsJsonArray("diagnostics").asList().stream().allMatch(value ->
                        "WARNING".equals(value.getAsJsonObject().get("severity").getAsString())
                        && "provider_fallback".equals(value.getAsJsonObject().get("code").getAsString())),
                "The default draft may contain only current team fallback warnings.");
            baselineWarnings.set(baseline.getAsJsonArray("diagnostics").size());
            long revision = initial.getAsJsonObject("draft").get("revision").getAsLong();
            active.set(null, capture);
            capture.startWriter();
            String source = "[schema]\nversion = 4\n[stage]\nid = \"progressivestages:" + folder
                + "\"\nscope = \"server\"\nteam_stage = false\n";
            JsonObject edit = new JsonObject();
            edit.addProperty("action", "mutate");
            edit.addProperty("path", "stages/" + folder + "/stage.toml");
            edit.addProperty("content", source);
            edit.addProperty("revision", revision);
            var changed = request(operator, session, edit.toString());
            long current = changed.get("revision").getAsLong();
            var rejected = request(operator, session,
                "{\"action\":\"apply\",\"confirmed\":true,\"revision\":" + current + "}");
            helper.assertTrue("validation_failed".equals(rejected.get("code").getAsString()),
                "Invalid stage options must reject apply.");
            var diagnostic = rejected.getAsJsonObject("validation").getAsJsonArray("diagnostics").get(0).getAsJsonObject();
            helper.assertTrue("stage.team_stage".equals(diagnostic.get("field").getAsString())
                    && "server_override".equals(diagnostic.get("code").getAsString())
                    && ("stages/" + folder + "/stage.toml").equals(diagnostic.get("file").getAsString()),
                "Rejected apply must return the owning field and package identity path.");
            helper.assertTrue(!Files.exists(directory) && loader.getCompiledSnapshot().revision() == initialDefinition,
                "Invalid apply must preserve files and the installed revision.");
            var stale = request(operator, session, "{\"action\":\"apply\",\"confirmed\":true,\"revision\":-1}");
            helper.assertTrue("draft_conflict".equals(stale.get("error").getAsString()),
                "The stale request must be rejected before apply.");
            String validSource = source.replace("scope = \"server\"", "scope = \"team\"")
                + "[luckperms]\nenabled = false\n[[luckperms.outbound]]\nid = \"chef_output\"\nkind = \"group\"\nvalue = \"chef\"\n"
                + "[[command_permissions]]\nid = \"known\"\npath = \"stage\"\n"
                + "[[command_permissions]]\nid = \"missing\"\npath = \"missing_fixture_command\"\n";
            edit.addProperty("content", validSource);
            edit.addProperty("revision", current);
            changed = request(operator, session, edit.toString());
            var applied = request(operator, session,
                "{\"action\":\"apply\",\"confirmed\":true,\"revision\":" + changed.get("revision").getAsLong() + "}");
            if (applied.has("transactionId")) transaction = applied.get("transactionId").getAsString();
            helper.assertTrue(applied.get("success").getAsBoolean(), "The valid draft must apply normally.");
            var capabilities = applied.getAsJsonObject("validation").getAsJsonObject("stageCapabilities");
            helper.assertTrue(capabilities.getAsJsonObject("configuredGroupStatus").has("chef")
                    && "RESOLVED".equals(capabilities.getAsJsonObject("configuredCommandStatus").get("stage").getAsString())
                    && "MISSING".equals(capabilities.getAsJsonObject("configuredCommandStatus").get("missing_fixture_command").getAsString()),
                "New draft groups and commands must be checked before they are installed.");
            var warnings = applied.getAsJsonObject("validation").getAsJsonArray("diagnostics");
            helper.assertTrue(warnings.asList().stream().anyMatch(value -> "bridge_disabled".equals(value.getAsJsonObject().get("code").getAsString()))
                    && warnings.asList().stream().anyMatch(value -> "missing_command".equals(value.getAsJsonObject().get("code").getAsString())
                        && "missing".equals(value.getAsJsonObject().get("ruleId").getAsString())),
                "Dormant mappings must return field warnings without blocking apply.");
            helper.assertTrue(Files.readString(directory.resolve("stage.toml")).equals(validSource),
                "Capability warnings must preserve the applied source exactly.");
            helper.assertTrue(!capture.status().active() && capture.status().stopReason().equals("reload"),
                "The actual reload must stop accepting captures.");
            request(operator, session, "{\"action\":\"bootstrap\"}");
            helper.assertTrue(capture.status().records() == 5, "No new operation may enter after reload.");
        } finally {
            active.set(null, null);
            capture.stop(InteractionCaptureManager.StopReason.MANUAL);
            if (session != null) sessions.discard(operator.getUUID(), session.draftId());
            deleteTree(directory);
            if (!transaction.isEmpty()) deleteTree(ConfigPaths.rootDirectory().resolve(".editor-backups").resolve(transaction));
            loader.reload();
            operator.discard();
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(capture.canReplace() && capture.status().outputState().equals("drained"),
                "The accepted apply observation must drain after reload.");
            try {
                var lines = Files.readAllLines(output);
                helper.assertTrue(lines.size() == 6, "One header and five editor observations must be written.");
                var invalid = JsonParser.parseString(lines.get(2)).getAsJsonObject();
                var stale = JsonParser.parseString(lines.get(3)).getAsJsonObject();
                var applied = JsonParser.parseString(lines.get(5)).getAsJsonObject();
                helper.assertTrue(invalid.get("reason").getAsString().equals("invalid_field")
                        && invalid.get("source_preserved").getAsBoolean()
                        && invalid.get("definition_revision").getAsLong() == initialDefinition,
                    "Invalid apply must explain source and runtime preservation.");
                helper.assertTrue(invalid.get("field").getAsString().equals("stage.team_stage")
                        && invalid.get("severity").getAsString().equals("ERROR")
                        && invalid.get("validation_code").getAsString().equals("server_override")
                        && invalid.get("operation_code").getAsString().equals("validation_failed")
                        && invalid.get("file_role").getAsString().equals("identity")
                        && invalid.get("diagnostic_count").getAsInt() == baselineWarnings.get() + 1
                        && !invalid.get("provider_state").getAsString().equals("not_observed"),
                    "The capture must identify the rejected field without source text.");
                helper.assertTrue(stale.get("reason").getAsString().equals("stale_revision"),
                    "A stale request must have a distinct reason.");
                helper.assertTrue(applied.get("reason").getAsString().equals("applied")
                        && applied.get("apply_revision").getAsLong() > initialDefinition
                        && applied.get("definition_revision").equals(applied.get("apply_revision")),
                    "The final observation must identify the successfully installed revision.");
                helper.assertTrue(lines.stream().noneMatch(line -> line.contains(folder) || line.contains("team_stage =")),
                    "Diagnostic output must not disclose source paths or source text.");
            } catch (java.io.IOException failure) { helper.fail("The editor capture could not be read."); }
        });
    }

    private static JsonObject request(ServerPlayer operator, EditorSessionOpen session, String request) {
        return JsonParser.parseString(EditorSessionService.get().handle(operator,
            session.sessionId(), session.secret(), request)).getAsJsonObject();
    }

    private static void deleteTree(Path path) throws Exception {
        if (!Files.exists(path)) return;
        try (var entries = Files.walk(path)) {
            for (Path entry : entries.sorted(Comparator.reverseOrder()).toList()) Files.delete(entry);
        }
    }
}
