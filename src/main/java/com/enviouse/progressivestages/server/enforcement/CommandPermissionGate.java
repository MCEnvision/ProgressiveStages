package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageOrder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/** enforces configured stage command gates at the actual parsed command boundary. */
public final class CommandPermissionGate {
    private CommandPermissionGate() {}

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (event == null || event.getParseResults() == null) return;
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        ServerPlayer player;
        try {
            player = source.getPlayer();
        } catch (Exception ignored) {
            player = null;
        }
        if (player == null) return;
        List<String> path = new ArrayList<>();
        for (var node : event.getParseResults().getContext().getNodes()) {
            if (node.getNode() instanceof com.mojang.brigadier.tree.LiteralCommandNode<?> literal) {
                path.add(literal.getLiteral());
            }
        }
        if (path.isEmpty()) return;
        for (StageId stageId : StageOrder.getInstance().getOrderedStages()) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
            if (definition == null || definition.getLuckPerms().commandPermissions().isEmpty()) continue;
            if (!hasApplicableRule(definition.getLuckPerms().commandPermissions(), path)) continue;
            if (!com.enviouse.progressivestages.common.api.ProgressiveStagesAPI.hasStage(player, stageId)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("You need the stage " + stageId.getPath() + " to use this command."));
                InteractionCaptureManager.recordCommandPermission(player, stageId, String.join(" ", path), false, "command_denied");
                return;
            }
            InteractionCaptureManager.recordCommandPermission(player, stageId, String.join(" ", path), true, "reconciled");
        }
    }

    public static boolean isAllowed(ServerPlayer player, List<String> path) {
        if (player == null || path == null || path.isEmpty()) return true;
        for (StageId stageId : StageOrder.getInstance().getOrderedStages()) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
            if (definition != null && hasApplicableRule(definition.getLuckPerms().commandPermissions(), path)
                    && !com.enviouse.progressivestages.common.api.ProgressiveStagesAPI.hasStage(player, stageId)) return false;
        }
        return true;
    }

    private static boolean hasApplicableRule(List<LuckPermsStageOptions.CommandPermissionRule> rules,
                                             List<String> path) {
        for (var rule : rules) {
            if (matchesPath(rule.path(), path, rule.descendants())) return true;
        }
        return false;
    }

    public static boolean matchesPath(String configured, List<String> actual, boolean descendants) {
        if (configured == null || actual == null || actual.isEmpty()) return false;
        String[] expected = configured.split(" ");
        for (int index = 0; index < expected.length; index++) expected[index] = normalizeLiteral(expected[index]);
        if (actual.size() < expected.length) return false;
        if (descendants) return startsWith(actual, expected);
        return deepestLiteralPath(actual).equals(String.join(" ", expected));
    }

    private static boolean startsWith(List<String> actual, String[] expected) {
        for (int index = 0; index < expected.length; index++) {
            String observed = actual.get(index);
            observed = normalizeLiteral(observed);
            if (!expected[index].equals(observed)) return false;
        }
        return true;
    }

    private static String deepestLiteralPath(List<String> path) {
        return path.stream().map(CommandPermissionGate::normalizeLiteral)
            .collect(java.util.stream.Collectors.joining(" "));
    }

    private static String normalizeLiteral(String token) {
        int separator = token == null ? -1 : token.indexOf(':');
        return separator >= 0 ? token.substring(separator + 1) : token;
    }
}
