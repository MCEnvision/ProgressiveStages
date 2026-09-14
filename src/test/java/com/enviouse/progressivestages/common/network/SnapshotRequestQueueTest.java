package com.enviouse.progressivestages.common.network;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotRequestQueueTest {
    @Test
    void burstRetainsOnePendingFullRecoveryWithoutDelayingItAgain() {
        var queue = new SnapshotRequestQueue();
        var player = UUID.randomUUID();
        List<Map.Entry<UUID, Long>> sent = new ArrayList<>();
        assertTrue(queue.request(player, 7));
        assertFalse(queue.request(player, 8));
        assertFalse(queue.request(player, 0));
        for (int tick = 0; tick < 19; tick++) {
            for (int request = 0; request < 100; request++) assertFalse(queue.request(player, 9));
            queue.tick((id, revision) -> sent.add(Map.entry(id, revision)));
        }
        assertTrue(sent.isEmpty());
        queue.tick((id, revision) -> sent.add(Map.entry(id, revision)));
        assertEquals(List.of(Map.entry(player, 0L)), sent);
        for (int tick = 0; tick < 20; tick++) queue.tick((id, revision) -> fail("No duplicate request is pending."));
        assertTrue(queue.request(player, 10));
    }

    @Test
    void nonzeroRequestsCoalesceToTheLatestBaseAndRepeatAtMostOncePerWindow() {
        var queue = new SnapshotRequestQueue();
        var player = UUID.randomUUID();
        List<Long> sent = new ArrayList<>();
        assertTrue(queue.request(player, 1));
        for (int tick = 0; tick < 100; tick++) {
            assertFalse(queue.request(player, tick + 2));
            queue.tick((id, revision) -> sent.add(revision));
        }
        assertEquals(List.of(21L, 41L, 61L, 81L, 101L), sent);
    }

    @Test
    void disconnectAndShutdownCancelPendingWorkWithoutThrottlingAnotherPlayer() {
        var queue = new SnapshotRequestQueue();
        var player = UUID.randomUUID();
        var other = UUID.randomUUID();
        assertTrue(queue.request(player, 1));
        assertFalse(queue.request(player, 0));
        assertTrue(queue.request(other, 2));
        assertFalse(queue.request(other, 3));
        queue.clear(player);
        List<Map.Entry<UUID, Long>> sent = new ArrayList<>();
        for (int tick = 0; tick < 20; tick++) queue.tick((id, revision) -> sent.add(Map.entry(id, revision)));
        assertEquals(List.of(Map.entry(other, 3L)), sent);
        assertTrue(queue.request(player, 0));
        assertFalse(queue.request(player, 4));
        queue.clear();
        for (int tick = 0; tick < 20; tick++) queue.tick((id, revision) -> fail("Shutdown must cancel recovery."));
        assertTrue(queue.request(other, 0));
    }

    @Test
    void negativeRevisionRequestsFullRecovery() {
        var queue = new SnapshotRequestQueue();
        var player = UUID.randomUUID();
        assertTrue(queue.request(player, 1));
        assertFalse(queue.request(player, -1));
        assertFalse(queue.request(player, Long.MAX_VALUE));
        List<Long> sent = new ArrayList<>();
        for (int tick = 0; tick < 20; tick++) queue.tick((id, revision) -> sent.add(revision));
        assertEquals(List.of(0L), sent);
    }
}
