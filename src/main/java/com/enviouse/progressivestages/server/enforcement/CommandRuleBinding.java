package com.enviouse.progressivestages.server.enforcement;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Binds literal configuration to one registered command dispatcher. */
public final class CommandRuleBinding<S> {
    private final Set<CommandNode<S>> executions = identitySet();
    private final Set<CommandNode<S>> excluded = identitySet();
    private final Set<CommandNode<S>> registered = identitySet();
    private final Set<Command<S>> commands = identitySet();
    private boolean resolved;
    private boolean ambiguous;

    private CommandRuleBinding() {}

    public static <S> CommandRuleBinding<S> resolve(RootCommandNode<S> root, String path,
                                                   boolean descendants) {
        var result = new CommandRuleBinding<S>();
        if (path == null || path.isBlank()) return result;
        var targets = CommandRuleBinding.<CommandNode<S>>identitySet();
        resolvePath(root, path.split(" ", -1), 0, new IdentityHashMap<>(), targets);
        result.resolved = !targets.isEmpty();
        result.ambiguous = targets.size() > 1;
        collectRegistered(root, result.registered);
        for (var target : targets) {
            result.collectExecutions(target, descendants, identitySet());
        }
        return result;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public boolean matches(CommandContext<S> context) {
        if (context == null || context.getCommand() == null) return false;
        List<com.mojang.brigadier.context.ParsedCommandNode<S>> nodes = context.getNodes();
        CommandNode<S> terminal = nodes.isEmpty() ? context.getRootNode()
            : nodes.get(nodes.size() - 1).getNode();
        if (executions.contains(terminal)) return true;
        if (excluded.contains(terminal) || !registered.contains(terminal)) return false;
        return commands.contains(context.getCommand());
    }

    private void collectExecutions(CommandNode<S> node, boolean descendants,
                                   Set<CommandNode<S>> visited) {
        if (!visited.add(node)) return;
        if (node.getCommand() != null) {
            executions.add(node);
            commands.add(node.getCommand());
        }
        for (var child : node.getChildren()) {
            if (descendants || child instanceof ArgumentCommandNode<?, ?>) {
                collectExecutions(child, descendants, visited);
            } else {
                collectRegistered(child, excluded);
            }
        }
        var redirect = node.getRedirect();
        // Root redirects delegate to a separately parsed command rather than an alias subtree.
        if (redirect != null && !(redirect instanceof RootCommandNode<?>)) {
            collectExecutions(redirect, descendants, visited);
        }
    }

    private static <S> void resolvePath(CommandNode<S> node, String[] tokens, int offset,
                                       Map<CommandNode<S>, Set<Integer>> visited, Set<CommandNode<S>> targets) {
        if (!visited.computeIfAbsent(node, ignored -> new HashSet<>()).add(offset)) return;
        if (offset == tokens.length) {
            targets.add(node);
            return;
        }
        for (var child : node.getChildren()) {
            if (child instanceof LiteralCommandNode<S> literal && literal.getLiteral().equals(tokens[offset])) {
                resolvePath(child, tokens, offset + 1, visited, targets);
            } else if (child instanceof ArgumentCommandNode<?, ?>) {
                resolvePath(child, tokens, offset, visited, targets);
            }
        }
        var redirect = node.getRedirect();
        if (redirect != null && !(redirect instanceof RootCommandNode<?>)) {
            resolvePath(redirect, tokens, offset, visited, targets);
        }
    }

    private static <S> void collectRegistered(CommandNode<S> start, Set<CommandNode<S>> result) {
        var pending = new ArrayList<CommandNode<S>>();
        pending.add(start);
        for (int index = 0; index < pending.size(); index++) {
            var node = pending.get(index);
            if (!result.add(node)) continue;
            pending.addAll(node.getChildren());
        }
    }

    private static <T> Set<T> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

}
