package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.rehaul.SelectorSpec;
import com.enviouse.progressivestages.common.rehaul.selector.SelectorMatcherRegistry;
import com.enviouse.progressivestages.common.rehaul.selector.SelectorTarget;
import com.enviouse.progressivestages.common.stage.StageManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.Set;
import java.util.stream.Collectors;

/** Server-authoritative decision for a player placing an item into one inventory slot. */
public final class InventoryInsertionEnforcer {
    public static final String TYPE = "item_into_inventory";

    private InventoryInsertionEnforcer() {}

    public static Optional<Decision> denied(ServerPlayer player, AbstractContainerMenu menu, Slot destination,
                                            ItemStack source) {
        if (player == null || menu == null || destination == null || source == null || source.isEmpty()) {
            return Optional.empty();
        }
        SelectorTarget sourceTarget = itemTarget(source.getItem());
        return resolve(
            LockRegistry.getInstance().getAllInteractionLocksOfType(TYPE),
            stage -> StageManager.getInstance().hasStage(player, stage), sourceTarget,
            destinationTargets(player, menu, destination))
            .filter(Decision::denied);
    }

    public static Optional<Decision> resolve(Collection<LockRegistry.InteractionLockEntry> entries,
                                             Predicate<StageId> ownsStage, SelectorTarget source,
                                             String targetKind, SelectorTarget destination) {
        return resolve(entries, ownsStage, source, Map.of(targetKind, destination));
    }

    public static Optional<Decision> resolve(Collection<LockRegistry.InteractionLockEntry> entries,
                                             Predicate<StageId> ownsStage, SelectorTarget source,
                                             Map<String, SelectorTarget> destinations) {
        return explain(entries, ownsStage, source, destinations)
            .flatMap(Explanation::winner);
    }

    public static Optional<Explanation> explain(Collection<LockRegistry.InteractionLockEntry> entries,
                                                Predicate<StageId> ownsStage, SelectorTarget source,
                                                String targetKind, SelectorTarget destination) {
        return explain(entries, ownsStage, source, Map.of(targetKind, destination));
    }

    public static Optional<Explanation> explain(Collection<LockRegistry.InteractionLockEntry> entries,
                                                Predicate<StageId> ownsStage, SelectorTarget source,
                                                Map<String, SelectorTarget> destinations) {
        if (entries == null || ownsStage == null || source == null || destinations == null) {
            return Optional.empty();
        }
        Decision winner = null;
        List<Decision> matchedDecisions = new ArrayList<>();
        for (LockRegistry.InteractionLockEntry entry : entries) {
            SelectorTarget destination = destinations.get(entry.targetKind);
            if (!TYPE.equals(entry.type) || destination == null) continue;
            if (!matches(entry.heldItem, source) || !matches(entry.targetBlock, destination)) continue;
            Decision candidate = decision(entry, ownsStage.test(entry.requiredStage), source.id(), entry.targetKind, destination.id());
            if (matchedDecisions.size() < 32) matchedDecisions.add(candidate);
            if (!candidate.active()) continue;
            if (winner == null || candidate.priority() > winner.priority()
                    || candidate.priority() == winner.priority() && candidate.denied() && !winner.denied()) {
                winner = candidate;
            }
        }
        if (matchedDecisions.isEmpty()) return Optional.empty();
        return Optional.of(new Explanation(source.id(), Map.copyOf(destinations), List.copyOf(matchedDecisions),
            Optional.ofNullable(winner)));
    }

    private static Decision decision(LockRegistry.InteractionLockEntry entry, boolean owned,
                                     ResourceLocation source, String targetKind, ResourceLocation destination) {
        boolean active = switch (entry.effect) {
            case "lock", "deny" -> !owned;
            case "allow", "unlock" -> owned;
            case "exclude" -> true;
            default -> false;
        };
        boolean denied = active && ("lock".equals(entry.effect) || "deny".equals(entry.effect));
        return new Decision(entry.requiredStage, entry.effect, entry.priority, source, targetKind, destination,
            entry.description, active, denied);
    }

    private static boolean matches(String rawSelector, SelectorTarget target) {
        return SelectorSpec.parse(rawSelector)
            .map(selector -> SelectorMatcherRegistry.get().match(selector, target).matched())
            .orElse(false);
    }

    private static SelectorTarget itemTarget(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return new SelectorTarget(id, Registries.ITEM.location(), tags(BuiltInRegistries.ITEM.wrapAsHolder(item)), java.util.Map.of());
    }

    private static Map<String, SelectorTarget> destinationTargets(ServerPlayer player, AbstractContainerMenu menu, Slot slot) {
        Map<String, SelectorTarget> targets = new LinkedHashMap<>();
        blockTarget(slot).ifPresent(target -> targets.put("block", target));
        menuTarget(menu).ifPresent(target -> targets.put("menu", target));
        InventoryTargetResolverRegistry.get().resolve(player, menu, slot)
            .map(target -> new SelectorTarget(target.id(), null, target.tags(), java.util.Map.of()))
            .ifPresent(target -> targets.put("inventory", target));
        return Map.copyOf(targets);
    }

    private static Optional<SelectorTarget> blockTarget(Slot slot) {
        if (!(slot.container instanceof BlockEntity blockEntity)) return Optional.empty();
        Block block = blockEntity.getBlockState().getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) return Optional.empty();
        return Optional.of(new SelectorTarget(id, Registries.BLOCK.location(),
            tags(BuiltInRegistries.BLOCK.wrapAsHolder(block)), java.util.Map.of()));
    }

    private static Optional<SelectorTarget> menuTarget(AbstractContainerMenu menu) {
        ResourceLocation id = BuiltInRegistries.MENU.getKey(menu.getType());
        return id == null ? Optional.empty() : Optional.of(new SelectorTarget(id, Registries.MENU.location(),
            tags(BuiltInRegistries.MENU.wrapAsHolder(menu.getType())), java.util.Map.of()));
    }

    private static Set<ResourceLocation> tags(Holder<?> holder) {
        return holder.tags().map(TagKey::location).collect(Collectors.toUnmodifiableSet());
    }

    public record Decision(StageId stage, String effect, int priority, ResourceLocation source, String targetKind,
                           ResourceLocation destination, String description, boolean active, boolean denied) {}

    public record Explanation(ResourceLocation source, Map<String, SelectorTarget> destinations,
                              List<Decision> matches, Optional<Decision> winner) {}
}
