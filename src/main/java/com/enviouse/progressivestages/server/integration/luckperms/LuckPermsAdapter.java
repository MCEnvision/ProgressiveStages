package com.enviouse.progressivestages.server.integration.luckperms;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** small optional boundary used by the bridge so common startup never loads luckperms classes. */
public interface LuckPermsAdapter {
    enum State { ABSENT, STARTING, READY, FAILED }
    enum PermissionValue { TRUE, FALSE, UNDEFINED }
    enum NodeKind { GROUP, PERMISSION }
    enum MutationResult { APPLIED, UNAVAILABLE, CONFLICT, FAILED }
    record PermissionResult(boolean ready, PermissionValue value) {
        public static PermissionResult unavailable() {
            return new PermissionResult(false, PermissionValue.UNDEFINED);
        }
    }
    record OfflineResult(boolean stale, SubjectSnapshot snapshot) {}

    record NodeSpec(NodeKind kind, String value, Map<String, String> contexts) {
        public NodeSpec {
            java.util.Objects.requireNonNull(kind);
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Node value is required");
            Map<String, String> normalized = new java.util.LinkedHashMap<>();
            if (contexts != null) contexts.forEach((key, context) -> {
                String previous = normalized.put(key.toLowerCase(java.util.Locale.ROOT), context.toLowerCase(java.util.Locale.ROOT));
                if (previous != null) throw new IllegalArgumentException("Duplicate LuckPerms context key ignoring case");
            });
            contexts = Map.copyOf(normalized);
        }
    }

    record SubjectSnapshot(boolean ready, Set<String> groups,
                           Map<String, PermissionValue> permissions,
                           Map<String, Set<String>> contexts) {
        public SubjectSnapshot {
            groups = groups == null ? Set.of() : Set.copyOf(groups);
            permissions = permissions == null ? Map.of() : Map.copyOf(permissions);
            Map<String, Set<String>> copiedContexts = new java.util.LinkedHashMap<>();
            if (contexts != null) contexts.forEach((key, values) -> copiedContexts.put(key, Set.copyOf(values)));
            contexts = Map.copyOf(copiedContexts);
        }
        public static SubjectSnapshot unavailable() {
            return new SubjectSnapshot(false, Set.of(), Map.of(), Map.of());
        }
    }

    State state();
    SubjectSnapshot snapshot(UUID player);
    default PermissionValue permission(UUID player, String node) { return PermissionValue.UNDEFINED; }
    default PermissionResult permissionResult(UUID player, String node) {
        return state() == State.READY
            ? new PermissionResult(true, permission(player, node)) : PermissionResult.unavailable();
    }
    boolean groupExists(String group);
    default boolean subscribeChanges(java.util.function.Consumer<UUID> subjectChanged, Runnable allChanged) { return true; }
    default boolean stopListening() { return true; }
    default boolean requestOffline(UUID subject, Set<String> permissions, java.util.function.Consumer<UUID> completed) { return false; }
    default OfflineResult takeOffline(UUID subject) { return null; }
    default boolean isOfflineCurrent(UUID subject) { return false; }
    default void completeOffline(UUID subject) {}
    default void invalidateOffline(UUID subject) {}
    default void invalidateOffline() {}
    default long prepareProjection(UUID subject, Object target) { return 0; }
    default boolean publishProjection(UUID subject, long ticket) { return true; }
    default boolean publishProjection(UUID subject, long ticket, java.util.function.BooleanSupplier current) {
        if (!current.getAsBoolean()) return false;
        boolean published = publishProjection(subject, ticket);
        if (!published || !current.getAsBoolean()) {
            invalidateProjection(subject);
            return false;
        }
        return true;
    }

    default boolean invalidateProjection(UUID subject) { return true; }
    default boolean invalidateProjections() { return true; }
    default com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus groupStatus(String group) {
        return groupExists(group)
            ? com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus.PRESENT
            : com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus.UNKNOWN;
    }
    MutationResult addTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                      String ownerKey);
    MutationResult removeTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                         String ownerKey);
    default boolean cleanupTransientNodes() { return true; }
    default boolean shutdown() { return true; }
}
