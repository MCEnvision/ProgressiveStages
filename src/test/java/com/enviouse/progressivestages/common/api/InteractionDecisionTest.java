package com.enviouse.progressivestages.common.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InteractionDecisionTest {

    @Test
    void decisionCopiesStageLists() {
        List<StageId> matched = new ArrayList<>(List.of(new StageId("progressivestages", "one")));
        InteractionDecision decision = new InteractionDecision(matched, List.of(),
            InteractionDecision.Reason.STAGE_MISSING, false);

        matched.clear();

        assertEquals(1, decision.matchedStages().size());
        assertThrows(UnsupportedOperationException.class,
            () -> decision.matchedStages().add(new StageId("progressivestages", "two")));
    }
}
