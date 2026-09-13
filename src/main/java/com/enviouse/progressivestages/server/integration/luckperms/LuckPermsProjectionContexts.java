package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class LuckPermsProjectionContexts implements ContextCalculator<Object>, AutoCloseable {
    private final ContextManager contexts;
    private final Map<UUID, Projection> subjects = new HashMap<>();
    private final Map<Object, Projection> targets = new ConcurrentHashMap<>();
    private long sequence;
    private volatile long generation;
    private volatile boolean closing;
    private boolean registered;

    LuckPermsProjectionContexts(Object api) {
        contexts = ((LuckPerms) api).getContextManager();
    }

    void register() {
        if (registered || closing) return;
        registered = true;
        contexts.registerCalculator(this);
    }

    long prepare(UUID subject, Object target) {
        invalidate(subject);
        synchronized (this) {
            if (closing || !registered) return -1;
            Projection projection = new Projection(target, ++sequence, generation);
            subjects.put(subject, projection);
            targets.put(target, projection);
            return projection.ticket;
        }
    }

    boolean publish(UUID subject, long ticket) {
        Projection projection;
        synchronized (this) {
            projection = subjects.get(subject);
            if (closing || projection == null || !projection.valid || projection.ticket != ticket
                || projection.generation != generation) return false;
            projection.active = true;
        }
        try {
            contexts.signalContextUpdate(projection.target);
        } catch (RuntimeException | LinkageError failure) {
            projection.active = false;
            projection.valid = false;
            try { contexts.signalContextUpdate(projection.target); }
            catch (RuntimeException | LinkageError invalidation) { failure.addSuppressed(invalidation); }
            throw failure;
        }
        return projection.valid && projection.active && !closing && projection.generation == generation;
    }

    synchronized void markInvalid(UUID subject) {
        Projection projection = subjects.get(subject);
        if (projection != null) {
            projection.valid = false;
            projection.active = false;
        }
    }

    synchronized boolean tracksSubject(UUID subject) { return subjects.containsKey(subject); }

    synchronized void markAllInvalid() {
        generation++;
    }

    void invalidate(UUID subject) {
        Projection projection;
        synchronized (this) {
            projection = subjects.get(subject);
            if (projection == null) return;
            projection.active = false;
            projection.valid = false;
        }
        contexts.signalContextUpdate(projection.target);
        synchronized (this) {
            subjects.remove(subject, projection);
            targets.remove(projection.target, projection);
        }
    }

    boolean invalidateAll() {
        Map<UUID, Projection> pending;
        synchronized (this) {
            pending = Map.copyOf(subjects);
            pending.values().forEach(projection -> {
                projection.active = false;
                projection.valid = false;
            });
        }
        boolean complete = true;
        for (UUID subject : pending.keySet()) {
            try {
                invalidate(subject);
            } catch (RuntimeException | LinkageError failure) {
                complete = false;
            }
        }
        return complete;
    }

    @Override
    public void calculate(Object target, ContextConsumer consumer) {
        Projection projection = targets.get(target);
        if (!closing && projection != null && projection.valid && projection.active
            && projection.generation == generation) {
            consumer.accept("progressivestages_bridge", "active");
        }
    }

    @Override
    public void close() {
        closing = true;
        boolean complete = invalidateAll();
        if (registered) {
            contexts.unregisterCalculator(this);
            registered = false;
        }
        if (!complete) throw new IllegalStateException("LuckPerms projection context invalidation is incomplete");
    }

    private static final class Projection {
        final Object target;
        final long ticket;
        final long generation;
        volatile boolean valid = true;
        volatile boolean active;

        Projection(Object target, long ticket, long generation) {
            this.target = java.util.Objects.requireNonNull(target);
            this.ticket = ticket;
            this.generation = generation;
        }
    }
}
