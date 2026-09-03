package com.enviouse.progressivestages.client.emi;

import java.util.concurrent.atomic.AtomicLong;

final class EmiReloadSessionGate {
    static final long NO_PENDING_GENERATION = -1L;

    private final AtomicLong pendingGeneration = new AtomicLong(NO_PENDING_GENERATION);

    boolean claim(long refreshGeneration, long activeGeneration, boolean disconnecting) {
        if (disconnecting || activeGeneration != refreshGeneration) return false;
        while (true) {
            long pending = pendingGeneration.get();
            if (pending >= refreshGeneration) return false;
            if (pendingGeneration.compareAndSet(pending, refreshGeneration)) return true;
        }
    }

    void release(long refreshGeneration) {
        pendingGeneration.compareAndSet(refreshGeneration, NO_PENDING_GENERATION);
    }

    void clear() {
        pendingGeneration.set(NO_PENDING_GENERATION);
    }

    long pendingGeneration() {
        return pendingGeneration.get();
    }
}
