package com.enviouse.progressivestages.client;

import com.enviouse.progressivestages.common.api.StageId;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientLockCacheTest {

    @AfterEach
    void clearMultiLocks() {
        ClientLockCache.clear();
    }

    @Test
    void synchronizedStageSetsAreDefensiveCopies() {
        ResourceLocation item = ResourceLocation.parse("minecraft:diamond");
        StageId stage = StageId.parse("diamond_age");
        Set<StageId> sourceStages = new HashSet<>(Set.of(stage));
        Map<ResourceLocation, Set<StageId>> source = new HashMap<>();
        source.put(item, sourceStages);

        ClientLockCache.setItemMultiLocks(source);
        sourceStages.clear();

        Set<StageId> cached = ClientLockCache.getRequiredStagesForItem(item);
        assertEquals(Set.of(stage), cached);
        assertThrows(UnsupportedOperationException.class, () -> cached.add(StageId.parse("other")));
    }

    @Test
    void viewerLockSnapshotsStayIndependent() {
        ResourceLocation item = ResourceLocation.parse("minecraft:diamond_sword");
        StageId stage = StageId.parse("warrior");

        ClientLockCache.setEmiViewerItemLocks(Map.of(item, Set.of(stage)));

        assertEquals(Set.of(item), ClientLockCache.getEmiViewerItemIds());
        assertTrue(ClientLockCache.isItemHiddenInEmi(item));
        assertFalse(ClientLockCache.isItemHiddenInJei(item));

        ClientLockCache.setEmiViewerItemLocks(Map.of());
        ClientLockCache.setJeiViewerItemLocks(Map.of(item, Set.of(stage)));

        assertEquals(Set.of(item), ClientLockCache.getJeiViewerItemIds());
        assertEquals(Set.of(item), ClientLockCache.getJeiViewerCandidateItemIds());
        assertFalse(ClientLockCache.isItemHiddenInEmi(item));
        assertTrue(ClientLockCache.isItemHiddenInJei(item));
        assertEquals(Set.of(item), ClientLockCache.getJeiHiddenItemIds());

        ClientLockCache.setJeiViewerItemLocks(Map.of());

        assertEquals(Set.of(item), ClientLockCache.getJeiViewerCandidateItemIds());
        assertTrue(ClientLockCache.getJeiHiddenItemIds().isEmpty());
    }
}
