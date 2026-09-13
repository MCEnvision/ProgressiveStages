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
    private final Set<Reference> unconfirmed = new HashSet<>();

    private record Reference(UUID subject, String owner) {}

    interface Observer {
        void mutation(String owner, boolean adding, MutationResult result);
    }

    boolean reconcile(UUID subject, Map<String, NodeSpec> desired, LuckPermsAdapter adapter,
                      Observer observer) {
        Map<String, NodeSpec> actual = owned.computeIfAbsent(subject, ignored -> new LinkedHashMap<>());
        boolean complete = true;
        for (var entry : Map.copyOf(actual).entrySet()) {
            if (entry.getValue().equals(desired.get(entry.getKey()))) continue;
            NodeSpec node = entry.getValue();
            MutationResult result = adapter.removeTransient(subject, node.kind(), node.value(),
                node.contexts(), entry.getKey());
            observer.mutation(entry.getKey(), false, result);
            if (result == MutationResult.APPLIED) {
                actual.remove(entry.getKey());
                unconfirmed.remove(new Reference(subject, entry.getKey()));
            }
            else complete = false;
        }
        for (var entry : desired.entrySet()) {
            NodeSpec node = entry.getValue();
            NodeSpec previous = actual.get(entry.getKey());
            if (previous != null && !previous.equals(node)) continue;
            actual.put(entry.getKey(), node);
            MutationResult result = adapter.addTransient(subject, node.kind(), node.value(),
                node.contexts(), entry.getKey());
            Reference reference = new Reference(subject, entry.getKey());
            if (previous == null || unconfirmed.contains(reference) || result != MutationResult.APPLIED) {
                observer.mutation(entry.getKey(), true, result);
            }
            if (result != MutationResult.APPLIED) {
                unconfirmed.add(reference);
                complete = false;
            } else unconfirmed.remove(reference);
        }
        if (actual.isEmpty()) owned.remove(subject);
        return complete;
    }

    boolean cleanup(LuckPermsAdapter adapter) {
        boolean complete = true;
        for (UUID subject : java.util.List.copyOf(owned.keySet())) {
            complete &= reconcile(subject, Map.of(), adapter, (owner, adding, result) -> {});
        }
        return complete;
    }

    boolean containsValue(UUID subject, String value) {
        return owned.getOrDefault(subject, Map.of()).values().stream().anyMatch(node -> node.value().equals(value));
    }
}
