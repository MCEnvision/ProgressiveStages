package com.enviouse.progressivestages.common.stage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StageMutationGuardTest {
    @Test
    void lifecycleChangesInvalidateGuardsEvenWhenTheRevisionIsReset() throws Exception {
        var constructor = StageManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var manager = constructor.newInstance();
        var initial = manager.mutationGuard();
        manager.initialize(null);
        assertFalse(initial.getAsBoolean());
        var initialized = manager.mutationGuard();
        assertTrue(initialized.getAsBoolean());
        manager.shutdown(null);
        assertFalse(initialized.getAsBoolean());
        assertEquals(0, manager.getMutationRevision());
        var stopped = manager.mutationGuard();
        manager.initialize(null);
        assertFalse(stopped.getAsBoolean());
        assertTrue(manager.mutationGuard().getAsBoolean());
    }
}
