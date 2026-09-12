package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageCapabilities;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class StageCapabilityResolver {
    private StageCapabilityResolver() {}

    public static <S> StageCapabilities resolve(Collection<StageDefinition> definitions,
            StageCapabilities.TeamProviderStatus team, StageCapabilities.LuckPermsStatus luckPerms,
            Function<String, StageCapabilities.GroupStatus> groupLookup, RootCommandNode<S> root,
            long definitionRevision) {
        Map<String, StageCapabilities.GroupStatus> groups = new LinkedHashMap<>();
        Map<String, StageCapabilities.CommandStatus> commands = new LinkedHashMap<>();
        Function<String, StageCapabilities.GroupStatus> lookup = luckPerms == StageCapabilities.LuckPermsStatus.READY
            ? groupLookup : ignored -> StageCapabilities.GroupStatus.UNKNOWN;
        for (StageDefinition definition : definitions) {
            for (var row : definition.getLuckPerms().inbound()) {
                row.groups().forEach(group -> groups.computeIfAbsent(group, lookup));
            }
            for (var row : definition.getLuckPerms().outbound()) {
                if (row.kind() == LuckPermsStageOptions.OutboundKind.GROUP) {
                    groups.computeIfAbsent(row.value(), lookup);
                }
            }
            if (root == null) continue;
            for (var row : definition.getLuckPerms().commandPermissions()) {
                commands.computeIfAbsent(row.path(), path -> {
                    var binding = CommandRuleBinding.resolve(root, path, true);
                    return !binding.isResolved() ? StageCapabilities.CommandStatus.MISSING
                        : binding.isAmbiguous() ? StageCapabilities.CommandStatus.AMBIGUOUS
                        : StageCapabilities.CommandStatus.RESOLVED;
                });
            }
        }
        return new StageCapabilities(team, luckPerms,
            List.of("inherit", "personal", "team", "server"), List.of("synchronized", "permanent"),
            groups, commands, definitionRevision);
    }
}
