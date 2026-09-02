package com.enviouse.progressivestages.common.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewerSyncContractTest {

    private static final Path PROJECT = Path.of(System.getProperty("progressivestages.projectDir"));

    @Test
    void fullStageSyncIncludesTheAuthoritativeViewerLockSnapshotFirst() throws IOException {
        String source = Files.readString(PROJECT.resolve(
            "src/main/java/com/enviouse/progressivestages/common/network/NetworkHandler.java"));
        int method = source.indexOf("public static void sendStageSync");
        int locks = source.indexOf("sendLockSync(player);", method);
        int snapshot = source.indexOf("new StageSyncPayload(stageList)", method);

        assertTrue(method >= 0);
        assertTrue(locks > method);
        assertTrue(snapshot > locks);
    }
}
