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
        assertTrue(source.contains("private static final AtomicLong clientSessionGeneration"));
        assertTrue(source.contains("if (!initialized || clientDisconnecting.get())"));
        assertTrue(source.contains("long refreshGeneration = clientSessionGeneration.get();"));
        assertTrue(source.contains("if (!isCurrentSession(refreshGeneration, minecraft)) return;"));
        assertTrue(source.contains("clientSessionGeneration.incrementAndGet();"));
        assertTrue(source.contains("clearPendingForCurrentSession(refreshGeneration);"));
        assertTrue(source.contains("minecraft.level != null && minecraft.player != null"));
        int disconnectBridge = eventHandler.indexOf("EmiLifecycleBridge.beginClientDisconnect()");
        int cacheClear = eventHandler.indexOf("ClientStageCache.clear()");
        assertTrue(disconnectBridge >= 0);
        assertTrue(cacheClear >= 0);
        assertTrue(disconnectBridge < cacheClear);
        assertTrue(eventHandler.contains("EmiLifecycleBridge.endClientDisconnect()"));
        assertTrue(!eventHandler.contains("client.emi.ProgressiveStagesEMIPlugin"));
        assertTrue(bridge.contains("ModList.get().isLoaded(\"emi\")"));
        assertTrue(bridge.contains("Class.forName(EMI_PLUGIN)"));
        assertTrue(bridge.contains("ReflectiveOperationException | LinkageError"));
    }
}
