package com.enviouse.progressivestages.server.enforcement;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmithingMenuMixinTest {

    private static final Path SOURCE = Path.of(System.getProperty("progressivestages.projectDir"))
        .resolve("src/main/java/com/enviouse/progressivestages/mixin/SmithingMenuMixin.java");

    @Test
    void smithingMenuMixinAppliesToTheVanillaMenu() throws ClassNotFoundException {
        assertNotNull(Class.forName("net.minecraft.world.inventory.SmithingMenu"));
    }

    @Test
    void smithingUsesEveryCraftingGateAndRejectsPickupOnTheServer() throws IOException {
        String source = Files.readString(SOURCE);

        assertTrue(source.contains("@Inject(method = \"mayPickup\", at = @At(\"HEAD\"), cancellable = true)"));
        assertTrue(source.contains("StageConfig.isBlockCrafting()"));
        assertTrue(source.contains("StageConfig.isHideLockRecipeOutput()"));
        assertTrue(source.contains("registry.isItemBlockedFor(player, result.getItem())"));
        assertTrue(source.contains("registry.isRecipeOutputBlockedFor(player, result.getItem())"));
        assertTrue(source.contains("this.resultSlots.getRecipeUsed()"));
        assertTrue(source.contains("registry.isRecipeBlockedFor(player, recipe.id())"));
        assertTrue(source.contains("IngredientGateHelper.checkContainer(player, this.inputSlots)"));
        assertTrue(source.contains("cir.setReturnValue(false)"));
        assertTrue(source.contains("this.resultSlots.setItem(0, ItemStack.EMPTY)"));
        assertTrue(source.contains("this.broadcastChanges()"));
        assertFalse(source.contains("isIngredientGatingActive()) return"));
    }
}
