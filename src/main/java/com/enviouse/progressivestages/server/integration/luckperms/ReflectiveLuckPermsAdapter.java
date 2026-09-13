package com.enviouse.progressivestages.server.integration.luckperms;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** optional luckperms adapter with guarded provider access. */
final class ReflectiveLuckPermsAdapter implements LuckPermsAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Object api;
    private final GroupAvailabilityCache groupAvailability = new GroupAvailabilityCache();
    private Method getUser;
    private LuckPermsTransientNodes transientNodes;
    private boolean closing;
    private Class<?> queryOptionsClass;

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
            getUser = Class.forName("net.luckperms.api.model.user.UserManager").getMethod("getUser", UUID.class);
            transientNodes = new LuckPermsTransientNodes(api);
            queryOptionsClass = Class.forName("net.luckperms.api.query.QueryOptions");
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
        if (api == null || player == null) return SubjectSnapshot.unavailable();
        try {
            Object user = getUser.invoke(api.getClass().getMethod("getUserManager").invoke(api), player);
            if (user == null) return SubjectSnapshot.unavailable();
            Object query = queryOptionsClass.getMethod("nonContextual").invoke(null);
            Object cached = user.getClass().getMethod("getCachedData").invoke(user);
            Object permissions = cached.getClass().getMethod("getPermissionData", queryOptionsClass).invoke(cached, query);
            Set<String> groups = new LinkedHashSet<>();
            Object inherited = user.getClass().getMethod("getInheritedGroups", queryOptionsClass).invoke(user, query);
            if (inherited instanceof Collection<?> collection) {
                for (Object group : collection) {
                    Object name = group.getClass().getMethod("getName").invoke(group);
                    if (name != null) groups.add(String.valueOf(name));
                }
            }
            Map<String, PermissionValue> values = new LinkedHashMap<>();
            Map<String, String> contexts = new LinkedHashMap<>();
            Object contextSet = queryOptionsClass.getMethod("context").invoke(query);
            Object flattened = contextSet.getClass().getMethod("toFlattenedMap").invoke(contextSet);
            if (flattened instanceof Map<?, ?> map) {
                for (var entry : map.entrySet()) contexts.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            return new SubjectSnapshot(true, groups, values, contexts);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.debug("unable to read luckperms user", exception);
            return SubjectSnapshot.unavailable();
        }
    }

    @Override
    public PermissionValue permission(UUID player, String node) {
        if (api == null || player == null || node == null || node.isBlank()) return PermissionValue.UNDEFINED;
        try {
            Object users = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = getUser.invoke(users, player);
            if (user == null) return PermissionValue.UNDEFINED;
            Object query = queryOptionsClass.getMethod("nonContextual").invoke(null);
            Object cached = user.getClass().getMethod("getCachedData").invoke(user);
            Object permissions = cached.getClass().getMethod("getPermissionData", queryOptionsClass).invoke(cached, query);
            Object result = permissions.getClass().getMethod("checkPermission", String.class).invoke(permissions, node);
            String value = String.valueOf(result).toUpperCase(java.util.Locale.ROOT);
            if (value.contains("TRUE")) return PermissionValue.TRUE;
            if (value.contains("FALSE")) return PermissionValue.FALSE;
            return PermissionValue.UNDEFINED;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return PermissionValue.UNDEFINED;
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
