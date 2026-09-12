package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.InteractionDecision;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.lock.PrefixEntry;
import com.enviouse.progressivestages.common.stage.StageManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * Handles interaction locking (item-on-block, block right-click, item-on-entity, etc.)
 * Useful for Create mod style interactions.
 *
 * <p>Interaction keys are multi-stage: when several stages declare the same held-item/target
 * combination, the player must own every gating stage. Tag and wildcard entries participate in
 * the same most-restrictive-wins resolution.
 */
public class InteractionEnforcer {

    public static final String TYPE_ITEM_ON_BLOCK = "item_on_block";
    public static final String TYPE_BLOCK_RIGHT_CLICK = "block_right_click";
    public static final String TYPE_ITEM_ON_ENTITY = "item_on_entity";

    /**
     * Check if an interaction is allowed
     * @param player The player performing the interaction
     * @param heldItem The item held by the player (can be empty)
     * @param targetBlock The block being interacted with
     * @return true if allowed, false if blocked
     */
    public static boolean canInteract(ServerPlayer player, ItemStack heldItem, Block targetBlock) {
        return evaluateInteraction(player, heldItem, targetBlock).allowed();
    }

    /**
     * Evaluate item on block and block right click rules once for the actual
     * player, stack, and block holders.
     */
    public static InteractionDecision evaluateInteraction(ServerPlayer player, ItemStack heldItem,
                                                           Block targetBlock) {
        if (!StageConfig.isBlockInteractions()
                || (player != null && StageConfig.isAllowCreativeBypass() && player.isCreative())) {
            return new InteractionDecision(List.of(), List.of(), InteractionDecision.Reason.BYPASS, true);
        }

        ItemStack stack = heldItem == null ? ItemStack.EMPTY : heldItem;
        Collection<LockRegistry.InteractionLockEntry> itemRules =
            LockRegistry.getInstance().getAllInteractionLocksOfType(TYPE_ITEM_ON_BLOCK);
        Collection<LockRegistry.InteractionLockEntry> blockRules =
            LockRegistry.getInstance().getAllInteractionLocksOfType(TYPE_BLOCK_RIGHT_CLICK);
        boolean hasRules = !itemRules.isEmpty() || !blockRules.isEmpty();
        LinkedHashSet<StageId> matched = new LinkedHashSet<>();

        if (!stack.isEmpty()) {
            for (LockRegistry.InteractionLockEntry entry : itemRules) {
                if (matchesItemOnBlock(entry, stack, targetBlock)) matched.add(entry.requiredStage);
            }
        }
        for (LockRegistry.InteractionLockEntry entry : blockRules) {
            if (matchesBlockRule(entry, targetBlock)) matched.add(entry.requiredStage);
        }

        if (matched.isEmpty()) {
            InteractionDecision.Reason reason = hasRules
                ? InteractionDecision.Reason.SELECTOR_MISMATCH
                : InteractionDecision.Reason.NO_RULE;
            return new InteractionDecision(List.of(), List.of(), reason, true);
        }

        List<StageId> missing = new ArrayList<>();
        for (StageId stage : matched) {
            if (player == null || !StageManager.getInstance().hasStage(player, stage)) missing.add(stage);
        }
        InteractionDecision.Reason reason = missing.isEmpty()
            ? InteractionDecision.Reason.STAGE_OWNED : InteractionDecision.Reason.STAGE_MISSING;
        return new InteractionDecision(List.copyOf(matched), missing, reason, missing.isEmpty());
    }

    /**
     * Get the required stage for an interaction
     */
    public static Optional<StageId> getRequiredStage(ItemStack heldItem, Block targetBlock) {
        return matchingStages(heldItem, targetBlock).stream().findFirst();
    }

    /**
     * Notify player that an interaction is locked.
     * Checks exact-match locks first, then falls back to tag-pattern locks.
     */
    public static void notifyLocked(ServerPlayer player, ItemStack heldItem, Block targetBlock) {
        InteractionDecision decision = evaluateInteraction(player, heldItem, targetBlock);
        notifyLocked(player, decision);
    }

    public static void notifyLocked(ServerPlayer player, InteractionDecision decision) {
        if (!decision.missingStages().isEmpty()) {
            ItemEnforcer.notifyLocked(player, decision.missingStages().getFirst(),
                StageConfig.getMsgTypeLabelInteraction());
        }
    }

    private static List<StageId> matchingStages(ItemStack heldItem, Block targetBlock) {
        ItemStack stack = heldItem == null ? ItemStack.EMPTY : heldItem;
        LinkedHashSet<StageId> stages = new LinkedHashSet<>();
        if (!stack.isEmpty()) {
            for (LockRegistry.InteractionLockEntry entry : LockRegistry.getInstance()
                    .getAllInteractionLocksOfType(TYPE_ITEM_ON_BLOCK)) {
                if (matchesItemOnBlock(entry, stack, targetBlock)) stages.add(entry.requiredStage);
            }
        }
        for (LockRegistry.InteractionLockEntry entry : LockRegistry.getInstance()
                .getAllInteractionLocksOfType(TYPE_BLOCK_RIGHT_CLICK)) {
            if (matchesBlockRule(entry, targetBlock)) stages.add(entry.requiredStage);
        }
        return List.copyOf(stages);
    }

