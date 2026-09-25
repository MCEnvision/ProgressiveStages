package com.enviouse.progressivestages.common.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StageGuideNetworkContractTest {
    private static final Path PROJECT = Path.of(System.getProperty("progressivestages.projectDir"));

    @Test
    void guideSyncUsesProtocolThreeAndAtomicChunks() throws IOException {
        String source = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/common/network/NetworkHandler.java"));
        assertTrue(source.contains("event.registrar(\"3\")"));
        assertTrue(source.contains("totalChunks"));
        assertTrue(source.contains("applyStageDefinitionSnapshot"));
        assertTrue(source.contains("MAX_TEXT_BYTES"));
    }
}
