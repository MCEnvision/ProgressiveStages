package com.enviouse.progressivestages.server.editor;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.ConfigPaths;
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
public final class EditorRevisionGameTests {
    private EditorRevisionGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void sessionApplyRejectsUnreviewedChanges(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var server = level.getServer();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "editor-test"), false);
        var operator = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override public boolean hasPermissions(int permissionLevel) { return permissionLevel <= 3; }
        };
        var sessions = EditorSessionService.get();
        var loader = StageFileLoader.getInstance();
        String folder = "revision_test_" + UUID.randomUUID().toString().replace("-", "");
        String path = "stages/" + folder + "/stage.toml";
        Path stageDirectory = ConfigPaths.rootDirectory().resolve("stages").resolve(folder);
        StageId stage = StageId.parse("progressivestages:" + folder);
        EditorSessionOpen session = null;
        String transaction = "";
        try {
            session = sessions.open(operator);
            JsonObject initial = request(operator, session, "{\"action\":\"bootstrap\"}");
            long revision = initial.getAsJsonObject("draft").get("revision").getAsLong();
            String source = "[schema]\nversion = 4\n[stage]\nid = \"" + stage
                + "\"\ndisplay_name = \"Reviewed\"\n";
            JsonObject edit = new JsonObject();
            edit.addProperty("action", "mutate");
            edit.addProperty("path", path);
            edit.addProperty("content", source);
            edit.addProperty("revision", revision);
            request(operator, session, edit.toString());
            JsonObject review = request(operator, session, "{\"action\":\"review\"}");
            helper.assertTrue(review.getAsJsonObject("validation").get("valid").getAsBoolean(),
                "The initial draft must be valid for review.");
            long reviewed = review.get("revision").getAsLong();
            edit.addProperty("revision", reviewed);
            edit.addProperty("content", source.replace("Reviewed", "Changed after review"));
            JsonObject changed = request(operator, session, edit.toString());
            long current = changed.get("revision").getAsLong();
            long configuration = loader.getCompiledSnapshot().revision();
            JsonObject stale = request(operator, session,
                "{\"action\":\"apply\",\"confirmed\":true,\"revision\":" + reviewed + "}");
            helper.assertTrue("draft_conflict".equals(stale.get("error").getAsString()),
                "Apply must reject changes made after review.");
            helper.assertTrue(stale.get("currentRevision").getAsLong() == current,
                "The conflict must identify the current draft revision.");
            JsonObject missing = request(operator, session, "{\"action\":\"apply\",\"confirmed\":true}");
            helper.assertTrue("draft_conflict".equals(missing.get("error").getAsString()),
                "Apply must require an explicit reviewed revision.");
            helper.assertTrue(!Files.exists(stageDirectory) && loader.getStage(stage).isEmpty()
                    && loader.getCompiledSnapshot().revision() == configuration,
                "Rejected apply must leave files and the active snapshot unchanged.");
            JsonObject freshReview = request(operator, session, "{\"action\":\"review\"}");
            JsonObject applied = request(operator, session,
                "{\"action\":\"apply\",\"confirmed\":true,\"revision\":"
                    + freshReview.get("revision").getAsLong() + "}");
            if (applied.has("transactionId")) transaction = applied.get("transactionId").getAsString();
            helper.assertTrue(applied.has("success") && applied.get("success").getAsBoolean(),
                "Reviewing the current draft must permit the normal apply transaction.");
            helper.assertTrue(loader.getStage(stage).isPresent()
                    && Files.readString(stageDirectory.resolve("stage.toml")).contains("Changed after review"),
                "The newly reviewed source must reach the live snapshot and files.");
            helper.succeed();
        } finally {
            if (session != null) sessions.discard(operator.getUUID(), session.draftId());
            deleteTree(stageDirectory);
            if (!transaction.isEmpty()) deleteTree(ConfigPaths.rootDirectory().resolve(".editor-backups").resolve(transaction));
            loader.reload();
            operator.discard();
        }
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
