package com.enviouse.progressivestages.server.integration.luckperms;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** deterministic adapter for headless tests and provider diagnostics. */
public final class InMemoryLuckPermsAdapter implements LuckPermsAdapter {
    private final Map<UUID, Set<String>> externalGroups = new LinkedHashMap<>();
    private final Map<UUID, Map<String, PermissionValue>> externalPermissions = new LinkedHashMap<>();
    private final Map<UUID, Map<NodeSpec, Set<String>>> transientNodes = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Set<String>>> contexts = new LinkedHashMap<>();
    private final Set<String> knownGroups = new LinkedHashSet<>();
    private State state = State.READY;

    public InMemoryLuckPermsAdapter group(String name) { if (name != null) knownGroups.add(name); return this; }
    public InMemoryLuckPermsAdapter member(UUID player, String group) {
        group(group);
        externalGroups.computeIfAbsent(player, ignored -> new LinkedHashSet<>()).add(group);
        return this;
    }
    public InMemoryLuckPermsAdapter permission(UUID player, String node, PermissionValue value) {
        externalPermissions.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(node, value);
        return this;
    }
    public InMemoryLuckPermsAdapter context(UUID player, String key, String value) {
        contexts.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(key, Set.of(value));
        return this;
    }
    public InMemoryLuckPermsAdapter state(State value) { state = value == null ? State.FAILED : value; return this; }

    @Override public State state() { return state; }
    @Override public SubjectSnapshot snapshot(UUID player) {
        if (state != State.READY) return SubjectSnapshot.unavailable();
        return new SubjectSnapshot(true, externalGroups.getOrDefault(player, Set.of()),
            externalPermissions.getOrDefault(player, Map.of()), contexts.getOrDefault(player, Map.of()));
    }
    public SubjectSnapshot effectiveSnapshot(UUID player) {
        if (state != State.READY) return SubjectSnapshot.unavailable();
        Set<String> groups = new LinkedHashSet<>(externalGroups.getOrDefault(player, Set.of()));
        Map<String, PermissionValue> permissions = new LinkedHashMap<>(externalPermissions.getOrDefault(player, Map.of()));
        for (NodeSpec node : transientNodes.getOrDefault(player, Map.of()).keySet()) {
            if (!contextMatches(player, node)) continue;
            if (node.kind() == NodeKind.GROUP) groups.add(node.value());
            else if (permissions.get(node.value()) != PermissionValue.FALSE) permissions.put(node.value(), PermissionValue.TRUE);
        }
        return new SubjectSnapshot(true, groups, permissions, contexts.getOrDefault(player, Map.of()));
    }
    @Override public PermissionValue permission(UUID player, String node) {
        return snapshot(player).permissions().getOrDefault(node, PermissionValue.UNDEFINED);
    }
    public PermissionValue effectivePermission(UUID player, String node) {
        return effectiveSnapshot(player).permissions().getOrDefault(node, PermissionValue.UNDEFINED);
    }
    @Override public boolean groupExists(String group) { return knownGroups.contains(group); }
    @Override public MutationResult addTransient(UUID player, NodeKind kind, String value,
                                                  Map<String, String> contexts, String ownerKey) {
        if (state != State.READY) return MutationResult.UNAVAILABLE;
        transientNodes.computeIfAbsent(player, ignored -> new LinkedHashMap<>())
            .computeIfAbsent(new NodeSpec(kind, value, contexts), ignored -> new LinkedHashSet<>()).add(ownerKey);
        return MutationResult.APPLIED;
    }
    @Override public MutationResult removeTransient(UUID player, NodeKind kind, String value,
                                                     Map<String, String> contexts, String ownerKey) {
        Map<NodeSpec, Set<String>> nodes = transientNodes.get(player);
        NodeSpec node = new NodeSpec(kind, value, contexts);
        Set<String> owners = nodes == null ? null : nodes.get(node);
        if (owners == null || !owners.contains(ownerKey)) return MutationResult.APPLIED;
        if (state != State.READY) return MutationResult.UNAVAILABLE;
        owners.remove(ownerKey);
        if (owners.isEmpty()) nodes.remove(node);
        if (nodes.isEmpty()) transientNodes.remove(player);
        return MutationResult.APPLIED;
    }

    @Override public boolean cleanupTransientNodes() {
        if (!transientNodes.isEmpty() && state != State.READY) return false;
        transientNodes.clear();
        return true;
    }

    private boolean contextMatches(UUID player, NodeSpec node) {
        return node.contexts().entrySet().stream().allMatch(entry ->
            contexts.getOrDefault(player, Map.of()).getOrDefault(entry.getKey(), Set.of()).contains(entry.getValue()));
    }
}
