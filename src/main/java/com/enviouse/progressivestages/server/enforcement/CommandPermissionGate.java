package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.api.ProgressiveStagesAPI;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Enforces stage and native permissions for the effective command actor. */
public final class CommandPermissionGate {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static RootCommandNode<CommandSourceStack> dispatcherRoot;
    private static final Map<StageDefinition, List<CommandRuleBinding<CommandSourceStack>>> bindings =
        new WeakHashMap<>();

    private CommandPermissionGate() {}

    public static boolean checkExecution(CommandSourceStack source,
                                         CommandContext<CommandSourceStack> context) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return true;
        var root = source.getServer().getCommands().getDispatcher().getRoot();
        if (dispatcherRoot != root) {
            bindings.clear();
            dispatcherRoot = root;
        }
        for (StageId stageId : StageOrder.getInstance().getOrderedStages()) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
            if (definition == null || definition.getLuckPerms().commandPermissions().isEmpty()) continue;
            var resolved = bindings.computeIfAbsent(definition, value -> {
                var result = new ArrayList<CommandRuleBinding<CommandSourceStack>>();
                for (var rule : value.getLuckPerms().commandPermissions()) {
                    var binding = CommandRuleBinding.resolve(root, rule.path(), rule.descendants());
                    if (!binding.isResolved()) {
                        LOGGER.warn("Unresolved command rule {} in stage {}. Path {} remains inactive.",
                            rule.id(), stageId, rule.path());
                    }
                    result.add(binding);
                }
                return List.copyOf(result);
            });
            if (resolved.stream().noneMatch(binding -> binding.matches(context))) continue;
            boolean stageAllowed = ProgressiveStagesAPI.hasStage(player, stageId);
            boolean nativeAllowed = context.getRootNode().canUse(source)
                && context.getNodes().stream().allMatch(node -> node.getNode().canUse(source));
            String reason = !nativeAllowed ? "native_denied" : !stageAllowed ? "command_denied" : "reconciled";
            InteractionCaptureManager.recordCommandPermission(player, stageId, context, stageAllowed, nativeAllowed, reason);
            if (!nativeAllowed || !stageAllowed) {
                source.sendFailure(nativeAllowed
                    ? Component.translatable("progressivestages.command.stage_required", definition.getDisplayName())
                    : Component.translatable("progressivestages.command.native_denied"));
                return false;
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        bindings.clear();
        dispatcherRoot = null;
    }
}
