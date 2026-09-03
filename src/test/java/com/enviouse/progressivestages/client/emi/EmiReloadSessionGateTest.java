package com.enviouse.progressivestages.client.emi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmiReloadSessionGateTest {

    @Test
    void aStaleSessionCannotReplaceOrReleaseTheCurrentSessionSlot() {
        EmiReloadSessionGate gate = new EmiReloadSessionGate();

        assertTrue(gate.claim(0, 0, false));
        assertTrue(gate.claim(1, 1, false));
        assertFalse(gate.claim(0, 1, false));
        gate.release(0);

        assertEquals(1, gate.pendingGeneration());
        gate.release(1);
        assertEquals(EmiReloadSessionGate.NO_PENDING_GENERATION, gate.pendingGeneration());
    }

    @Test
    void aDisconnectingOrDuplicateSessionCannotClaimTheSlot() {
        EmiReloadSessionGate gate = new EmiReloadSessionGate();

        assertFalse(gate.claim(2, 2, true));
        assertTrue(gate.claim(2, 2, false));
        assertFalse(gate.claim(2, 2, false));
    }
}
