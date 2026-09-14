package com.enviouse.progressivestages.server.integration.luckperms;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class GroupAvailabilityCacheTest {
    @Test
    void missingRequiresACompletedNegativeLookupAndExpiresForRecovery() {
        var clock = new AtomicLong();
        var cache = new GroupAvailabilityCache(clock::get);
        var lookup = new CompletableFuture<Boolean>();
        assertEquals(UNKNOWN, cache.query("chef", () -> lookup));
        assertEquals(UNKNOWN, cache.query("chef", () -> { fail("Duplicate lookup"); return null; }));
        lookup.complete(false);
        assertEquals(MISSING, cache.query("chef", () -> { fail("Cached lookup"); return null; }));
        clock.addAndGet(GroupAvailabilityCache.LIFETIME_NANOS);
        assertEquals(PRESENT, cache.query("chef", () -> CompletableFuture.completedFuture(true)));
        cache.clear();
    }

    @Test
    void failedLookupStaysUnknownAndCanRecover() {
        var clock = new AtomicLong();
        var cache = new GroupAvailabilityCache(clock::get);
        assertEquals(UNKNOWN, cache.query("chef", () -> { throw new IllegalStateException("Unavailable"); }));
        clock.addAndGet(GroupAvailabilityCache.LIFETIME_NANOS);
        assertEquals(PRESENT, cache.query("chef", () -> CompletableFuture.completedFuture(true)));
        cache.clear();
    }

    @Test
    void pendingProviderRequestsRemainBoundedEvenWhenTheyNeverComplete() {
        var clock = new AtomicLong();
        var cache = new GroupAvailabilityCache(clock::get);
        var pending = new ArrayList<CompletableFuture<Boolean>>();
        var calls = new AtomicInteger();
        for (int index = 0; index < 1000; index++) {
            clock.addAndGet(GroupAvailabilityCache.LIFETIME_NANOS);
            assertEquals(UNKNOWN, cache.query("group" + index, () -> {
                calls.incrementAndGet();
                var future = new CompletableFuture<Boolean>();
                pending.add(future);
                return future;
            }));
        }
        assertEquals(GroupAvailabilityCache.MAX_PENDING, calls.get());
        pending.getFirst().complete(false);
        assertEquals(PRESENT, cache.query("next", () -> CompletableFuture.completedFuture(true)));
        cache.clear();
        assertTrue(pending.stream().noneMatch(CompletableFuture::isCancelled));
        pending.forEach(future -> future.complete(false));
    }

    @Test
    void completedEntriesAreBoundedAndLateCompletionsCannotReplaceNewObservations() {
        var clock = new AtomicLong();
        var cache = new GroupAvailabilityCache(clock::get);
        for (int index = 0; index < GroupAvailabilityCache.MAX_ENTRIES; index++) {
            assertEquals(PRESENT, cache.query("group" + index, () -> CompletableFuture.completedFuture(true)));
        }
        assertEquals(UNKNOWN, cache.query("overflow", () -> { fail("Cache capacity exceeded"); return null; }));
        clock.addAndGet(GroupAvailabilityCache.LIFETIME_NANOS);
        var old = new CompletableFuture<Boolean>();
        assertEquals(UNKNOWN, cache.query("chef", () -> old));
        cache.clear();
        assertEquals(PRESENT, cache.query("chef", () -> CompletableFuture.completedFuture(true)));
        old.complete(false);
        assertEquals(PRESENT, cache.query("chef", () -> { fail("New observation lost"); return null; }));
        cache.clear();
    }
}
