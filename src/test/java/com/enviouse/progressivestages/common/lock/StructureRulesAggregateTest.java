package com.enviouse.progressivestages.common.lock;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.api.structure.StructureAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureRulesAggregateTest {

    @Test
    void keepsEntryAndProtectionContributionsIndependent() {
        var structure = net.minecraft.resources.ResourceLocation.parse("minecraft:stronghold");
        var entryStage = StageId.parse("test:entry");
        var protectionStage = StageId.parse("test:protection");
        var entryRules = new LockDefinition.StructureRules(
            CategoryLocks.builder().addLocked(List.of("id:minecraft:stronghold|priority=7")).build(),
            false, false, false, false, 2, false, null);
        var protectionRules = new LockDefinition.StructureRules(
            CategoryLocks.builder().addLocked(List.of("minecraft:stronghold")).build(),
            false, true, true, true, 0, true, 4);

        var aggregate = LockRegistry.StructureRulesAggregate.EMPTY
            .merge(entryRules, entryStage, "entry.toml#structures.rules", 1)
            .merge(protectionRules, protectionStage, "protection.toml#structures.rules", 2);

        assertEquals(4, aggregate.contributions.get(structure).size());
        assertEquals(1, aggregate.forAction(structure, StructureAction.ENTRY).size());
        assertEquals(1, aggregate.forAction(structure, StructureAction.BLOCK_PLACE).size());
        assertEquals(1, aggregate.forAction(structure, StructureAction.ACTORLESS_EXPLOSION).size());
        assertEquals(1, aggregate.forAction(structure, StructureAction.ACTORLESS_SPAWN).size());
        assertEquals(7, aggregate.forAction(structure, StructureAction.ENTRY).getFirst().priority());
        assertEquals(4, aggregate.forAction(structure, StructureAction.BLOCK_PLACE).getFirst().priority());
        assertTrue(aggregate.lockedEntry.get(structure).contains(entryStage));
        assertFalse(aggregate.lockedEntry.get(structure).contains(protectionStage));
        assertTrue(aggregate.preventBlockPlace);
        assertTrue(aggregate.preventExplosions);
        assertTrue(aggregate.disableMobSpawning);
        assertEquals(2, aggregate.entryPadding);
    }
}
