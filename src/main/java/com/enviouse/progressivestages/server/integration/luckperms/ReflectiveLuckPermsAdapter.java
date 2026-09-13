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
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("luckperms integration is unavailable", exception);
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
    public boolean cleanupTransientNodes() {
        boolean complete = transientNodes == null || transientNodes.cleanup();
        if (!complete) LOGGER.warn("LuckPerms output cleanup is incomplete. Owned references are retained for retry.");
        return complete;
    }

    @Override
    public void shutdown() {
        closing = true;
        groupAvailability.clear();
        if (cleanupTransientNodes()) api = null;
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
