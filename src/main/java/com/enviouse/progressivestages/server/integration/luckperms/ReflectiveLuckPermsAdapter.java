package com.enviouse.progressivestages.server.integration.luckperms;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;

/** optional luckperms adapter with guarded provider access. */
final class ReflectiveLuckPermsAdapter implements LuckPermsAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Object api;
    private final GroupAvailabilityCache groupAvailability = new GroupAvailabilityCache();
    private LuckPermsQueries queries;
    private LuckPermsTransientNodes transientNodes;
    private LuckPermsProjectionContexts projectionContexts;
    private LuckPermsEventSubscriptions eventSubscriptions;
    private boolean closing;

    static ReflectiveLuckPermsAdapter create() {
        ReflectiveLuckPermsAdapter adapter = new ReflectiveLuckPermsAdapter();
        adapter.initialize();
        return adapter;
    }

    private void initialize() {
        if (!ModList.get().isLoaded("luckperms")) return;
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            api = provider.getMethod("get").invoke(null);
            queries = new LuckPermsQueries(api);
            transientNodes = new LuckPermsTransientNodes(api);
            projectionContexts = new LuckPermsProjectionContexts(api);
            projectionContexts.register();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            LOGGER.warn("luckperms integration is unavailable", exception);
            if (projectionContexts != null) {
                try { projectionContexts.close(); }
                catch (RuntimeException | LinkageError cleanup) {
                    LOGGER.warn("LuckPerms projection context cleanup is incomplete", cleanup);
                }
            }
            api = null;
        }
    }

    @Override
    public State state() {
        if (!ModList.get().isLoaded("luckperms")) return State.ABSENT;
        return api == null || closing ? State.FAILED : State.READY;
    }

    @Override
    public SubjectSnapshot snapshot(UUID player) {
        if (api == null || closing || player == null) return SubjectSnapshot.unavailable();
        try {
            return queries.snapshot(player);
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.debug("Unable to read independent LuckPerms contexts and groups", exception);
            return SubjectSnapshot.unavailable();
        }
    }

    @Override
    public PermissionValue permission(UUID player, String node) {
        return permissionResult(player, node).value();
    }

    @Override
    public PermissionResult permissionResult(UUID player, String node) {
        if (api == null || closing || player == null || node == null || node.isBlank()) {
            return PermissionResult.unavailable();
        }
        try {
            return queries.permission(player, node);
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.debug("Unable to read an independent LuckPerms permission", exception);
            return PermissionResult.unavailable();
        }
    }

    @Override
    public boolean groupExists(String group) {
        if (api == null || group == null || group.isBlank()) return false;
        try {
            Object manager = api.getClass().getMethod("getGroupManager").invoke(api);
            return manager.getClass().getMethod("getGroup", String.class).invoke(manager, group) != null;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    @Override
    public com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus groupStatus(String group) {
        var unknown = com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus.UNKNOWN;
        if (api == null || group == null || group.isBlank()) return unknown;
        try {
            Object manager = api.getClass().getMethod("getGroupManager").invoke(api);
            Class<?> managerType = Class.forName("net.luckperms.api.model.group.GroupManager");
            if (managerType.getMethod("getGroup", String.class).invoke(manager, group) != null) {
                return com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus.PRESENT;
            }
            return groupAvailability.query(group, () -> {
                try {
                    Object result = managerType.getMethod("loadGroup", String.class).invoke(manager, group);
                    if (!(result instanceof java.util.concurrent.CompletableFuture<?> future)) {
                        return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("Group lookup is unavailable"));
                    }
                    return future.thenApply(value -> {
                        if (!(value instanceof java.util.Optional<?> loaded)) throw new IllegalStateException("Invalid group lookup result");
                        return loaded.isPresent();
                    });
                } catch (ReflectiveOperationException | RuntimeException failure) {
                    return java.util.concurrent.CompletableFuture.failedFuture(failure);
                }
            });
        } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
            return unknown;
        }
    }

    @Override
    public boolean subscribeChanges(java.util.function.Consumer<UUID> subjectChanged, Runnable allChanged) {
        if (api == null || closing) return true;
        if (eventSubscriptions != null) throw new IllegalStateException("LuckPerms events are already subscribed");
        try {
            eventSubscriptions = new LuckPermsEventSubscriptions(api, subject -> {
                projectionContexts.markInvalid(subject);
                subjectChanged.accept(subject);
            }, () -> {
                projectionContexts.markAllInvalid();
                allChanged.run();
            });
            eventSubscriptions.register();
            return true;
        } catch (RuntimeException | LinkageError failure) {
            closing = true;
            projectionContexts.markAllInvalid();
            stopListening();
            LOGGER.warn("LuckPerms event registration failed. The bridge remains unavailable.", failure);
            return false;
        }
    }

    @Override
    public boolean stopListening() {
        if (eventSubscriptions == null) return true;
        try {
            eventSubscriptions.close();
            eventSubscriptions = null;
            return true;
        } catch (RuntimeException | LinkageError failure) {
            LOGGER.warn("LuckPerms event cleanup is incomplete. Subscriptions are retained for retry.", failure);
            return false;
        }
    }

    @Override
    public long prepareProjection(UUID subject, Object target) {
        if (api == null || closing || projectionContexts == null) return -1;
        try {
            return projectionContexts.prepare(subject, target);
        } catch (RuntimeException | LinkageError failure) {
            LOGGER.debug("Unable to invalidate the previous LuckPerms projection", failure);
            return -1;
        }
    }

    @Override
    public boolean publishProjection(UUID subject, long ticket) {
        if (api == null || closing || projectionContexts == null) return false;
        try {
            return projectionContexts.publish(subject, ticket);
        } catch (RuntimeException | LinkageError failure) {
            LOGGER.debug("Unable to publish the LuckPerms projection context", failure);
            return false;
        }
    }

    @Override
    public boolean invalidateProjection(UUID subject) {
        if (projectionContexts == null) return true;
        try {
            projectionContexts.invalidate(subject);
            return true;
        } catch (RuntimeException | LinkageError failure) {
            LOGGER.debug("Unable to invalidate the LuckPerms projection context", failure);
            return false;
        }
    }

    @Override
    public boolean invalidateProjections() {
        return projectionContexts == null || projectionContexts.invalidateAll();
    }

    @Override
    public boolean cleanupTransientNodes() {
        boolean complete = invalidateProjections();
        complete &= transientNodes == null || transientNodes.cleanup();
        if (!complete) LOGGER.warn("LuckPerms output cleanup is incomplete. Owned references are retained for retry.");
        return complete;
    }

    @Override
    public boolean shutdown() {
        closing = true;
        groupAvailability.clear();
        boolean complete = stopListening();
        complete &= cleanupTransientNodes();
        if (projectionContexts != null) {
            try { projectionContexts.close(); }
            catch (RuntimeException | LinkageError failure) {
                complete = false;
                LOGGER.warn("LuckPerms projection context cleanup is incomplete", failure);
            }
        }
        if (complete) api = null;
        return complete;
    }

    @Override
    public MutationResult addTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                                       String ownerKey) {
        if (api == null || closing || transientNodes == null) return MutationResult.UNAVAILABLE;
        try {
            return transientNodes.add(player, new NodeSpec(kind, value, contexts), ownerKey);
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.debug("Unable to add owned LuckPerms output", exception);
            return MutationResult.FAILED;
        }
    }

    @Override
    public MutationResult removeTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                                          String ownerKey) {
        if (transientNodes == null) return MutationResult.APPLIED;
        try {
            return transientNodes.remove(player, new NodeSpec(kind, value, contexts), ownerKey);
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.debug("Unable to remove owned LuckPerms output", exception);
            return MutationResult.FAILED;
        }
    }
}
