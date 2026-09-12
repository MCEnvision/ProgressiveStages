package com.enviouse.progressivestages.server.enforcement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InteractionCaptureManagerTest {

    @AfterEach
    void reset() {
        InteractionCaptureManager.resetRuntimeState();
    }

    @Test
    void inactiveStatusIsSafeAndStopIsIdempotent() {
        InteractionCaptureManager.resetRuntimeState();

        InteractionCaptureManager.CaptureStatus status = InteractionCaptureManager.status();
        assertFalse(status.active());
        assertEquals("off", status.stopReason());
        assertFalse(InteractionCaptureManager.stop(InteractionCaptureManager.StopReason.MANUAL));
    }

    @Test
    void invalidStartDoesNotCreateCapture() {
        InteractionCaptureManager.StartResult result =
            InteractionCaptureManager.start(null, null);

        assertTrue(result.invalidTarget());
        assertFalse(result.started());
        assertFalse(InteractionCaptureManager.status().active());
    }

    @Test
    void budgetsAndStopReasonsRemainFixed() {
        assertEquals(60, InteractionCaptureManager.MAX_SECONDS);
        assertEquals(200, InteractionCaptureManager.MAX_DECISIONS);
        assertEquals(20, InteractionCaptureManager.MAX_DECISIONS_PER_SECOND);
        assertEquals(128 * 1024, InteractionCaptureManager.MAX_OUTPUT_BYTES);
        assertEquals(256, InteractionCaptureManager.MAX_QUEUE);
        assertEquals(256, InteractionCaptureManager.MAX_STRING_LENGTH);
        assertEquals(32, InteractionCaptureManager.MAX_COLLECTION_LENGTH);
        assertEquals("output_error", InteractionCaptureManager.StopReason.OUTPUT_ERROR.value());
        assertEquals("target_removed", InteractionCaptureManager.StopReason.TARGET_REMOVED.value());
    }
}
