package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.server.enforcement.IngredientGateHelper;
import com.enviouse.progressivestages.server.enforcement.ItemEnforcer;
import com.enviouse.progressivestages.server.enforcement.RecipeEnforcer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the normal crafting gates to smithing results.
 * Result clearing is visual. Pickup validation is the authoritative server gate.
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {

    public SmithingMenuMixin() { super(null, 0, null, null); }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void progressivestages$hideLockedResult(CallbackInfo ci) {
        if (!(this.player instanceof ServerPlayer sp)) return;
        ItemStack result = this.resultSlots.getItem(0);
        if (result.isEmpty()) return;

        if (progressivestages$isResultBlocked(sp, result, StageConfig.isHideLockRecipeOutput())) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.broadcastChanges();
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void progressivestages$blockLockedResultPickup(
            Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer sp)) return;
        ItemStack result = this.resultSlots.getItem(0);
        if (result.isEmpty() || !progressivestages$isResultBlocked(sp, result, true)) return;

        cir.setReturnValue(false);
        progressivestages$notifyBlocked(sp, result);
    }

    @Unique
    private boolean progressivestages$isResultBlocked(
            ServerPlayer player, ItemStack result, boolean includeDirectItemLock) {
        if (!StageConfig.isBlockCrafting()) return false;
        if (StageConfig.isAllowCreativeBypass() && player.isCreative()) return false;

        LockRegistry registry = LockRegistry.getInstance();
        if (includeDirectItemLock && registry.isItemBlockedFor(player, result.getItem())) return true;
        if (registry.isRecipeOutputBlockedFor(player, result.getItem())) return true;

        RecipeHolder<?> recipe = this.resultSlots.getRecipeUsed();
        if (recipe != null && registry.isRecipeBlockedFor(player, recipe.id())) return true;

        return IngredientGateHelper.checkContainer(player, this.inputSlots).isPresent();
    }

    @Unique
    private void progressivestages$notifyBlocked(ServerPlayer player, ItemStack result) {
        LockRegistry registry = LockRegistry.getInstance();
        if (registry.isItemBlockedFor(player, result.getItem())) {
            ItemEnforcer.notifyLockedWithCooldown(player, result.getItem());
            return;
        }
        if (registry.isRecipeOutputBlockedFor(player, result.getItem())) {
            RecipeEnforcer.notifyOutputLocked(player, result.getItem());
            return;
        }

        RecipeHolder<?> recipe = this.resultSlots.getRecipeUsed();
        if (recipe != null && registry.isRecipeBlockedFor(player, recipe.id())) {
            RecipeEnforcer.notifyLocked(player, recipe.id());
            return;
        }

        IngredientGateHelper.checkContainer(player, this.inputSlots)
            .ifPresent(blocked -> IngredientGateHelper.notifyIngredientBlocked(player, blocked));
    }
}
