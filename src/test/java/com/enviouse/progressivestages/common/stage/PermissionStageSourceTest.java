package com.enviouse.progressivestages.common.stage;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionStageSourceTest {
    private static final UUID FIRST = new UUID(0L, 1L);
    private static final UUID SECOND = new UUID(0L, 2L);

    @Test
    void subjectRowAndRetentionRemainDistinctAndRoundtrip() {
        var source = new PermissionStageSource(FIRST, "chef.rank", false);
        assertEquals(source, PermissionStageSource.parse(source.label()).orElseThrow());
        assertNotEquals(source.label(), new PermissionStageSource(SECOND, "chef.rank", false).label());
        assertNotEquals(source.label(), new PermissionStageSource(FIRST, "chef.other", false).label());
        var permanent = new PermissionStageSource(FIRST, "chef.rank", true);
        assertNotEquals(source.label(), permanent.label());
        assertEquals(permanent, PermissionStageSource.parse(permanent.label()).orElseThrow());
    }

    @Test
    void legacyOrMalformedSourcesCannotBeAssignedToAnArbitrarySubject() {
        for (String label : new String[] {"independent", "luckperms:synchronized:chef",
                "luckperms:permanent:chef", "luckperms:synchronized:0-0-0-0-1:chef",
                "luckperms:synchronized:" + FIRST + ":", "luckperms:synchronized:" + FIRST + ":chef:other",
                "luckperms:unknown:" + FIRST + ":chef"}) {
            assertTrue(PermissionStageSource.parse(label).isEmpty(), label);
        }
        assertTrue(PermissionStageSource.parse(null).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new PermissionStageSource(FIRST, "row:other", false));
        assertThrows(IllegalArgumentException.class, () -> new PermissionStageSource(FIRST, "x".repeat(65), false));
    }

    @Test
    void attributedAndLegacyRowLabelsNeverBecomeIndependentKinds() {
        for (String label : new String[] {"luckperms:synchronized:chef",
                new PermissionStageSource(FIRST, "chef", false).label()}) {
            assertEquals(StageSourceKind.LUCKPERMS_SYNCHRONIZED, StageSourceKind.fromLabel(label));
        }
        for (String label : new String[] {"luckperms:permanent:chef",
                new PermissionStageSource(FIRST, "chef", true).label()}) {
            assertEquals(StageSourceKind.LUCKPERMS_PERMANENT, StageSourceKind.fromLabel(label));
        }
    }
}
