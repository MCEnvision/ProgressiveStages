package com.enviouse.progressivestages.server.enforcement;

import org.junit.jupiter.api.Test;

import java.util.Set;

import com.enviouse.progressivestages.common.api.StageId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbilityEnforcerTest {
    @Test
    void everyDocumentedAbilityHasAuthoritativeEnforcement() {
        assertEquals(Set.of("jump", "elytra", "sprint", "swim", "climb"),
            AbilityEnforcer.ENFORCED_ABILITIES);
    }

    @Test
    void onlyChangedAbilityStatesAreReported() {
        assertEquals(Set.of("elytra", "sprint"),
            AbilityEnforcer.changedAbilities(Set.of("jump", "sprint"), Set.of("jump", "elytra")));
        assertEquals(AbilityEnforcer.ENFORCED_ABILITIES,
            AbilityEnforcer.changedAbilities(Set.of("jump"), null));
    }

    @Test
    void gateSourceAndMissingStagesAreRetained() {
        var stage = StageId.parse("test:jump");
        var gate = new AbilityEnforcer.AbilityGate(true, "stage:test:jump", Set.of(stage));

        assertEquals("stage:test:jump", gate.source());
        assertEquals(Set.of(stage), gate.missingStages());
    }
}
