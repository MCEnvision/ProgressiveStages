package com.enviouse.progressivestages.server.integration.luckperms;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** deterministic adapter for headless tests and provider diagnostics. */
public final class InMemoryLuckPermsAdapter implements LuckPermsAdapter {
    private final Map<UUID, Set<String>> groups = new LinkedHashMap<>();
    private final Map<UUID, Set<String>> externalGroups = new LinkedHashMap<>();
    private final Map<UUID, Map<String, PermissionValue>> permissions = new LinkedHashMap<>();
    private final Map<UUID, Map<String, PermissionValue>> externalPermissions = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Set<String>>> transientNodes = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Map<String, String>>> transientContexts = new LinkedHashMap<>();
    private final Map<UUID, Map<String, String>> contexts = new LinkedHashMap<>();
    private final Set<String> knownGroups = new LinkedHashSet<>();
    private State state = State.READY;

    public InMemoryLuckPermsAdapter group(String name) { if (name != null) knownGroups.add(name); return this; }
    public InMemoryLuckPermsAdapter member(UUID player, String group) {
        group(group);
        externalGroups.computeIfAbsent(player, ignored -> new LinkedHashSet<>()).add(group);
        groups.computeIfAbsent(player, ignored -> new LinkedHashSet<>()).add(group);
        return this;
    }
    public InMemoryLuckPermsAdapter permission(UUID player, String node, PermissionValue value) {
        permissions.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(node, value);
        externalPermissions.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(node, value);
        return this;
    }
    public InMemoryLuckPermsAdapter context(UUID player, String key, String value) {
        contexts.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(key, value);
        return this;
    }
    public InMemoryLuckPermsAdapter state(State value) { state = value == null ? State.FAILED : value; return this; }

    @Override public State state() { return state; }
    @Override public SubjectSnapshot snapshot(UUID player) {
        if (state != State.READY) return SubjectSnapshot.unavailable();
        return new SubjectSnapshot(true, groups.getOrDefault(player, Set.of()),
            permissions.getOrDefault(player, Map.of()), contexts.getOrDefault(player, Map.of()));
    }
    @Override public PermissionValue permission(UUID player, String node) {
        return permissions.getOrDefault(player, Map.of()).getOrDefault(node, PermissionValue.UNDEFINED);
    }
    @Override public boolean groupExists(String group) { return knownGroups.contains(group); }
    @Override public void addTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts, String ownerKey) {
        String node = kind.name().toLowerCase(java.util.Locale.ROOT) + "|" + value;
        transientNodes.computeIfAbsent(player, ignored -> new LinkedHashMap<>())
            .computeIfAbsent(node, ignored -> new LinkedHashSet<>()).add(ownerKey);
        transientContexts.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(node,
            contexts == null ? Map.of() : Map.copyOf(contexts));
        if (kind == NodeKind.GROUP) {
            group(value);
            groups.computeIfAbsent(player, ignored -> new LinkedHashSet<>()).add(value);
        } else {
            permissions.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(value, PermissionValue.TRUE);
        }
    }
    @Override public void removeTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts, String ownerKey) {
        String node = kind.name().toLowerCase(java.util.Locale.ROOT) + "|" + value;
        Map<String, Set<String>> nodes = transientNodes.get(player);
        Set<String> owners = nodes == null ? null : nodes.get(node);
        if (owners == null) return;
        owners.remove(ownerKey);
        if (!owners.isEmpty()) return;
        nodes.remove(node);
        if (nodes.isEmpty()) transientNodes.remove(player);
        if (kind == NodeKind.GROUP) {
            if (!externalGroups.getOrDefault(player, Set.of()).contains(value)
                    && !hasTransientNode(player, NodeKind.GROUP, value)) {
                groups.getOrDefault(player, Set.of()).remove(value);
            }
        } else if (!externalPermissions.getOrDefault(player, Map.of()).containsKey(value)
                && !hasTransientNode(player, NodeKind.PERMISSION, value)) {
            permissions.getOrDefault(player, Map.of()).remove(value);
        }
    }

    private boolean hasTransientNode(UUID player, NodeKind kind, String value) {
        return transientNodes.getOrDefault(player, Map.of())
            .containsKey(kind.name().toLowerCase(java.util.Locale.ROOT) + "|" + value);
    }
}
