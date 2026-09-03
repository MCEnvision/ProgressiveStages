package com.enviouse.progressivestages.server.enforcement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves stable owner identities for inventories that are not backed by a block or menu type.
 * Resolvers run only on the logical server while handling an authenticated player transaction.
 */
public final class InventoryTargetResolverRegistry {
    public static final int MAX_RESOLVERS = 64;
    public static final int MAX_CATALOG_TARGETS = 2048;
    private static final ResourceLocation PLAYER_INVENTORY = ResourceLocation.withDefaultNamespace("player_inventory");
    private static final InventoryTargetResolverRegistry INSTANCE = new InventoryTargetResolverRegistry();

    private volatile Map<ResourceLocation, InventoryTargetResolver> resolvers;

    private InventoryTargetResolverRegistry() {
        Map<ResourceLocation, InventoryTargetResolver> builtIns = new LinkedHashMap<>();
        ResourceLocation builtInId = ResourceLocation.fromNamespaceAndPath("progressivestages", "player_inventory");
        builtIns.put(builtInId,
            (player, menu, slot) -> slot.container == player.getInventory()
                ? Optional.of(new InventoryTarget(PLAYER_INVENTORY, Set.of(PLAYER_INVENTORY)))
                : Optional.empty());
        resolvers = immutable(builtIns);
        targets = List.of(new InventoryTargetDescriptor(PLAYER_INVENTORY, "Player inventory",
            Set.of(PLAYER_INVENTORY), builtInId));
    }

    public static InventoryTargetResolverRegistry get() {
        return INSTANCE;
    }

    static InventoryTargetResolverRegistry createForTesting() {
        return new InventoryTargetResolverRegistry();
    }

    public synchronized void register(ResourceLocation resolverId, InventoryTargetResolver resolver) {
        register(resolverId, resolver, List.of());
    }

    public synchronized void register(ResourceLocation resolverId, InventoryTargetResolver resolver,
                                      List<InventoryTargetDescriptor> contributedTargets) {
        Objects.requireNonNull(resolverId, "resolverId");
        Objects.requireNonNull(resolver, "resolver");
        if (resolvers.containsKey(resolverId)) {
            throw new IllegalArgumentException("Duplicate inventory target resolver. " + resolverId);
        }
        if (resolvers.size() >= MAX_RESOLVERS) {
            throw new IllegalArgumentException("Too many inventory target resolvers. " + MAX_RESOLVERS + " maximum");
        }
        Map<ResourceLocation, InventoryTargetResolver> copy = new LinkedHashMap<>(resolvers);
        copy.put(resolverId, resolver);
        List<InventoryTargetDescriptor> nextTargets = new ArrayList<>(targets);
        for (InventoryTargetDescriptor target : contributedTargets == null ? List.<InventoryTargetDescriptor>of() : contributedTargets) {
            if (nextTargets.size() >= MAX_CATALOG_TARGETS) {
                throw new IllegalArgumentException("Too many inventory target catalog entries. " + MAX_CATALOG_TARGETS + " maximum");
            }
            if (nextTargets.stream().anyMatch(existing -> existing.id().equals(target.id()))) {
                throw new IllegalArgumentException("Duplicate inventory target identity. " + target.id());
            }
            if (!target.resolver().equals(resolverId)) {
                throw new IllegalArgumentException("Inventory target belongs to a different resolver. " + target.id());
            }
            nextTargets.add(target);
        }
        resolvers = immutable(copy);
        targets = List.copyOf(nextTargets);
    }

    public Optional<InventoryTarget> resolve(ServerPlayer player, AbstractContainerMenu menu, Slot slot) {
        return resolution(player, menu, slot).target();
    }

    public Resolution resolution(ServerPlayer player, AbstractContainerMenu menu, Slot slot) {
        if (player == null || menu == null || slot == null) return Resolution.unresolved();
        List<InventoryTarget> matches = new ArrayList<>();
        for (InventoryTargetResolver resolver : resolvers.values()) {
            Optional<InventoryTarget> resolved = resolver.resolve(player, menu, slot);
            resolved.ifPresent(matches::add);
        }
        if (matches.isEmpty()) return Resolution.unresolved();
        if (matches.size() != 1) return Resolution.ambiguous();
        return Resolution.resolved(matches.getFirst());
    }

    public List<InventoryTargetDescriptor> catalogTargets() {
        return targets;
    }

    @FunctionalInterface
    public interface InventoryTargetResolver {
        Optional<InventoryTarget> resolve(ServerPlayer player, AbstractContainerMenu menu, Slot slot);
    }

    public record InventoryTarget(ResourceLocation id, Set<ResourceLocation> tags) {
        public InventoryTarget {
            Objects.requireNonNull(id, "id");
            tags = tags == null ? Set.of() : Set.copyOf(tags);
        }
    }

    public record InventoryTargetDescriptor(ResourceLocation id, String label, Set<ResourceLocation> tags,
                                            ResourceLocation resolver) {
        public InventoryTargetDescriptor {
            Objects.requireNonNull(id, "id");
            label = label == null || label.isBlank() ? id.toString() : label;
            tags = tags == null ? Set.of() : Set.copyOf(tags);
            Objects.requireNonNull(resolver, "resolver");
        }
    }

    public record Resolution(Status status, Optional<InventoryTarget> target) {
        static Resolution resolved(InventoryTarget target) {
            return new Resolution(Status.RESOLVED, Optional.of(target));
        }

        static Resolution unresolved() {
            return new Resolution(Status.UNRESOLVED, Optional.empty());
        }

        static Resolution ambiguous() {
            return new Resolution(Status.AMBIGUOUS, Optional.empty());
        }
    }

    public enum Status {
        RESOLVED,
        UNRESOLVED,
        AMBIGUOUS
    }

    private static <T> Map<ResourceLocation, T> immutable(Map<ResourceLocation, T> entries) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    private volatile List<InventoryTargetDescriptor> targets;
}
