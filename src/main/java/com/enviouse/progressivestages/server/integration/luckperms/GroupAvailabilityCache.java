package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

final class GroupAvailabilityCache {
    static final int MAX_ENTRIES = 256;
    static final int MAX_PENDING = 8;
    static final long LIFETIME_NANOS = TimeUnit.SECONDS.toNanos(10);
    private final Map<String, Entry> entries = new HashMap<>();
    private final LongSupplier clock;
    private int pending;

    GroupAvailabilityCache() { this(System::nanoTime); }
    GroupAvailabilityCache(LongSupplier clock) { this.clock = clock; }

    synchronized GroupStatus query(String group, Supplier<CompletableFuture<Boolean>> load) {
        long now = clock.getAsLong();
        entries.values().removeIf(entry -> entry.completed && now - entry.completedAt >= LIFETIME_NANOS);
        Entry previous = entries.get(group);
        if (previous != null) return previous.status;
        if (entries.size() >= MAX_ENTRIES || pending >= MAX_PENDING) return GroupStatus.UNKNOWN;
        Entry entry = new Entry();
        entries.put(group, entry);
        pending++;
        entry.future.whenComplete((present, failure) -> {
            synchronized (GroupAvailabilityCache.this) {
                if (entries.get(group) != entry) return;
                pending--;
                entry.status = failure != null ? GroupStatus.UNKNOWN
                    : Boolean.TRUE.equals(present) ? GroupStatus.PRESENT : GroupStatus.MISSING;
                entry.completedAt = clock.getAsLong();
                entry.completed = true;
            }
        });
        try {
            load.get().whenComplete((present, failure) -> {
                if (failure != null) entry.future.completeExceptionally(failure);
                else if (present == null) entry.future.completeExceptionally(new IllegalStateException("Group lookup returned no result"));
                else entry.future.complete(present);
            });
        } catch (RuntimeException failure) {
            entry.future.completeExceptionally(failure);
        }
        return entry.status;
    }

    synchronized void clear() {
        var previous = entries.values().stream().map(entry -> entry.future).toList();
        entries.clear();
        pending = 0;
        previous.forEach(future -> future.cancel(false));
    }

    private static final class Entry {
        private final CompletableFuture<Boolean> future = new CompletableFuture<>();
        private GroupStatus status = GroupStatus.UNKNOWN;
        private long completedAt;
        private boolean completed;
    }
}
