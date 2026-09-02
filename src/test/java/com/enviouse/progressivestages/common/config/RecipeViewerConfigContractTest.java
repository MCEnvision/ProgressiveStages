package com.enviouse.progressivestages.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeViewerConfigContractTest {

    private static final Path PROJECT = Path.of(System.getProperty("progressivestages.projectDir"));

    @Test
    void recipeViewerIntegrationsHaveIndependentEnabledDefaults() throws IOException {
        String source = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/common/config/StageConfig.java"));

        assertTrue(source.contains(".define(\"jei.enabled\", true)"));
        assertTrue(source.contains(".define(\"emi.enabled\", true)"));
        assertTrue(source.contains("jeiEnabled = JEI_ENABLED.get()"));
        assertTrue(source.contains("emiEnabled = EMI_ENABLED.get()"));
        assertTrue(source.contains("isJeiEnabled() { return jeiEnabled; }"));
        assertTrue(source.contains("isEmiEnabled() { return emiEnabled; }"));
    }

    @Test
    void configSpecDefaultsMissingViewerSettingsAndCorrectsOnlyTheMalformedOne() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set(List.of("jei", "enabled"), "not-a-boolean");
        config.set(List.of("emi", "enabled"), false);

        StageConfig.SPEC.correct(config);

        assertEquals(Boolean.TRUE, config.get(List.of("jei", "enabled")));
        assertEquals(Boolean.FALSE, config.get(List.of("emi", "enabled")));

        CommentedConfig missing = CommentedConfig.inMemory();
        StageConfig.SPEC.correct(missing);

        assertEquals(Boolean.TRUE, missing.get(List.of("jei", "enabled")));
        assertEquals(Boolean.TRUE, missing.get(List.of("emi", "enabled")));

        CommentedConfig explicit = CommentedConfig.inMemory();
        explicit.set(List.of("jei", "enabled"), true);
        explicit.set(List.of("emi", "enabled"), false);
        StageConfig.SPEC.correct(explicit);

        assertEquals(Boolean.TRUE, explicit.get(List.of("jei", "enabled")));
        assertEquals(Boolean.FALSE, explicit.get(List.of("emi", "enabled")));
    }

    @Test
    void eachViewerAndRefreshPathHonorsItsOwnEnabledSetting() throws IOException {
        String jei = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/client/jei/ProgressiveStagesJEIPlugin.java"));
        String emi = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/client/emi/ProgressiveStagesEMIPlugin.java"));
        String lockCache = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/client/ClientLockCache.java"));
        String stageCache = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/client/ClientStageCache.java"));
        String emiStackWidget = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/mixin/client/EmiStackWidgetMixin.java"));
        String emiScreenManager = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/mixin/client/EmiScreenManagerMixin.java"));

        assertTrue(jei.contains("if (!StageConfig.isJeiEnabled())"));
        assertTrue(emi.contains("if (!StageConfig.isEmiEnabled())"));
        assertTrue(lockCache.contains("if (StageConfig.isJeiEnabled())"));
        assertTrue(lockCache.contains("if (StageConfig.isEmiEnabled())"));
        assertTrue(stageCache.contains("if (StageConfig.isJeiEnabled())"));
        assertTrue(stageCache.contains("if (StageConfig.isEmiEnabled())"));
        assertTrue(emiStackWidget.contains("if (!StageConfig.isEmiEnabled())"));
        assertTrue(emiScreenManager.contains("if (!StageConfig.isEmiEnabled())"));
    }
}
