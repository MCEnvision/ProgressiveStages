package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.metadata.NodeMetadataKey;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.MutationResult;
import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.NodeSpec;

final class LuckPermsTransientNodes {
    private static final NodeMetadataKey<UUID> OWNER = NodeMetadataKey.of("progressivestages_owner", UUID.class);
    private final LuckPerms api;
    private final Map<UUID, Map<NodeSpec, Contribution>> contributions = new LinkedHashMap<>();

    LuckPermsTransientNodes(Object api) {
        this.api = (LuckPerms) api;
    }

    MutationResult add(UUID subject, NodeSpec spec, String owner) {
        if (subject == null || owner == null || owner.isBlank()) return MutationResult.FAILED;
        var user = api.getUserManager().getUser(subject);
        if (user == null) return MutationResult.UNAVAILABLE;
        NodeMap data = user.transientData();
        var entries = contributions.computeIfAbsent(subject, ignored -> new LinkedHashMap<>());
        Contribution contribution = entries.get(spec);
        if (contribution != null && contains(data, contribution)) {
            contribution.owners.add(owner);
            return MutationResult.APPLIED;
        }
        if (contribution == null) {
            UUID token = UUID.randomUUID();
            NodeBuilder<?, ?> builder = spec.kind() == LuckPermsAdapter.NodeKind.GROUP
                ? api.getNodeBuilderRegistry().forInheritance().group(spec.value())
                : api.getNodeBuilderRegistry().forPermission().permission(spec.value());
            builder.withContext("progressivestages_bridge", "active");
            spec.contexts().forEach(builder::withContext);
            contribution = new Contribution(builder.withMetadata(OWNER, token).build(), token);
        }
        Node requested = contribution.node;
        if (data.toCollection().stream().anyMatch(node -> node.equals(requested))) {
            if (entries.isEmpty()) contributions.remove(subject);
            return MutationResult.CONFLICT;
        }
        contribution.owners.add(owner);
        entries.put(spec, contribution);
        boolean applied = data.add(requested).wasSuccessful();
        return applied && contains(data, contribution) ? MutationResult.APPLIED : MutationResult.FAILED;
    }

    MutationResult remove(UUID subject, NodeSpec spec, String owner) {
        var entries = contributions.get(subject);
        Contribution contribution = entries == null ? null : entries.get(spec);
        if (contribution == null || !contribution.owners.contains(owner)) return MutationResult.APPLIED;
        if (contribution.owners.size() > 1) {
            contribution.owners.remove(owner);
            return MutationResult.APPLIED;
        }
        var user = api.getUserManager().getUser(subject);
        if (user == null) return MutationResult.UNAVAILABLE;
        NodeMap data = user.transientData();
        data.clear(node -> belongsTo(node, contribution));
        if (contains(data, contribution)) return MutationResult.FAILED;
        entries.remove(spec);
        if (entries.isEmpty()) contributions.remove(subject);
        return MutationResult.APPLIED;
    }

    boolean cleanup() {
        boolean complete = true;
        for (var subject : Map.copyOf(contributions).entrySet()) {
            for (var entry : Map.copyOf(subject.getValue()).entrySet()) {
                for (String owner : Set.copyOf(entry.getValue().owners)) {
                    try {
                        complete &= remove(subject.getKey(), entry.getKey(), owner) == MutationResult.APPLIED;
                    } catch (RuntimeException | LinkageError failure) {
                        complete = false;
                    }
                }
            }
        }
        return complete;
    }

    private static boolean contains(NodeMap data, Contribution contribution) {
        return data.toCollection().stream().anyMatch(node -> belongsTo(node, contribution));
    }

    private static boolean belongsTo(Node node, Contribution contribution) {
        return node.equals(contribution.node) && node.getMetadata(OWNER).filter(contribution.token::equals).isPresent();
    }

    private static final class Contribution {
        private final Node node;
        private final UUID token;
        private final Set<String> owners = new LinkedHashSet<>();

        private Contribution(Node node, UUID token) {
            this.node = node;
            this.token = token;
        }
    }
}
