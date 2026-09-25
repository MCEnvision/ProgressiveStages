package com.enviouse.progressivestages.server.loader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import com.enviouse.progressivestages.common.lock.PrefixEntry;

final class EditorConfigParityTest {
    @Test
    void parsesTheEditorProducedBlockOverrideFixture() throws Exception {
        try (var fixture = getClass().getResourceAsStream("/block-overrides.toml")) {
            assertNotNull(fixture, "The editor produced fixture must be on the test classpath.");

            var parsed = StageFileParser.parseText(new String(fixture.readAllBytes(), StandardCharsets.UTF_8),
                "challenge4.toml", "test", false);
            assertTrue(parsed.isSuccess(), parsed.getErrorMessage());
            var overrides = parsed.getStageDefinition().getLocks().blockOverrides();
            assertEquals(2, overrides.size());
            assertEquals("blocks.overrides", overrides.get(0).sourceTable());
            assertEquals("blocks.overrides[0].target", overrides.get(0).sourceField());
            assertEquals("minecraft:diamond_ore", overrides.get(0).targets().getFirst().id().toString());
            assertEquals("minecraft:stone", overrides.get(0).displayAs().toString());
            assertEquals("minecraft:cobblestone", overrides.get(0).dropAs().toString());
            assertEquals(0, overrides.get(0).priority());

            assertEquals("ores.overrides", overrides.get(1).sourceTable());
            assertEquals("ores.overrides[0].targets", overrides.get(1).sourceField());
            assertEquals(2, overrides.get(1).targets().size());
            assertEquals(PrefixEntry.Kind.TAG, overrides.get(1).targets().get(0).kind());
            assertEquals("c:ores", overrides.get(1).targets().get(0).id().toString());
            assertEquals(PrefixEntry.Kind.MOD, overrides.get(1).targets().get(1).kind());
            assertEquals("immersiveengineering", overrides.get(1).targets().get(1).value());
            assertEquals(7, overrides.get(1).priority());
        }
    }

    @Test
    void rejectsTheIgnoredOresLockedCategoryWithAUsefulError() {
        var parsed = StageFileParser.parseText("[stage]\nid = \"challenge4\"\n[ores]\nlocked = [\"minecraft:diamond_ore\"]\n",
            "challenge4.toml", "test", false);
        assertFalse(parsed.isSuccess());
        assertTrue(parsed.getErrorMessage().contains("[ores].locked"));
        assertTrue(parsed.getErrorMessage().contains("[[blocks.overrides]]"));
    }

    @Test
    void reportsTheExactFieldForAnInvalidScalarTarget() {
        var parsed = StageFileParser.parseText("[stage]\nid = \"challenge4\"\n"
                + "[[blocks.overrides]]\ntarget = \"name:ore\"\n"
                + "display_as = \"minecraft:stone\"\ndrop_as = \"minecraft:cobblestone\"\n",
            "challenge4.toml", "test", false);
        assertFalse(parsed.isSuccess());
        assertTrue(parsed.getErrorMessage().contains("blocks.overrides[0].target must be"));
        assertFalse(parsed.getErrorMessage().contains("blocks.overrides[0].target]"));
    }
}
