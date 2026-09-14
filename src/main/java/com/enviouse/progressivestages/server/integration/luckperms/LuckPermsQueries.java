package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;

final class LuckPermsQueries {
    private final LuckPerms api;

    LuckPermsQueries(Object api) {
        this.api = (LuckPerms) api;
    }

    SubjectSnapshot snapshot(UUID subject) {
        User user = api.getUserManager().getUser(subject);
        if (user == null) return SubjectSnapshot.unavailable();
        QueryOptions query = independentQuery(user);
        var groups = new LinkedHashSet<String>();
        user.getInheritedGroups(query).forEach(group -> groups.add(group.getName()));
        return new SubjectSnapshot(true, groups, Map.of(), query.context().toMap());
    }

    PermissionResult permission(UUID subject, String permission) {
        User user = api.getUserManager().getUser(subject);
        if (user == null) return PermissionResult.unavailable();
        QueryOptions query = independentQuery(user);
        var value = user.getCachedData().getPermissionData(query).checkPermission(permission);
        return new PermissionResult(true, switch (value) {
            case TRUE -> PermissionValue.TRUE;
            case FALSE -> PermissionValue.FALSE;
            case UNDEFINED -> PermissionValue.UNDEFINED;
        });
    }

    SubjectSnapshot offlineSnapshot(User user, Set<String> permissions) {
        QueryOptions current = api.getContextManager().getStaticQueryOptions();
        var contexts = current.context().mutableCopy();
        contexts.removeAll("progressivestages_bridge");
        QueryOptions query = current.toBuilder().mode(QueryMode.CONTEXTUAL).context(contexts.immutableCopy()).build();
        var groups = new LinkedHashSet<String>();
        user.getInheritedGroups(query).forEach(group -> groups.add(group.getName()));
        Map<String, PermissionValue> values = new java.util.LinkedHashMap<>();
        var data = user.getCachedData().getPermissionData(query);
        for (String permission : permissions) {
            values.put(permission, switch (data.checkPermission(permission)) {
                case TRUE -> PermissionValue.TRUE;
                case FALSE -> PermissionValue.FALSE;
                case UNDEFINED -> PermissionValue.UNDEFINED;
            });
        }
        return new SubjectSnapshot(true, groups, values, query.context().toMap());
    }

    private QueryOptions independentQuery(User user) {
        var manager = api.getContextManager();
        QueryOptions current = manager.getQueryOptions(user).orElseGet(manager::getStaticQueryOptions);
        var contexts = current.context().mutableCopy();
        contexts.removeAll("progressivestages_bridge");
        return current.toBuilder().mode(QueryMode.CONTEXTUAL).context(contexts.immutableCopy()).build();
    }
}
