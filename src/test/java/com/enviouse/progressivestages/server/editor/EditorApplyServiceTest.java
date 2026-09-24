package com.enviouse.progressivestages.server.editor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorApplyServiceTest {
    @TempDir
    Path root;

    @Test
    void distinguishesRevisionDriftFromLiveFileChanges() throws Exception {
        Path stage = root.resolve("stages/wizard_warlock/stage.toml");
        Files.createDirectories(stage.getParent());
        Files.writeString(root.resolve("progressivestages.toml"), "[general]\n");
        Files.writeString(stage, "[stage]\nid = \"wizard:warlock\"\n");
        Map<String, String> base = Map.of(
            "progressivestages.toml", "[general]\n",
            "stages/wizard_warlock/stage.toml", "[stage]\nid = \"wizard:warlock\"\n");
        EditorApplyService service = new EditorApplyService(root);

        assertTrue(service.liveFilesMatch(base));

        Files.writeString(stage, "[stage]\nid = \"wizard:wizard\"\n");
        assertFalse(service.liveFilesMatch(base));
    }

    @Test
    void validatesMainSettingsAgainstTheLoadedSpec() {
        EditorDraftValidator.MainConfigValidation valid = EditorDraftValidator.validateMainConfig(
            "[enforcement]\nblock_structure_entry = false\nregion_tick_frequency = 20\n");
        assertTrue(valid.valid(), () -> String.join(". ", valid.errors()));

        EditorDraftValidator.MainConfigValidation invalidRange = EditorDraftValidator.validateMainConfig(
            "[enforcement]\nregion_tick_frequency = 0\n");
        assertFalse(invalidRange.valid());
        assertTrue(invalidRange.errors().stream().anyMatch(error -> error.contains("region_tick_frequency")));

        EditorDraftValidator.MainConfigValidation invalidKey = EditorDraftValidator.validateMainConfig(
            "[general]\nmade_up_setting = true\n");
        assertFalse(invalidKey.valid());
        assertTrue(invalidKey.errors().stream().anyMatch(error -> error.contains("made_up_setting")));
    }
}
