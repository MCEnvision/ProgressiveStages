package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.server.enforcement.CommandPermissionGate;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.tasks.ExecuteCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteCommandMixin {
    @Redirect(method = "execute(Lnet/minecraft/commands/ExecutionCommandSource;Lnet/minecraft/commands/execution/ExecutionContext;Lnet/minecraft/commands/execution/Frame;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;runExecutable(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;Z)I"), require = 1)
    private <S> int progressivestages$checkExecution(CommandContext<S> context, S actor,
                                                   ResultConsumer<S> consumer, boolean forked)
            throws CommandSyntaxException {
        if (actor instanceof CommandSourceStack source) {
            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> actual = (CommandContext<CommandSourceStack>) context;
            if (!CommandPermissionGate.checkExecution(source, actual)) {
                consumer.onCommandComplete(context.copyFor(actor), false, 0);
                return 0;
            }
        }
        return ContextChain.runExecutable(context, actor, consumer, forked);
    }
}