    private static boolean matchesItemOnBlock(LockRegistry.InteractionLockEntry entry,
                                               ItemStack stack, Block targetBlock) {
        return itemMatches(stack, entry.heldItem) && blockMatches(targetBlock, entry.targetBlock);
    }

    private static boolean matchesBlockRule(LockRegistry.InteractionLockEntry entry, Block targetBlock) {
        return blockMatches(targetBlock, entry.targetBlock);
    }

    private static String getItemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "*";
        }
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return rl != null ? rl.toString() : "*";
    }

    private static String getBlockId(Block block) {
        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
        return rl != null ? rl.toString() : "*";
    }

    /**
     * Check if an item matches a pattern (supports tags with #)
     */
    public static boolean itemMatches(ItemStack stack, String pattern) {
        if (pattern == null || pattern.isBlank() || pattern.equals("*")) return true;
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
        PrefixEntry selector = PrefixEntry.parse(pattern);
        return selector != null && selector.matches(itemId, holder, PrefixEntry.Keys.ITEM);
    }

    /**
     * Check if a player is allowed to interact with an entity while holding the given item.
     * Handles item_on_entity interaction locks, including wildcard and tag patterns.
     *
     * @param player     The player performing the interaction
     * @param heldItem   The item held by the player
     * @param entityType The entity type being interacted with
     * @return true if allowed, false if blocked
     */
    public static boolean canInteractWithEntity(ServerPlayer player, ItemStack heldItem, EntityType<?> entityType) {
        if (!StageConfig.isBlockInteractions()) {
            return true;
        }

        if (StageConfig.isAllowCreativeBypass() && player.isCreative()) {
            return true;
        }

        for (LockRegistry.InteractionLockEntry entry : LockRegistry.getInstance().getAllInteractionLocksOfType(TYPE_ITEM_ON_ENTITY)) {
            if (itemMatches(heldItem, entry.heldItem) && entityTypeMatches(entityType, entry.targetBlock)) {
                if (!StageManager.getInstance().hasStage(player, entry.requiredStage)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Notify player that an entity interaction is locked
     */
    public static void notifyEntityInteractionLocked(ServerPlayer player, ItemStack heldItem, EntityType<?> entityType) {
        String heldItemId = getItemId(heldItem);
        String entityTypeId = getEntityTypeId(entityType);

        Optional<StageId> required = firstMissing(player,
            LockRegistry.getInstance().getRequiredStagesForInteraction(
            TYPE_ITEM_ON_ENTITY, heldItemId, entityTypeId
        ));
        if (required.isPresent()) {
            ItemEnforcer.notifyLocked(player, required.get(), com.enviouse.progressivestages.common.config.StageConfig.getMsgTypeLabelInteraction());
            return;
        }
        // Tag-pattern fallback
        for (LockRegistry.InteractionLockEntry entry : LockRegistry.getInstance().getAllInteractionLocksOfType(TYPE_ITEM_ON_ENTITY)) {
            if (itemMatches(heldItem, entry.heldItem) && entityTypeMatches(entityType, entry.targetBlock)
                    && !StageManager.getInstance().hasStage(player, entry.requiredStage)) {
                ItemEnforcer.notifyLocked(player, entry.requiredStage, com.enviouse.progressivestages.common.config.StageConfig.getMsgTypeLabelInteraction());
                return;
            }
        }
    }

    private static String getEntityTypeId(EntityType<?> entityType) {
        ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return rl != null ? rl.toString() : "*";
    }

    private static Optional<StageId> firstMissing(ServerPlayer player, java.util.Collection<StageId> stages) {
        if (player == null || stages == null) return Optional.empty();
        for (StageId stage : stages) {
            if (!StageManager.getInstance().hasStage(player, stage)) return Optional.of(stage);
        }
        return Optional.empty();
    }

    /**
     * Check if an entity type matches a pattern (supports tags with #)
     */
    public static boolean entityTypeMatches(EntityType<?> entityType, String pattern) {
        if (pattern == null || pattern.isBlank() || pattern.equals("*")) return true;
        if (entityType == null) return false;
        PrefixEntry selector = PrefixEntry.parse(pattern);
        return selector != null && selector.matches(BuiltInRegistries.ENTITY_TYPE.getKey(entityType),
            BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityType), PrefixEntry.Keys.ENTITY_TYPE);
    }

    /**
     * Check if a block matches a pattern (supports tags with #)
     */
    public static boolean blockMatches(Block block, String pattern) {
        if (pattern == null || pattern.isBlank() || pattern.equals("*")) return true;
        if (block == null) return false;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        PrefixEntry selector = PrefixEntry.parse(pattern);
        return selector != null && selector.matches(blockId, BuiltInRegistries.BLOCK.wrapAsHolder(block),
            PrefixEntry.Keys.BLOCK);
    }
}
