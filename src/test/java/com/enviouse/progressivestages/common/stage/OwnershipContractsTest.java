package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.StageId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OwnershipContractsTest {
    private static final StageId STAGE = StageId.parse("progressivestages:chef");

    @Test
    void sourceLabelsUseTheStablePublicKinds() {
        assertEquals(StageSourceKind.INDEPENDENT, StageSourceKind.fromLabel("independent"));
        assertEquals(StageSourceKind.LUCKPERMS_SYNCHRONIZED,
            StageSourceKind.fromLabel("luckperms_synchronized"));
        assertEquals(StageSourceKind.LUCKPERMS_PERMANENT,
            StageSourceKind.fromLabel("luckperms:permanent"));
        assertEquals(StageSourceKind.TEMPORARY, StageSourceKind.fromLabel("temporary"));
    }

    @Test
    void snapshotsAndMutationResultsAreImmutable() {
        UUID actor = UUID.randomUUID();
        Set<StageId> stages = new LinkedHashSet<>(Set.of(STAGE));
        var sources = new LinkedHashMap<StageId, Set<StageSourceKind>>();
        sources.put(STAGE, new LinkedHashSet<>(Set.of(StageSourceKind.INDEPENDENT)));
        EffectiveStageSnapshot snapshot = new EffectiveStageSnapshot(actor, 4L, stages, sources);
        stages.clear();
        sources.get(STAGE).clear();
        assertTrue(snapshot.contains(STAGE));
        assertEquals(Set.of(StageSourceKind.INDEPENDENT), snapshot.sources().get(STAGE));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.stages().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.sources().get(STAGE).clear());

        StageMutationResult result = new StageMutationResult(true, "committed", 4L,
            Set.of(new OwnerRef(OwnerKind.PERSONAL, actor)), Set.of(actor));
        assertThrows(UnsupportedOperationException.class, () -> result.affectedOwners().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.affectedPlayers().clear());
    }

    @Test
    void serverOwnershipOptionsRejectAnOverride() {
        assertEquals(StageOwnershipOptions.Scope.TEAM,
            StageOwnershipOptions.from("team", Optional.of(false)).scope());
        assertThrows(IllegalArgumentException.class,
            () -> new StageOwnershipOptions(StageOwnershipOptions.Scope.SERVER, Optional.of(true)));
    }
}
