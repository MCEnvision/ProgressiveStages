package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.server.enforcement.CommandPermissionGate;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.BuildContexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BuildContexts.class)
public abstract class BuildContextsMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/commands/execution/CustomCommandExecutor;run(Ljava/lang/Object;Lcom/mojang/brigadier/context/ContextChain;Lnet/minecraft/commands/execution/ChainModifiers;Lnet/minecraft/commands/execution/ExecutionControl;)V"), require = 1)
    private <S> void progressivestages$checkCustomExecution(CustomCommandExecutor<S> command, S actor,
            ContextChain<S> chain, ChainModifiers modifiers, ExecutionControl<S> control) {
        if (actor instanceof CommandSourceStack source) {
            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> context = (CommandContext<CommandSourceStack>) chain.getTopContext();
            if (!CommandPermissionGate.checkExecution(source, context)) {
                source.callback().onFailure();
                if (modifiers.isReturn()) {
                    control.currentFrame().returnFailure();
                    control.currentFrame().discard();
                }
                return;
            }
        }
        command.run(actor, chain, modifiers, control);
    }
}
