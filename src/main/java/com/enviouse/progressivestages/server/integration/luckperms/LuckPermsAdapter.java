package com.enviouse.progressivestages.server.integration.luckperms;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** small optional boundary used by the bridge so common startup never loads luckperms classes. */
public interface LuckPermsAdapter {
    enum State { ABSENT, STARTING, READY, FAILED }
    enum PermissionValue { TRUE, FALSE, UNDEFINED }
    enum NodeKind { GROUP, PERMISSION }

    record SubjectSnapshot(boolean ready, Set<String> groups,
                           Map<String, PermissionValue> permissions,
                           Map<String, String> contexts) {
        public SubjectSnapshot {
            groups = groups == null ? Set.of() : Set.copyOf(groups);
            permissions = permissions == null ? Map.of() : Map.copyOf(permissions);
            contexts = contexts == null ? Map.of() : Map.copyOf(contexts);
        }
        public static SubjectSnapshot unavailable() {
            return new SubjectSnapshot(false, Set.of(), Map.of(), Map.of());
        }
    }

    State state();
    SubjectSnapshot snapshot(UUID player);
    default PermissionValue permission(UUID player, String node) { return PermissionValue.UNDEFINED; }
    boolean groupExists(String group);
    void addTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                      String ownerKey);
    void removeTransient(UUID player, NodeKind kind, String value, Map<String, String> contexts,
                         String ownerKey);
    default void shutdown() {}
}
