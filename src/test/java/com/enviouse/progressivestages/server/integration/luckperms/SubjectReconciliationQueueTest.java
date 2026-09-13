package com.enviouse.progressivestages.server.integration.luckperms;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SubjectReconciliationQueueTest {
    @Test
    void overflowPreservesPendingWorkAndEventuallyVisitsEverySubject() {
        var queue = new SubjectReconciliationQueue(256);
        var subjects = subjects(1024);
        subjects.forEach(queue::request);
        assertEquals(256, queue.size());
        assertTrue(queue.hasRescan());
        Set<UUID> visited = new HashSet<>();
        for (int tick = 0; tick < 100; tick++) {
            for (int budget = 0; budget < 16; budget++) {
                UUID next = queue.poll(subjects::size, subjects::get);
                if (next == null) break;
                visited.add(next);
            }
            assertTrue(queue.size() <= 256);
        }
        assertEquals(new HashSet<>(subjects), visited);
        assertEquals(0, queue.size());
        assertFalse(queue.hasRescan());
    }

    @Test
    void repeatedEventsAreCoalescedWithoutRequestingAnotherScan() {
        var queue = new SubjectReconciliationQueue(2);
        var subjects = subjects(2);
        subjects.forEach(queue::request);
        for (int i = 0; i < 1000; i++) queue.request(subjects.getFirst());
        assertEquals(2, queue.size());
        assertFalse(queue.hasRescan());
        assertEquals(subjects.getFirst(), queue.poll(subjects::size, subjects::get));
        assertEquals(subjects.getLast(), queue.poll(subjects::size, subjects::get));
        assertNull(queue.poll(subjects::size, subjects::get));
    }

    @Test
    void ongoingEventTrafficCannotRestartOrStarveTheActiveScan() {
        var queue = new SubjectReconciliationQueue(16);
        var subjects = subjects(512);
        queue.requestRescan();
        Set<UUID> visited = new HashSet<>();
        for (int tick = 0; tick < 100; tick++) {
            subjects.subList(0, 32).forEach(queue::request);
            for (int budget = 0; budget < 16; budget++) {
                UUID next = queue.poll(subjects::size, subjects::get);
                if (next == null) break;
                visited.add(next);
            }
            assertTrue(queue.size() <= 16);
        }
        assertEquals(new HashSet<>(subjects), visited);
    }

    @Test
    void aRescanRequestedDuringScanningGetsAnotherCompletePass() {
        var queue = new SubjectReconciliationQueue(2);
        var subjects = subjects(4);
        queue.requestRescan();
        assertEquals(subjects.getFirst(), queue.poll(subjects::size, subjects::get));
        queue.requestRescan();
        Map<UUID, Integer> visits = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            UUID next = queue.poll(subjects::size, subjects::get);
            if (next != null) visits.merge(next, 1, Integer::sum);
        }
        assertEquals(1, visits.get(subjects.getFirst()));
        subjects.subList(1, 4).forEach(id -> assertEquals(2, visits.get(id)));
        assertFalse(queue.hasRescan());
    }

    @Test
    void removingAnEarlierMemberAndRequestingRescanCannotSkipAnotherMember() {
        var queue = new SubjectReconciliationQueue(2);
        var subjects = new ArrayList<>(subjects(5));
        queue.requestRescan();
        assertEquals(subjects.getFirst(), queue.poll(subjects::size, subjects::get));
        UUID skippedByIndexShift = subjects.get(1);
        queue.remove(subjects.removeFirst());
        queue.requestRescan();
        Set<UUID> visited = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            UUID next = queue.poll(subjects::size, subjects::get);
            if (next != null) visited.add(next);
        }
        assertTrue(visited.contains(skippedByIndexShift));
        assertEquals(new HashSet<>(subjects), visited);
    }

    @Test
    void scansDoNotMaterializeThePopulationOrFillTheDirtyQueue() {
        var queue = new SubjectReconciliationQueue(256);
        AtomicInteger reads = new AtomicInteger();
        queue.requestRescan();
        for (int budget = 0; budget < 16; budget++) {
            assertNotNull(queue.poll(() -> 1_000_000, index -> {
                reads.incrementAndGet();
                return new UUID(0, index + 1);
            }));
        }
        assertEquals(16, reads.get());
        assertEquals(0, queue.size());
        assertTrue(queue.hasRescan());
    }

    @Test
    void shutdownClearsBothPendingWorkAndPartialScanState() {
        var queue = new SubjectReconciliationQueue(2);
        var subjects = subjects(4);
        subjects.forEach(queue::request);
        queue.poll(subjects::size, subjects::get);
        queue.clear();
        assertEquals(0, queue.size());
        assertFalse(queue.hasRescan());
        assertNull(queue.poll(subjects::size, subjects::get));
        queue.requestRescan();
        assertEquals(subjects.getFirst(), queue.poll(subjects::size, subjects::get));
    }

    @Test
    void anEmptyPopulationFinishesWithoutInventingWork() {
        var queue = new SubjectReconciliationQueue(256);
        queue.request(null);
        queue.requestRescan();
        assertNull(queue.poll(() -> 0, index -> { throw new AssertionError("No subject exists"); }));
        assertFalse(queue.hasRescan());
        assertEquals(0, queue.size());
    }

    private static List<UUID> subjects(int count) {
        var result = new ArrayList<UUID>();
        for (int i = 1; i <= count; i++) result.add(new UUID(0, i));
        return List.copyOf(result);
    }
}
