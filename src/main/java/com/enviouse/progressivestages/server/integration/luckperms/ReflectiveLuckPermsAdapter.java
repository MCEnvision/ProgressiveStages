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

/** reflection-only luckperms adapter. it stays dormant when the optional mod is absent. */
final class ReflectiveLuckPermsAdapter implements LuckPermsAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Object api;
    private Method getUser;
    private Method saveUser;
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
            Object users = api.getClass().getMethod("getUserManager").invoke(api);
            getUser = users.getClass().getMethod("getUser", UUID.class);
            saveUser = users.getClass().getMethod("saveUser", Class.forName("net.luckperms.api.model.user.User"));
            queryOptionsClass = Class.forName("net.luckperms.api.query.QueryOptions");
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("luckperms integration is unavailable", exception);
            api = null;
        }
    }

    @Override
    public State state() {
        if (!ModList.get().isLoaded("luckperms")) return State.ABSENT;
        return api == null ? State.FAILED : State.READY;
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
    public void addTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                             String ownerKey) {
        mutate(player, kind, value, contexts, ownerKey, true);
    }

    @Override
    public void removeTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                                String ownerKey) {
        mutate(player, kind, value, contexts, ownerKey, false);
    }

    private void mutate(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                        String ownerKey, boolean add) {
        if (api == null || player == null || value == null || value.isBlank()) return;
        try {
            Object users = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = getUser.invoke(users, player);
            if (user == null) return;
            Class<?> nodeClass = Class.forName("net.luckperms.api.node.Node");
            Class<?> builderType = kind == NodeKind.GROUP
                ? Class.forName("net.luckperms.api.node.types.InheritanceNode") : nodeClass;
            Object builder = builderType.getMethod("builder", String.class).invoke(null, value);
            builder.getClass().getMethod("withContext", String.class, String.class)
                .invoke(builder, "progressivestages_bridge", "active");
            if (contexts != null) {
                for (var context : contexts.entrySet()) {
                    builder.getClass().getMethod("withContext", String.class, String.class)
                        .invoke(builder, context.getKey(), context.getValue());
                }
            }
            Object node = builder.getClass().getMethod("build").invoke(builder);
            Object data = user.getClass().getMethod("data").invoke(user);
            data.getClass().getMethod(add ? "add" : "remove", nodeClass).invoke(data, node);
            saveUser.invoke(users, user);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.debug("unable to update luckperms transient node", exception);
        }
    }
}
