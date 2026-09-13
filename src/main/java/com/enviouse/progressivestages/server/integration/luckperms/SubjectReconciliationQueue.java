package com.enviouse.progressivestages.server.integration.luckperms;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

final class SubjectReconciliationQueue {
    private final int limit;
    private final Queue<UUID> pending = new ArrayDeque<>();
    private final Set<UUID> queued = new HashSet<>();
    private boolean rescanRequested;
    private boolean scanning;
    private boolean scanTurn;
    private int scanIndex;

    SubjectReconciliationQueue(int limit) {
        if (limit < 1) throw new IllegalArgumentException("Queue limit must be positive");
        this.limit = limit;
    }

    synchronized void request(UUID subject) {
        if (subject == null || queued.contains(subject)) return;
        if (pending.size() == limit) {
            requestRescan();
            return;
        }
        pending.add(subject);
        queued.add(subject);
    }

    synchronized void requestRescan() {
        rescanRequested = true;
    }

    synchronized void remove(UUID subject) {
        pending.remove(subject);
        queued.remove(subject);
    }

    synchronized UUID poll(IntSupplier subjectCount, IntFunction<UUID> subjectAt) {
        UUID next = null;
        if (scanTurn) next = scanOne(subjectCount, subjectAt);
        if (next == null) next = pending.poll();
        if (next == null && !scanTurn) next = scanOne(subjectCount, subjectAt);
        scanTurn = !scanTurn;
        if (next != null) {
            queued.remove(next);
            pending.remove(next);
        }
        return next;
    }

    private UUID scanOne(IntSupplier subjectCount, IntFunction<UUID> subjectAt) {
        if (!scanning) {
            if (!rescanRequested) return null;
            rescanRequested = false;
            scanning = true;
            scanIndex = 0;
        }
        if (scanIndex >= subjectCount.getAsInt()) {
            scanning = false;
            return null;
        }
        return subjectAt.apply(scanIndex++);
    }

    synchronized int size() { return pending.size(); }

    synchronized boolean hasRescan() { return scanning || rescanRequested; }

    synchronized void clear() {
        pending.clear();
        queued.clear();
        scanning = false;
        rescanRequested = false;
        scanTurn = false;
        scanIndex = 0;
    }
}
