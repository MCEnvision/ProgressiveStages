package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.server.enforcement.ItemEnforcer;
import com.enviouse.progressivestages.server.enforcement.InventoryInsertionEnforcer;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;

/**
 * Mixin to prevent moving locked items in containers.
 *
 * <p>Enforcement priority (highest wins):
 * <ul>
 *   <li>{@code block_item_inventory = true} → block ALL mouse interaction with locked items (strictest)</li>
 *   <li>{@code block_item_mouse_pickup = true} → block picking up locked items with mouse cursor</li>
 *   <li>{@code block_item_hotbar = true} → block placing locked items into hotbar slots (slots 0-8)</li>
 *   <li>All false → free movement (items can be moved to chests, between slots)</li>
 * </ul>
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow @Final public NonNullList<Slot> slots;
    @Shadow @Final private Set<Slot> quickcraftSlots;

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void progressivestages$blockLockedItemMove(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (StageConfig.isAllowCreativeBypass() && player.isCreative()) {
            return;
        }

        Optional<InventoryInsertionEnforcer.Decision> insertion =
            progressivestages$deniedInventoryInsertion(slotId, button, clickType, serverPlayer);
        if (insertion.isPresent()) {
            ci.cancel();
            ((AbstractContainerMenu) (Object) this).broadcastChanges();
            ItemEnforcer.notifyLocked(serverPlayer, insertion.get().stage(),
                StageConfig.getMsgTypeLabelInteraction());
            return;
        }

        // Need at least one enforcement option enabled
        LockRegistry registry = LockRegistry.getInstance();
        if (!StageConfig.isBlockItemInventory() && !StageConfig.isBlockItemMousePickup()
                && !StageConfig.isBlockItemHotbar() && !registry.hasEnforcementOverrides()) {
            return;
        }

        try {
            if (slotId < 0 || slotId >= slots.size()) {
                return;
            }

            Slot slot = slots.get(slotId);
            if (slot == null || !slot.hasItem()) {
                return;
            }

            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                return;
            }

            // v2.0 multi-stage: blocked iff ANY gating stage is missing.
            java.util.Set<com.enviouse.progressivestages.common.api.StageId> missing =
                registry.missingStagesForItem(serverPlayer, stack.getItem());
            if (missing.isEmpty()) {
                // Item is not locked for this player — but check hotbar destination restriction
                progressivestages$checkHotbarDestination(slotId, clickType, serverPlayer, ci);
                return;
            }

            // Item IS locked for this player — apply enforcement based on config

            // Strictest: block_item_inventory blocks ALL interaction
            if (registry.isCategoryEnforced(missing,
                    com.enviouse.progressivestages.common.lock.EnforcementCategory.ITEM_INVENTORY)) {
                if (!registry.isExemptFromInventory(stack.getItem(), missing)) {
                    ci.cancel();
                    ItemEnforcer.notifyLockedWithCooldown(serverPlayer, stack.getItem());
                    return;
                }
            }

            // Medium: block_item_mouse_pickup blocks picking up the locked item with mouse
            if (registry.isCategoryEnforced(missing,
                    com.enviouse.progressivestages.common.lock.EnforcementCategory.ITEM_MOUSE_PICKUP)) {
                if (!registry.isExemptFromMousePickup(stack.getItem(), missing)) {
                    ci.cancel();
                    ItemEnforcer.notifyLockedWithCooldown(serverPlayer, stack.getItem());
                    return;
                }
            }

            // Softest: only block_item_hotbar — allow free movement but check destination
            // (destination check is handled below for the carried item)

        } catch (Exception e) {
            // Silently ignore to prevent crashes
        }
    }

    /**
     * Check if the player is trying to place a locked item (currently carried by mouse)
     * into a hotbar slot. Only applies when block_item_hotbar is enabled.
     */
    @Unique
    private void progressivestages$checkHotbarDestination(int slotId, ClickType clickType, ServerPlayer player, CallbackInfo ci) {
        // This method is called for non-locked items in the clicked slot.
        // We also need to check if the player is CARRYING a locked item on their cursor
        // and trying to place it into a hotbar slot.
        LockRegistry registry = LockRegistry.getInstance();
        if (!StageConfig.isBlockItemHotbar() && !registry.hasEnforcementOverrides()) {
            return;
        }

        // Get the item currently carried by the mouse cursor
        ItemStack carried = ((AbstractContainerMenu)(Object)this).getCarried();
        if (carried.isEmpty()) {
            return;
        }

        // v2.0 multi-stage
        java.util.Set<com.enviouse.progressivestages.common.api.StageId> missing =
            registry.missingStagesForItem(player, carried.getItem());
        if (missing.isEmpty()
                || !registry.isCategoryEnforced(missing,
                    com.enviouse.progressivestages.common.lock.EnforcementCategory.ITEM_HOTBAR)
                || registry.isExemptFromHotbar(carried.getItem(), missing)) return;

        // Carried item is locked — check if target is a hotbar slot
        if (progressivestages$isHotbarSlot(slotId)) {
            ci.cancel();
            ItemEnforcer.notifyLockedWithCooldown(player, carried.getItem());
        }
    }

    /**
     * Check if a container slot index maps to a player hotbar slot.
     * In the player inventory container, hotbar slots are indices 36-44 (mapped from inventory 0-8).
     * In other containers, the player hotbar is typically the last 9 slots.
     */
    @Unique
    private boolean progressivestages$isHotbarSlot(int slotId) {
        if (slotId < 0 || slotId >= slots.size()) {
            return false;
        }

        Slot slot = slots.get(slotId);
        if (slot == null) {
            return false;
        }

        // Check if this slot belongs to the player's inventory and is a hotbar slot (index 0-8)
        if (slot.container instanceof net.minecraft.world.entity.player.Inventory) {
            return slot.getContainerSlot() >= 0 && slot.getContainerSlot() <= 8;
        }

        return false;
    }

    @Unique
    private Optional<InventoryInsertionEnforcer.Decision> progressivestages$deniedInventoryInsertion(
            int slotId, int button, ClickType clickType, ServerPlayer player) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (clickType == ClickType.PICKUP_ALL || clickType == ClickType.THROW || clickType == ClickType.CLONE) {
            return Optional.empty();
        }
        if (clickType == ClickType.QUICK_CRAFT) {
            for (Slot destination : quickcraftSlots) {
                if (progressivestages$canReceive(destination, menu.getCarried())) {
                    Optional<InventoryInsertionEnforcer.Decision> denied = InventoryInsertionEnforcer.denied(
                        player, menu, destination, menu.getCarried());
                    if (denied.isPresent()) return denied;
                }
            }
            return progressivestages$checkDestination(slotId, player, menu.getCarried(), clickType);
        }
        if (clickType == ClickType.SWAP) {
            return progressivestages$checkSwapDestinations(slotId, button, player);
        }
        if (clickType == ClickType.QUICK_MOVE) {
            if (slotId < 0 || slotId >= slots.size()) return Optional.empty();
            Slot source = slots.get(slotId);
            if (!source.hasItem() || !source.mayPickup(player)) return Optional.empty();
            ItemStack stack = source.getItem();
            boolean sourceIsPlayerInventory = source.container == player.getInventory();
            for (Slot destination : slots) {
                if (destination == source || sourceIsPlayerInventory == (destination.container == player.getInventory())
                        || !progressivestages$canReceive(destination, stack)) continue;
                Optional<InventoryInsertionEnforcer.Decision> denied = InventoryInsertionEnforcer.denied(
                    player, menu, destination, stack);
                if (denied.isPresent()) return denied;
            }
            return Optional.empty();
        }
        return progressivestages$checkDestination(slotId, player, menu.getCarried(), clickType);
    }

    @Unique
    private Optional<InventoryInsertionEnforcer.Decision> progressivestages$checkSwapDestinations(
            int slotId, int button, ServerPlayer player) {
        if (slotId < 0 || slotId >= slots.size() || button < 0 || button >= player.getInventory().getContainerSize()) {
            return Optional.empty();
        }
        Slot destination = slots.get(slotId);
        ItemStack hotbarStack = player.getInventory().getItem(button);
        ItemStack destinationStack = destination.getItem();
        boolean hotbarCanEnter = !hotbarStack.isEmpty() && destination.mayPlace(hotbarStack);
        if (hotbarCanEnter) {
            Optional<InventoryInsertionEnforcer.Decision> denied = InventoryInsertionEnforcer.denied(
                player, (AbstractContainerMenu) (Object) this, destination, hotbarStack);
            if (denied.isPresent()) return denied;
        }
        if (destinationStack.isEmpty() || !destination.mayPickup(player)
                || !hotbarStack.isEmpty() && !hotbarCanEnter) return Optional.empty();
        return progressivestages$checkPlayerInventoryDestination(button, player, destinationStack);
    }

    @Unique
    private Optional<InventoryInsertionEnforcer.Decision> progressivestages$checkPlayerInventoryDestination(
            int inventorySlot, ServerPlayer player, ItemStack source) {
        for (Slot destination : slots) {
            if (destination.container == player.getInventory() && destination.getContainerSlot() == inventorySlot) {
                return InventoryInsertionEnforcer.denied(player, (AbstractContainerMenu) (Object) this, destination, source);
            }
        }
        return Optional.empty();
    }

    @Unique
    private Optional<InventoryInsertionEnforcer.Decision> progressivestages$checkDestination(
            int slotId, ServerPlayer player, ItemStack source, ClickType clickType) {
        if (slotId < 0 || slotId >= slots.size() || source == null || source.isEmpty()) return Optional.empty();
        Slot destination = slots.get(slotId);
        boolean canInsert = clickType == ClickType.PICKUP
            ? progressivestages$canReceiveByPickup(destination, source, player)
            : progressivestages$canReceive(destination, source);
        if (!canInsert) return Optional.empty();
        return InventoryInsertionEnforcer.denied(player, (AbstractContainerMenu) (Object) this, destination, source);
    }

    @Unique
    private boolean progressivestages$canReceiveByPickup(Slot destination, ItemStack source, ServerPlayer player) {
        if (destination == null || source == null || source.isEmpty() || !destination.mayPlace(source)) return false;
        ItemStack existing = destination.getItem();
        if (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, source)) {
            return existing.isEmpty() || existing.getCount() < destination.getMaxStackSize(source);
        }
        return destination.mayPickup(player) && source.getCount() <= destination.getMaxStackSize(source);
    }

    @Unique
    private boolean progressivestages$canReceive(Slot destination, ItemStack source) {
        if (destination == null || source == null || source.isEmpty() || !destination.mayPlace(source)) return false;
        ItemStack existing = destination.getItem();
        return existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, source)
            && existing.getCount() < destination.getMaxStackSize(source);
    }
}
