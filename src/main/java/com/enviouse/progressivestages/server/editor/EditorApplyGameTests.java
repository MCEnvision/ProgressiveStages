package com.enviouse.progressivestages.server.editor;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.ConfigPaths;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class EditorApplyGameTests {
    private static final StageId STAGE = StageId.parse("progressivestages:editor_apply_gametest");
    private static final String FOLDER = "stages/editor_apply_gametest";

    private EditorApplyGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void applyWritesReloadsAndRestoresCanonicalRules(GameTestHelper helper) {
        StageFileLoader loader = StageFileLoader.getInstance();
        Path root = ConfigPaths.rootDirectory();
        Path stageDirectory = root.resolve(FOLDER);
        UUID operator = UUID.randomUUID();
        long baselineRevision = loader.getCompiledSnapshot().revision();
        EditorDraft draft = new EditorDraft(UUID.randomUUID(), operator, baselineRevision, 0, Map.of());
        EditorApplyResult result = null;

        try {
            long draftRevision = draft.mutate(operator, 0, FOLDER + "/stage.toml", stageToml());
            draftRevision = draft.mutate(operator, draftRevision, FOLDER + "/rules.toml", rulesToml());
            draft.mutate(operator, draftRevision, FOLDER + "/progression.toml", "# Progression is optional.\n");

            result = new EditorApplyService(root).apply(helper.getLevel().getServer(), operator, draft,
                baselineRevision, true);

            helper.assertTrue(result.success(), "a valid editor draft must apply through the live server reload");
            helper.assertTrue(Files.readString(stageDirectory.resolve("rules.toml")).contains("locked_items"),
                "an applied recipe output lock must persist with its canonical key");
            helper.assertTrue(Files.readString(stageDirectory.resolve("rules.toml")).contains("item_into_inventory"),
                "an applied inventory rule must persist through the same server transaction");
            helper.assertTrue(loader.getStage(STAGE).isPresent(),
                "the loader must expose the stage from the applied and reloaded snapshot");
            helper.assertTrue(loader.getCompiledSnapshot().revision() > baselineRevision,
                "a successful editor apply must advance the compiled configuration revision");

            deleteTree(stageDirectory);
            helper.assertTrue(loader.reload(), "removing the test stage must restore the prior valid snapshot");
            helper.assertTrue(loader.getStage(STAGE).isEmpty(),
                "the restored snapshot must not retain an editor stage after its file is removed");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Editor apply transaction failed: " + failure.getMessage());
        } finally {
            try {
                deleteTree(stageDirectory);
                if (result != null && !result.transactionId().isBlank()) {
                    deleteTree(root.resolve(".editor-backups").resolve(result.transactionId()));
                }
                loader.reload();
            } catch (IOException ignored) {}
        }
    }

    private static String stageToml() {
        return """
            [schema]
            version = 4

            [stage]
            id = "progressivestages:editor_apply_gametest"
            display_name = "Editor Apply GameTest"
            icon = "minecraft:crafting_table"
            """;
    }

    private static String rulesToml() {
        return """
            [recipes]
            locked_items = ["id:minecraft:diamond"]

            [[interactions]]
            type = "item_into_inventory"
            held_item = "id:minecraft:diamond"
            target_kind = "block"
            target = "id:minecraft:furnace"
            effect = "lock"
            priority = 100
            """;
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
}
