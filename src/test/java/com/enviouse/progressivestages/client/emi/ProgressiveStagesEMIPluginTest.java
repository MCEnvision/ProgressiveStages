package com.enviouse.progressivestages.client.emi;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressiveStagesEMIPluginTest {

    @Test
    void doesNotStartOrRunARefreshAfterTheClientWorldCloses() throws Exception {
        String source = Files.readString(Path.of(System.getProperty("progressivestages.projectDir"),
            "src/main/java/com/enviouse/progressivestages/client/emi/ProgressiveStagesEMIPlugin.java"));
        String eventHandler = Files.readString(Path.of(System.getProperty("progressivestages.projectDir"),
            "src/main/java/com/enviouse/progressivestages/client/ClientEventHandler.java"));
        String bridge = Files.readString(Path.of(System.getProperty("progressivestages.projectDir"),
            "src/main/java/com/enviouse/progressivestages/client/EmiLifecycleBridge.java"));

        assertTrue(source.contains("private static final AtomicBoolean clientDisconnecting"));
        assertTrue(source.contains("if (!initialized || clientDisconnecting.get())"));
        assertTrue(source.contains("if (clientDisconnecting.get() || !isReloadableClient(minecraft)) return;"));
        assertTrue(source.contains("minecraft.level != null && minecraft.player != null"));
        assertTrue(eventHandler.indexOf("EmiLifecycleBridge.beginClientDisconnect()")
            < eventHandler.indexOf("ClientStageCache.clear()"));
        assertTrue(eventHandler.contains("EmiLifecycleBridge.endClientDisconnect()"));
        assertTrue(!eventHandler.contains("client.emi.ProgressiveStagesEMIPlugin"));
        assertTrue(bridge.contains("ModList.get().isLoaded(\"emi\")"));
        assertTrue(bridge.contains("Class.forName(EMI_PLUGIN)"));
        assertTrue(bridge.contains("ReflectiveOperationException | LinkageError"));
    }
}
