package com.enviouse.progressivestages.server.enforcement;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionEnforcerTest {

    @Test
    void itemSelectorsAcceptModernAndLegacyExactForms() {
        ItemStack stick = new ItemStack(Items.STICK);

        assertTrue(InteractionEnforcer.itemMatches(stick, "all:*"));
        assertTrue(InteractionEnforcer.itemMatches(stick, "id:minecraft:stick"));
        assertTrue(InteractionEnforcer.itemMatches(stick, "minecraft:stick"));
        assertFalse(InteractionEnforcer.itemMatches(stick, "id:minecraft:diamond"));
    }

    @Test
    void blockSelectorsAcceptModernAndLegacyExactForms() {
        assertTrue(InteractionEnforcer.blockMatches(Blocks.CRAFTING_TABLE, "all:*"));
        assertTrue(InteractionEnforcer.blockMatches(Blocks.CRAFTING_TABLE, "id:minecraft:crafting_table"));
        assertTrue(InteractionEnforcer.blockMatches(Blocks.CRAFTING_TABLE, "minecraft:crafting_table"));
        assertFalse(InteractionEnforcer.blockMatches(Blocks.CRAFTING_TABLE, "id:minecraft:anvil"));
    }
}
