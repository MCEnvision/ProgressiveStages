package com.enviouse.progressivestages.server.editor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
public final class EditorImportGameTests {
    private EditorImportGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void packageImportRejectsInvalidFilesAtomically(GameTestHelper helper) {
        ServerPlayer operator = operator(helper);
        var sessions = EditorSessionService.get();
        EditorSessionOpen session = sessions.open(operator);
        try {
            JsonObject before = request(operator, session, action("bootstrap")).getAsJsonObject("draft");
            String folder = "import_guard_" + UUID.randomUUID().toString().replace("-", "");
            for (String invalid : new String[] {"script.js", "../outside.toml", "nested/../../outside.toml",
                    "/outside.toml", "C:/outside.toml", "nested/../stage.toml"}) {
                JsonObject files = new JsonObject();
                files.addProperty("stage.toml", identity(folder));
                files.addProperty(invalid, "# Rejected file.\n");
                JsonObject command = action("import_stage");
                command.addProperty("destination", folder);
                command.addProperty("revision", before.get("revision").getAsLong());
                command.add("files", files);
                JsonObject result = request(operator, session, command);
                helper.assertTrue(result.has("error"), "An unsafe package must be rejected.");
                JsonObject after = request(operator, session, action("bootstrap")).getAsJsonObject("draft");
                for (String field : new String[] {"revision", "files", "canUndo", "canRedo"}) {
                    helper.assertTrue(before.get(field).equals(after.get(field)),
                        "A rejected import must preserve the draft and its undo history. " + field);
                }
            }
            helper.succeed();
        } finally {
            helper.assertTrue(sessions.discard(operator.getUUID(), session.draftId()),
                "The import fixture draft must be removed.");
            operator.discard();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void packageExportReimportsNestedToml(GameTestHelper helper) {
        ServerPlayer operator = operator(helper);
        var sessions = EditorSessionService.get();
        EditorSessionOpen session = sessions.open(operator);
        try {
            JsonObject before = request(operator, session, action("bootstrap")).getAsJsonObject("draft");
            long revision = before.get("revision").getAsLong();
            String folder = "import_roundtrip_" + UUID.randomUUID().toString().replace("-", "");
            JsonObject files = new JsonObject();
            files.addProperty("stage.toml", identity(folder).replace("[stage]",
                "[package]\nrules_includes = [\"nested/foods.toml\"]\n[stage]"));
            files.addProperty("rules.toml", "# Main rules stay empty.\n");
            files.addProperty("nested/foods.toml", "# Preserve this included rule.\n[items]\nlocked = [\"minecraft:bread\"]\n");
            files.addProperty("notes.toml", "# Keep unrelated source.\n[notes]\n\"quoted key\" = 'Keep this text.'\n");
            for (var file : files.entrySet()) {
                JsonObject edit = action("mutate");
                edit.addProperty("path", "stages/" + folder + "/" + file.getKey());
                edit.addProperty("content", file.getValue().getAsString());
                edit.addProperty("revision", revision);
                revision = request(operator, session, edit).get("revision").getAsLong();
            }
            JsonObject export = action("export_stage");
            export.addProperty("folder", "stages/" + folder);
            JsonObject exported = request(operator, session, export).getAsJsonObject("files");
            helper.assertTrue(files.equals(exported), "Export must preserve every package file byte for byte.");
            JsonObject remove = action("delete_stage");
            remove.addProperty("folder", "stages/" + folder);
            remove.addProperty("revision", revision);
            revision = request(operator, session, remove).get("revision").getAsLong();
            JsonObject beforeImport = request(operator, session, action("bootstrap")).getAsJsonObject("draft");
            JsonObject command = action("import_stage");
            command.addProperty("destination", folder + "_copy");
            command.addProperty("revision", revision);
            command.add("files", exported);
            JsonObject imported = request(operator, session, command);
            helper.assertTrue(!imported.has("error"), "An exported package with nested TOML must import.");
            revision = imported.get("revision").getAsLong();
            helper.assertTrue(request(operator, session, action("validate")).get("valid").getAsBoolean(),
                "The reimported package must compile with its included rules.");
            export.addProperty("folder", "stages/" + folder + "_copy");
            helper.assertTrue(exported.equals(request(operator, session, export).getAsJsonObject("files")),
                "Reexport must preserve nested files, helper files, comments and quoted keys.");
            for (int index = 0; index < files.size(); index++) {
                JsonObject undo = action("undo");
                undo.addProperty("revision", revision);
                revision = request(operator, session, undo).get("revision").getAsLong();
            }
            helper.assertTrue(beforeImport.get("files").equals(
                    request(operator, session, action("bootstrap")).getAsJsonObject("draft").get("files")),
                "Undoing the imported files must restore the earlier draft.");
            for (int index = 0; index < files.size(); index++) {
                JsonObject redo = action("redo");
                redo.addProperty("revision", revision);
                revision = request(operator, session, redo).get("revision").getAsLong();
            }
            helper.assertTrue(exported.equals(request(operator, session, export).getAsJsonObject("files")),
                "Redo must restore every imported file unchanged.");
            helper.succeed();
        } finally {
            helper.assertTrue(sessions.discard(operator.getUUID(), session.draftId()),
                "The import fixture draft must be removed.");
            operator.discard();
        }
    }

    private static ServerPlayer operator(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "import-test"), false);
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override public boolean hasPermissions(int permissionLevel) { return permissionLevel <= 3; }
        };
    }

    private static String identity(String folder) {
        return "[schema]\nversion = 4\n[stage]\nid = \"test:" + folder + "\"\n";
    }

    private static JsonObject action(String name) {
        JsonObject result = new JsonObject();
        result.addProperty("action", name);
        return result;
    }

    private static JsonObject request(ServerPlayer operator, EditorSessionOpen session, JsonObject request) {
        return JsonParser.parseString(EditorSessionService.get().handle(operator,
            session.sessionId(), session.secret(), request.toString())).getAsJsonObject();
    }
}
