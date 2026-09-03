package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.rehaul.selector.SelectorTarget;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryInsertionEnforcerTest {

    private static final StageId MINER = new StageId("test", "miner");
    private static final StageId PERMIT = new StageId("test", "permit");
    private static final SelectorTarget ORE = target("minecraft:iron_ore", "c:ores");
    private static final SelectorTarget BIN = target("example:selling_bin", "example:selling_bins");

    @Test
    void matchesPairedSelectorsAcrossEverySupportedPrefix() {
        for (String source : List.of("all:*", "id:minecraft:iron_ore", "mod:minecraft", "tag:c:ores", "#c:ores", "name:iron")) {
            for (String destination : List.of("all:*", "id:example:selling_bin", "mod:example", "tag:example:selling_bins", "#example:selling_bins", "name:selling")) {
                var decision = InventoryInsertionEnforcer.resolve(List.of(rule(source, destination, "lock", 100, MINER)),
                    stage -> false, ORE, "block", BIN);
                assertTrue(decision.isPresent(), source + " and " + destination);
                assertTrue(decision.get().denied(), source + " and " + destination);
            }
        }
    }

    @Test
    void highPriorityOwnedAllowDefeatsBroadMissingLock() {
        var decision = InventoryInsertionEnforcer.resolve(List.of(
                rule("all:*", "all:*", "lock", 100, MINER),
                rule("id:minecraft:iron_ore", "id:example:selling_bin", "allow", 200, PERMIT)),
            stage -> stage.equals(PERMIT), ORE, "block", BIN);

        assertTrue(decision.isPresent());
        assertEquals("allow", decision.get().effect());
        assertFalse(decision.get().denied());
    }

    @Test
    void highPriorityExcludeDefeatsBroadMissingLock() {
        var decision = InventoryInsertionEnforcer.resolve(List.of(
                rule("all:*", "all:*", "lock", 100, MINER),
                rule("id:minecraft:iron_ore", "id:example:selling_bin", "exclude", 200, PERMIT)),
            stage -> false, ORE, "block", BIN);

        assertTrue(decision.isPresent());
        assertEquals("exclude", decision.get().effect());
        assertFalse(decision.get().denied());
    }

    @Test
    void denyWinsAConflictingPriorityTie() {
        var decision = InventoryInsertionEnforcer.resolve(List.of(
                rule("all:*", "all:*", "allow", 100, PERMIT),
                rule("all:*", "all:*", "deny", 100, MINER)),
            stage -> stage.equals(PERMIT), ORE, "block", BIN);

        assertTrue(decision.isPresent());
        assertEquals("deny", decision.get().effect());
        assertTrue(decision.get().denied());
    }

    @Test
    void explainReportsBothSelectorsAndTheBoundedWinningDecision() {
        var trace = InventoryInsertionEnforcer.explain(List.of(
                rule("all:*", "all:*", "lock", 100, MINER),
                rule("id:minecraft:iron_ore", "id:example:selling_bin", "allow", 200, PERMIT)),
            stage -> stage.equals(PERMIT), ORE, "block", BIN);

        assertTrue(trace.isPresent());
        assertEquals(ORE.id(), trace.get().source());
        assertEquals(BIN, trace.get().destinations().get("block"));
        assertEquals(2, trace.get().matches().size());
        assertTrue(trace.get().winner().isPresent());
        assertEquals(PERMIT, trace.get().winner().get().stage());
        assertEquals("allow", trace.get().winner().get().effect());
    }

    @Test
    void mismatchedTargetKindDoesNotBlockTheInsertion() {
        var decision = InventoryInsertionEnforcer.resolve(List.of(
                rule("all:*", "all:*", "deny", 100, MINER)),
            stage -> false, ORE, Map.of("menu", BIN));

        assertTrue(decision.isEmpty());
    }

    @Test
    void bothTheItemAndTheDestinationMustMatchTheSameRule() {
        var entries = List.of(rule("id:minecraft:iron_ore", "id:example:selling_bin", "deny", 100, MINER));
        SelectorTarget dirt = target("minecraft:dirt", "minecraft:dirt");
        SelectorTarget chest = target("minecraft:chest", "minecraft:chests");

        assertTrue(InventoryInsertionEnforcer.resolve(entries, stage -> false, dirt, "block", BIN).isEmpty());
        assertTrue(InventoryInsertionEnforcer.resolve(entries, stage -> false, ORE, "block", chest).isEmpty());
    }

    private static LockRegistry.InteractionLockEntry rule(String source, String destination, String effect,
                                                           int priority, StageId stage) {
        return new LockRegistry.InteractionLockEntry(InventoryInsertionEnforcer.TYPE, source, destination,
            "block", effect, priority, "", stage);
    }

    private static SelectorTarget target(String id, String tag) {
        return new SelectorTarget(ResourceLocation.parse(id), null, Set.of(ResourceLocation.parse(tag)), Map.of());
    }
}
