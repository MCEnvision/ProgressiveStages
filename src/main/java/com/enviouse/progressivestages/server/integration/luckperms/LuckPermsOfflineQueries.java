package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class LuckPermsOfflineQueries {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final int MAX_LOADS = 8;
    private final UserManager users;
    private final LuckPermsQueries queries;
    private final Map<UUID, Pending> pending = new HashMap<>();
    private boolean closed;

    LuckPermsOfflineQueries(Object api) {
        users = ((LuckPerms) api).getUserManager();
        queries = new LuckPermsQueries(api);
    }

    boolean request(UUID subject, Set<String> permissions, Consumer<UUID> completed) {
        Set<String> keys = Set.copyOf(permissions);
        Pending request;
        synchronized (this) {
            if (closed || pending.size() >= MAX_LOADS || pending.containsKey(subject)) return false;
            request = new Pending();
            pending.put(subject, request);
        }
        try {
            User loaded = users.getUser(subject);
            boolean owned = loaded == null;
            CompletableFuture<User> future = owned ? users.loadUser(subject) : CompletableFuture.completedFuture(loaded);
            future.whenComplete((user, failure) -> {
                LuckPermsAdapter.SubjectSnapshot snapshot = LuckPermsAdapter.SubjectSnapshot.unavailable();
                if (failure != null) LOGGER.debug("Unable to load offline LuckPerms eligibility", failure);
                try {
                    if (failure == null && user != null && user.getUniqueId().equals(subject)) {
                        snapshot = queries.offlineSnapshot(user, keys);
                    }
                } catch (RuntimeException | LinkageError unavailable) {
                    LOGGER.debug("Unable to query offline LuckPerms eligibility", unavailable);
                    snapshot = LuckPermsAdapter.SubjectSnapshot.unavailable();
                } finally {
                    if (owned && user != null) {
                        try { users.cleanupUser(user); }
                        catch (RuntimeException | LinkageError cleanup) {
                            request.cleanupFailed = user;
                            LOGGER.warn("Offline LuckPerms user cleanup is incomplete. The owned reference is retained.", cleanup);
                        }
                    }
                }
                finish(subject, request, snapshot, completed);
            });
        } catch (RuntimeException | LinkageError failure) {
            LOGGER.debug("Unable to start an offline LuckPerms query", failure);
            finish(subject, request, LuckPermsAdapter.SubjectSnapshot.unavailable(), completed);
        }
        return true;
    }

    private void finish(UUID subject, Pending request, LuckPermsAdapter.SubjectSnapshot snapshot, Consumer<UUID> completed) {
        synchronized (this) {
            request.snapshot = snapshot;
            request.complete = true;
            if (closed) {
                if (request.cleanupFailed == null) pending.remove(subject, request);
                return;
            }
            completed.accept(subject);
        }
    }

    LuckPermsAdapter.OfflineResult take(UUID subject) {
        Pending request;
        synchronized (this) {
            request = pending.get(subject);
            if (request == null || !request.complete) return null;
        }
        if (request.cleanupFailed != null) {
            try { users.cleanupUser(request.cleanupFailed); request.cleanupFailed = null; }
            catch (RuntimeException | LinkageError failure) { throw new IllegalStateException("Offline LuckPerms user cleanup is incomplete", failure); }
        }
        synchronized (this) {
            return new LuckPermsAdapter.OfflineResult(closed || request.invalidated, request.snapshot);
        }
    }

    synchronized boolean current(UUID subject) {
        Pending request = pending.get(subject);
        return !closed && request != null && request.complete && !request.invalidated && request.cleanupFailed == null;
    }

    synchronized void complete(UUID subject) {
        Pending request = pending.get(subject);
        if (request != null && request.complete && request.cleanupFailed == null) pending.remove(subject);
    }

    synchronized boolean owns(UUID subject) {
        Pending request = pending.get(subject);
        return request != null && !request.complete;
    }

    synchronized void invalidate(UUID subject) {
        Pending request = pending.get(subject);
        if (request != null) request.invalidated = true;
    }

    synchronized void invalidateAll() { pending.values().forEach(request -> request.invalidated = true); }

    boolean close() {
        Map<UUID, Pending> requests;
        synchronized (this) {
            closed = true;
            pending.values().forEach(request -> request.invalidated = true);
            requests = Map.copyOf(pending);
        }
        for (var entry : requests.entrySet()) {
            Pending request = entry.getValue();
            if (!request.complete) continue;
            if (request.cleanupFailed != null) {
                try { users.cleanupUser(request.cleanupFailed); request.cleanupFailed = null; }
                catch (RuntimeException | LinkageError failure) { continue; }
            }
            synchronized (this) { pending.remove(entry.getKey(), request); }
        }
        synchronized (this) { return pending.isEmpty(); }
    }

    private static final class Pending {
        boolean invalidated;
        volatile boolean complete;
        volatile User cleanupFailed;
        LuckPermsAdapter.SubjectSnapshot snapshot;
    }
}
