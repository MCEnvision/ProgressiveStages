package com.enviouse.progressivestages.server.integration.luckperms;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.MutationResult;
import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.NodeSpec;

final class OutboundNodeTracker {
    private final Map<UUID, Map<String, NodeSpec>> owned = new LinkedHashMap<>();
    private final Set<UUID> inFlight = new HashSet<>();
    private final Set<Reference> unconfirmed = new HashSet<>();

    private record Reference(UUID subject, String owner) {}

    interface Observer {
        void mutation(String owner, boolean adding, MutationResult result);
    }

    boolean reconcile(UUID subject, Map<String, NodeSpec> desired, LuckPermsAdapter adapter,
                      Observer observer) {
        return reconcile(subject, desired, adapter, observer, () -> true);
    }

    boolean reconcile(UUID subject, Map<String, NodeSpec> desired, LuckPermsAdapter adapter,
                      Observer observer, java.util.function.BooleanSupplier current) {
        if (!current.getAsBoolean() || !inFlight.add(subject)) return false;
        Map<String, NodeSpec> actual = owned.computeIfAbsent(subject, ignored -> new LinkedHashMap<>());
        try {
            boolean complete = true;
            for (var entry : Map.copyOf(actual).entrySet()) {
                if (!current.getAsBoolean()) return false;
                if (entry.getValue().equals(desired.get(entry.getKey()))) continue;
                NodeSpec node = entry.getValue();
                MutationResult result = adapter.removeTransient(subject, node.kind(), node.value(),
                    node.contexts(), entry.getKey());
                if (result == MutationResult.APPLIED) {
                    actual.remove(entry.getKey());
                    unconfirmed.remove(new Reference(subject, entry.getKey()));
                } else complete = false;
                observer.mutation(entry.getKey(), false, result);
                if (!current.getAsBoolean()) return false;
            }
            for (var entry : desired.entrySet()) {
                if (!current.getAsBoolean()) return false;
                NodeSpec node = entry.getValue();
                NodeSpec previous = actual.get(entry.getKey());
                if (previous != null && !previous.equals(node)) continue;
                Reference reference = new Reference(subject, entry.getKey());
                boolean pending = unconfirmed.contains(reference);
                actual.put(entry.getKey(), node);
                unconfirmed.add(reference);
                MutationResult result = adapter.addTransient(subject, node.kind(), node.value(),
                    node.contexts(), entry.getKey());
                if (result != MutationResult.APPLIED) complete = false;
                else unconfirmed.remove(reference);
                if (previous == null || pending || result != MutationResult.APPLIED) {
                    observer.mutation(entry.getKey(), true, result);
                }
                if (!current.getAsBoolean()) return false;
            }
            return complete && current.getAsBoolean();
        } finally {
            if (actual.isEmpty()) owned.remove(subject);
            inFlight.remove(subject);
        }
    }

    boolean cleanup(LuckPermsAdapter adapter) {
        boolean complete = true;
        for (UUID subject : java.util.List.copyOf(owned.keySet())) {
            complete &= reconcile(subject, Map.of(), adapter, (owner, adding, result) -> {});
        }
        return complete;
    }

}
