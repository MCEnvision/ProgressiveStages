package com.enviouse.progressivestages.server.editor;

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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class EditorAuthorizationGameTests {
    private EditorAuthorizationGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void removedCollaboratorsLoseEverySessionAction(GameTestHelper helper) {
        var permissions = new AtomicInteger(3);
        var owner = player(helper, "draft-owner", new AtomicInteger(3));
        var collaborator = player(helper, "draft-collaborator", permissions);
        var stranger = player(helper, "draft-stranger", new AtomicInteger(3));
        var sessions = EditorSessionService.get();
        EditorSessionOpen ownerSession = null;
        String path = "stages/auth_test_" + UUID.randomUUID().toString().replace("-", "") + "/stage.toml";
        long configuration = StageFileLoader.getInstance().getCompiledSnapshot().revision();
        try {
            ownerSession = sessions.open(owner);
            changeCollaborator(helper, owner, ownerSession, collaborator, true);
            var shared = sessions.resume(collaborator, ownerSession.draftId());
            var secondShared = sessions.resume(collaborator, ownerSession.draftId());
            var before = request(collaborator, shared, "{\"action\":\"bootstrap\"}");
            helper.assertTrue(before.has("draft"), "An invited operator must be able to read the shared draft.");
            long revision = before.getAsJsonObject("draft").get("revision").getAsLong();
            JsonObject edit = new JsonObject();
            edit.addProperty("action", "mutate");
            edit.addProperty("revision", revision);
            edit.addProperty("path", path);
            edit.addProperty("content", "[stage]\ndisplay_name = 'Authorization fixture'\nteam_stage = false\n");
            var changed = request(owner, ownerSession, edit.toString());
            helper.assertTrue(changed.has("revision"), "The owner must retain normal draft editing.");
            revision = changed.get("revision").getAsLong();
            changeCollaborator(helper, owner, ownerSession, collaborator, false);

            for (var session : List.of(shared, secondShared)) {
                for (String action : List.of("bootstrap", "catalog", "mutate", "undo", "redo", "validate", "review",
                        "apply", "rollback", "scaffold", "duplicate_stage", "delete_stage", "rename_stage", "move_stage",
                        "archive_stage", "restore_stage", "export_stage", "import_stage", "collaborator_add", "collaborator_remove")) {
                    assertDenied(helper, request(collaborator, session,
                        "{\"action\":\"" + action + "\",\"confirmed\":true,\"revision\":" + revision + "}"));
                }
            }
            helper.assertTrue(!Files.exists(ConfigPaths.rootDirectory().resolve(path))
                    && StageFileLoader.getInstance().getCompiledSnapshot().revision() == configuration,
                "Removed collaborator requests must leave live files and definitions unchanged.");
            helper.assertTrue(request(owner, ownerSession, "{\"action\":\"bootstrap\"}").has("draft"),
                "Removing a collaborator must preserve the owner's session.");

            changeCollaborator(helper, owner, ownerSession, collaborator, true);
            assertDenied(helper, request(collaborator, shared, "{\"action\":\"bootstrap\"}"));
            assertDenied(helper, request(collaborator, secondShared, "{\"action\":\"bootstrap\"}"));
            var restored = sessions.resume(collaborator, ownerSession.draftId());
            helper.assertTrue(request(collaborator, restored, "{\"action\":\"bootstrap\"}").has("draft"),
                "An invited operator must resume with a fresh session after access is restored.");
            assertDenied(helper, request(stranger, restored, "{\"action\":\"bootstrap\"}"));
            assertDenied(helper, JsonParser.parseString(sessions.handle(collaborator, restored.sessionId(),
                "invalid", "{\"action\":\"bootstrap\"}")).getAsJsonObject());
            helper.assertTrue(request(collaborator, restored, "{\"action\":\"bootstrap\"}").has("draft"),
                "A rejected actor or credential must not revoke another operator's valid session.");

            permissions.set(0);
            assertDenied(helper, request(collaborator, restored, "{\"action\":\"bootstrap\"}"));
            permissions.set(3);
            assertDenied(helper, request(collaborator, restored, "{\"action\":\"bootstrap\"}"));
            helper.succeed();
        } finally {
            if (ownerSession != null) {
                helper.assertTrue(sessions.discard(owner.getUUID(), ownerSession.draftId()),
                    "The owned draft file and all its sessions must be removed.");
            }
            owner.discard();
            collaborator.discard();
            stranger.discard();
        }
    }

    private static void changeCollaborator(GameTestHelper helper, ServerPlayer owner, EditorSessionOpen session,
                                           ServerPlayer collaborator, boolean add) {
        var result = request(owner, session, "{\"action\":\"collaborator_" + (add ? "add" : "remove")
            + "\",\"player\":\"" + collaborator.getUUID() + "\"}");
        helper.assertTrue(result.has("collaborators"), "Only the owner changes the shared draft access list.");
    }

    private static JsonObject request(ServerPlayer actor, EditorSessionOpen session, String body) {
        return JsonParser.parseString(EditorSessionService.get().handle(actor, session.sessionId(), session.secret(), body))
            .getAsJsonObject();
    }

    private static void assertDenied(GameTestHelper helper, JsonObject response) {
        helper.assertTrue(response.has("error") && response.get("error").getAsString().equals("unauthorized"),
            "An unauthorized editor request must be rejected before its action runs.");
    }

    private static ServerPlayer player(GameTestHelper helper, String name, AtomicInteger permissions) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override public boolean hasPermissions(int level) { return level <= permissions.get(); }
        };
    }
}
