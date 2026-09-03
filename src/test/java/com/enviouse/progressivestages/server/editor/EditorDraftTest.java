package com.enviouse.progressivestages.server.editor;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorDraftTest {
    @Test
    void serializesMutationsAndSupportsSemanticUndoRedo() {
        UUID owner = UUID.randomUUID();
        EditorDraft draft = new EditorDraft(UUID.randomUUID(), owner, 5, 8,
            Map.of("stages/test/stage.toml", "old"));
        assertEquals(1, draft.mutate(owner, 0, "stages/test/stage.toml", "new"));
        assertEquals("new", draft.files().get("stages/test/stage.toml"));
        assertEquals(DraftDiffEntry.ChangeType.MODIFIED, draft.diff().getFirst().change());
        assertEquals(2, draft.undo(owner, 1));
        assertEquals("old", draft.files().get("stages/test/stage.toml"));
        assertEquals(3, draft.redo(owner, 2));
        assertEquals("new", draft.files().get("stages/test/stage.toml"));
        assertThrows(EditorDraft.DraftConflictException.class,
            () -> draft.mutate(owner, 1, "stages/test/rules.toml", "rules"));
        assertThrows(SecurityException.class,
            () -> draft.mutate(UUID.randomUUID(), 3, "stages/test/rules.toml", "rules"));
    }

    @Test
    void rejectsEscapingAndNonTomlPaths() {
        UUID owner = UUID.randomUUID();
        EditorDraft draft = new EditorDraft(UUID.randomUUID(), owner, 0, 0, Map.of());
        assertThrows(IllegalArgumentException.class, () -> draft.mutate(owner, 0, "../secret.toml", "bad"));
        assertThrows(IllegalArgumentException.class, () -> draft.mutate(owner, 0, "stages/test/script.js", "bad"));
    }

    @Test
    void appliedDraftBecomesTheNewCleanBase() {
        UUID owner = UUID.randomUUID();
        EditorDraft draft = new EditorDraft(UUID.randomUUID(), owner, 5, 8,
            Map.of("stages/test/stage.toml", "old"));
        draft.mutate(owner, 0, "stages/test/stage.toml", "new");

        draft.acceptApplied(6);

        assertEquals(6, draft.baseConfigurationRevision());
        assertEquals("new", draft.baseFiles().get("stages/test/stage.toml"));
        assertTrue(draft.diff().isEmpty());
        assertTrue(!draft.canUndo());
    }

    @Test
    void migrationChangesTheDraftWithoutCreatingAnUndoStep() {
        UUID owner = UUID.randomUUID();
        EditorDraft draft = new EditorDraft(UUID.randomUUID(), owner, 5, 8,
            Map.of("stages/test/rules.toml", "legacy"));

        assertTrue(draft.migrate(owner, "stages/test/rules.toml", "canonical"));

        assertEquals(1, draft.revision());
        assertEquals("canonical", draft.files().get("stages/test/rules.toml"));
        assertEquals(DraftDiffEntry.ChangeType.MODIFIED, draft.diff().getFirst().change());
        assertTrue(!draft.canUndo());
    }

    @Test
    void sessionNormalizesRecognizableLegacyRecipeRulesBeforeValidation() {
        UUID owner = UUID.randomUUID();
        String legacyRules = """
            [[rules]]
            id = "showcase:aquatic_blessing/rule_mtjmf0xs"
            effect = "lock"
            priority = 100
            action = "craft"
            targets.recipes = ["minecraft:diamond_sword"]
            """;
        EditorDraft draft = new EditorDraft(UUID.randomUUID(), owner, 5, 8, Map.of(
            "stages/showcase_aquatic_blessing/stage.toml", "[schema]\nversion = 4\n[stage]\nid = \"showcase:aquatic_blessing\"\n",
            "stages/showcase_aquatic_blessing/rules.toml", legacyRules));

        assertTrue(EditorSessionService.normalizeLegacyRecipeRules(owner, draft));
        assertTrue(EditorDraftValidator.validate(draft.files(), draft.revision()).valid());
        assertTrue(draft.files().get("stages/showcase_aquatic_blessing/rules.toml")
            .contains("locked_items = [\"minecraft:diamond_sword\"]"));
    }

    @Test
    void validatesACompleteThreeFileDraft() {
        Map<String, String> files = Map.of(
            "stages/test/stage.toml", "[schema]\nversion = 4\n[stage]\nid = \"test:editor\"\ndisplay_name = \"Editor\"\n",
            "stages/test/rules.toml", "[items]\nlocked = [\"minecraft:diamond\"]\n",
            "stages/test/progression.toml", "# Progression may be empty.\n");
        DraftValidation validation = EditorDraftValidator.validate(files, 3);
        assertTrue(validation.valid(), String.join(". ", validation.errors()));
        assertEquals(1, validation.stages());
    }

    @Test
    void rejectsTheLegacyGenericRecipeRuleBeforeAnyDraftCanApply() {
        Map<String, String> files = Map.of(
            "stages/test/stage.toml", "[schema]\nversion = 4\n[stage]\nid = \"test:editor\"\ndisplay_name = \"Editor\"\n",
            "stages/test/rules.toml", "[[rules]]\naction = \"craft\"\ntargets.recipes = [\"minecraft:diamond_sword\"]\n",
            "stages/test/progression.toml", "# Progression may be empty.\n");

        DraftValidation validation = EditorDraftValidator.validate(files, 3);

        assertTrue(!validation.valid());
        assertTrue(validation.errors().stream().anyMatch(error -> error.contains("[recipes].locked_items")),
            String.join(". ", validation.errors()));
    }

    @Test
    void rejectsTheAmbiguousRecipesLockedAliasBeforeAnyDraftCanApply() {
        Map<String, String> files = Map.of(
            "stages/test/stage.toml", "[schema]\nversion = 4\n[stage]\nid = \"test:editor\"\ndisplay_name = \"Editor\"\n",
            "stages/test/rules.toml", "[recipes]\nlocked = [\"minecraft:diamond_sword\"]\n",
            "stages/test/progression.toml", "# Progression may be empty.\n");

        DraftValidation validation = EditorDraftValidator.validate(files, 3);

        assertTrue(!validation.valid());
        assertTrue(validation.errors().stream().anyMatch(error -> error.contains("[recipes].locked is ambiguous")),
            String.join(". ", validation.errors()));
    }

    @Test
    void rejectsAnExactInventoryTargetThatTheEditorCannotResolve() {
        Map<String, String> files = Map.of(
            "stages/test/stage.toml", "[schema]\nversion = 4\n[stage]\nid = \"test:editor\"\ndisplay_name = \"Editor\"\n",
            "stages/test/rules.toml", """
                [[interactions]]
                type = "item_into_inventory"
                held_item = "id:minecraft:diamond"
                target_kind = "inventory"
                target = "id:test:missing_inventory"
                effect = "lock"
                priority = 100
                """,
            "stages/test/progression.toml", "# Progression may be empty.\n");

        DraftValidation validation = EditorDraftValidator.validate(files, 3);

        assertTrue(!validation.valid());
        assertTrue(validation.errors().stream().anyMatch(error -> error.contains("registered inventory target")),
            String.join(". ", validation.errors()));
    }
}
