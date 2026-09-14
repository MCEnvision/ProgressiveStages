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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class EditorContextGameTests {
    private EditorContextGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 200)
    public static void contextApplyPreservesSourceAndRejectsOverflow(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var server = level.getServer();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "context-test"), false);
        var operator = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override public boolean hasPermissions(int permissionLevel) { return permissionLevel <= 3; }
        };
        var sessions = EditorSessionService.get();
        var loader = StageFileLoader.getInstance();
        String folder = "context_test_" + UUID.randomUUID().toString().replace("-", "");
        StageId stage = StageId.parse("progressivestages:" + folder);
        String path = "stages/" + folder + "/stage.toml";
        Path directory = ConfigPaths.stagesDirectory().resolve(folder);
        EditorSessionOpen session = null;
        String transaction = "";
        long baseline = loader.getCompiledSnapshot().revision();
        try {
            session = sessions.open(operator);
            var initial = request(operator, session, "{\"action\":\"bootstrap\"}");
            long revision = initial.getAsJsonObject("draft").get("revision").getAsLong();
            String source = "[schema]\nversion = 4\n[stage]\nid = \"" + stage
                + "\"\nteam_stage = false\n[luckperms]\nenabled = false\n"
                + "[[luckperms.inbound]]\nid = 'rank'\ngroups = ['chef'] # Keep the condition note.\n"
                + "[luckperms.inbound.contexts]\n\"server.name\" = [\n  'first,second', # Keep this value note.\n  \"third\\nfourth\",\n]\n"
                + "[[luckperms.outbound]]\nid = 'output'\nkind = 'permission'\nvalue = 'profession.chef'\n"
                + "[luckperms.outbound.contexts]\nworld = " + values(8) + "\nregion = " + values(8)
                + "\nserver = " + values(4) + "\n";
            String invalid = source.replace("world = " + values(8), "world = " + values(9));
            JsonObject edit = new JsonObject();
            edit.addProperty("action", "mutate");
            edit.addProperty("path", path);
            edit.addProperty("content", invalid);
            edit.addProperty("revision", revision);
            var changed = request(operator, session, edit.toString());
            var rejected = request(operator, session, "{\"action\":\"apply\",\"confirmed\":true,\"revision\":"
                + changed.get("revision").getAsLong() + "}");
            helper.assertTrue("validation_failed".equals(rejected.get("code").getAsString()),
                "Nine values must reject apply before any file or runtime change.");
            var diagnostics = rejected.getAsJsonObject("validation").getAsJsonArray("diagnostics");
            helper.assertTrue(diagnostics.asList().stream().anyMatch(value -> {
                var diagnostic = value.getAsJsonObject();
                return "luckperms.outbound[0].contexts".equals(diagnostic.get("field").getAsString())
                    && "output".equals(diagnostic.get("ruleId").getAsString());
            }), "The rejected context must identify the field and mapping.");
            var retained = request(operator, session, "{\"action\":\"bootstrap\"}");
            helper.assertTrue(invalid.equals(retained.getAsJsonObject("draft").getAsJsonObject("files").get(path).getAsString())
                    && !Files.exists(directory) && loader.getCompiledSnapshot().revision() == baseline,
                "Rejected apply must preserve the draft exactly and leave live state unchanged.");
            edit.addProperty("content", source);
            edit.addProperty("revision", changed.get("revision").getAsLong());
            changed = request(operator, session, edit.toString());
            var applied = request(operator, session, "{\"action\":\"apply\",\"confirmed\":true,\"revision\":"
                + changed.get("revision").getAsLong() + "}");
            if (applied.has("transactionId")) transaction = applied.get("transactionId").getAsString();
            helper.assertTrue(applied.get("success").getAsBoolean(), "A corrected context map must apply normally.");
            helper.assertTrue(Files.readString(directory.resolve("stage.toml")).equals(source),
                "Apply must preserve quotes, multiline arrays, comments and omitted defaults exactly.");
            var options = loader.getStage(stage).orElseThrow().getLuckPerms();
            helper.assertTrue(options.inbound().getFirst().contexts().get("server.name")
                    .equals(List.of("first,second", "third\nfourth")),
                "The loaded stage must retain a dotted key and unsplit values.");
            helper.assertTrue(options.outbound().getFirst().contexts().values().stream().mapToInt(List::size)
                    .reduce(1, (left, right) -> left * right) == 256,
                "The exact 256 combination boundary must reach the live definition.");
        } finally {
            if (session != null) sessions.discard(operator.getUUID(), session.draftId());
            deleteTree(directory);
            if (!transaction.isEmpty()) deleteTree(ConfigPaths.rootDirectory().resolve(".editor-backups").resolve(transaction));
            helper.assertTrue(loader.reload(), "Fixture cleanup must restore the valid baseline.");
            operator.discard();
        }
        helper.succeed();
    }

    private static String values(int count) {
        return IntStream.range(0, count).mapToObj(index -> "\"value" + index + "\"")
            .collect(Collectors.joining(", ", "[", "]"));
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
