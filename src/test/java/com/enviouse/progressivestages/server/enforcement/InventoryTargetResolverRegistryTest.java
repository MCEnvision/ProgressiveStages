package com.enviouse.progressivestages.server.enforcement;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTargetResolverRegistryTest {

    @Test
    void contributedTargetIsVisibleToTheCatalogWithItsStableTags() {
        InventoryTargetResolverRegistry registry = InventoryTargetResolverRegistry.createForTesting();
        ResourceLocation resolver = ResourceLocation.parse("example:merchant_inventory");
        ResourceLocation target = ResourceLocation.parse("example:selling_bin");
        registry.register(resolver, (player, menu, slot) -> Optional.empty(), List.of(
            new InventoryTargetResolverRegistry.InventoryTargetDescriptor(target, "Selling bin",
                Set.of(ResourceLocation.parse("example:selling_bins")), resolver)));

        var descriptor = registry.catalogTargets().stream()
            .filter(value -> value.id().equals(target))
            .findFirst().orElseThrow();
        assertEquals("Selling bin", descriptor.label());
        assertTrue(descriptor.tags().contains(ResourceLocation.parse("example:selling_bins")));
        assertEquals(resolver, descriptor.resolver());
    }

    @Test
    void duplicateResolverAndTargetIdentitiesFailBeforeRuntimeResolution() {
        InventoryTargetResolverRegistry registry = InventoryTargetResolverRegistry.createForTesting();
        ResourceLocation resolver = ResourceLocation.parse("example:inventory");
        ResourceLocation target = ResourceLocation.parse("example:bin");
        var descriptor = new InventoryTargetResolverRegistry.InventoryTargetDescriptor(target, "Bin", Set.of(), resolver);
        registry.register(resolver, (player, menu, slot) -> Optional.empty(), List.of(descriptor));

        assertThrows(IllegalArgumentException.class,
            () -> registry.register(resolver, (player, menu, slot) -> Optional.empty()));
        ResourceLocation secondResolver = ResourceLocation.parse("example:other_inventory");
        assertThrows(IllegalArgumentException.class,
            () -> registry.register(secondResolver, (player, menu, slot) -> Optional.empty(), List.of(
                new InventoryTargetResolverRegistry.InventoryTargetDescriptor(target, "Other bin", Set.of(), secondResolver))));
    }

    @Test
    void descriptorMustBelongToTheRegisteringResolver() {
        InventoryTargetResolverRegistry registry = InventoryTargetResolverRegistry.createForTesting();
        ResourceLocation resolver = ResourceLocation.parse("example:inventory");
        var descriptor = new InventoryTargetResolverRegistry.InventoryTargetDescriptor(ResourceLocation.parse("example:bin"),
            "Bin", Set.of(), ResourceLocation.parse("example:other_inventory"));

        assertThrows(IllegalArgumentException.class,
            () -> registry.register(resolver, (player, menu, slot) -> Optional.empty(), List.of(descriptor)));
    }

    @Test
    void resolverRegistrationHasAnExplicitHotPathBound() {
        InventoryTargetResolverRegistry registry = InventoryTargetResolverRegistry.createForTesting();
        for (int index = 1; index < InventoryTargetResolverRegistry.MAX_RESOLVERS; index++) {
            registry.register(ResourceLocation.fromNamespaceAndPath("example", "resolver" + index),
                (player, menu, slot) -> Optional.empty());
        }

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> registry.register(ResourceLocation.fromNamespaceAndPath("example", "overflow"),
                (player, menu, slot) -> Optional.empty()));

        assertTrue(error.getMessage().contains("Too many inventory target resolvers"));
    }
}
