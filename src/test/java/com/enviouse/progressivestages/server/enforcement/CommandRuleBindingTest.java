package com.enviouse.progressivestages.server.enforcement;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandRuleBindingTest {
    private final CommandDispatcher<String> dispatcher = new CommandDispatcher<>();

    @Test
    void argumentsDoNotBecomeLiteralSelectors() {
        dispatcher.register(literal("home").then(argument("name", StringArgumentType.word())
            .executes(context -> 1)));
        var binding = CommandRuleBinding.resolve(dispatcher.getRoot(), "home", false);
        assertTrue(binding.isResolved());
        assertTrue(binding.matches(context("home kitchen")));
        assertFalse(CommandRuleBinding.resolve(dispatcher.getRoot(), "home kitchen", true).isResolved());
    }

    @Test
    void exactLiteralExcludesChildEvenWhenItSharesTheCommand() {
        Command<String> action = context -> 1;
        dispatcher.register(literal("home").executes(action).then(literal("delete").executes(action)));
        assertFalse(CommandRuleBinding.resolve(dispatcher.getRoot(), "home", false)
            .matches(context("home delete")));
        assertTrue(CommandRuleBinding.resolve(dispatcher.getRoot(), "home", true)
            .matches(context("home delete")));
    }

    @Test
    void resolvesLiteralsAfterArguments() {
        dispatcher.register(literal("home").then(argument("name", StringArgumentType.word())
            .then(literal("delete").executes(context -> 1))));
        assertTrue(CommandRuleBinding.resolve(dispatcher.getRoot(), "home delete", false)
            .matches(context("home kitchen delete")));
    }

    @Test
    void redirectsKeepTheExecutedChildAndEffectiveSource() {
        dispatcher.register(literal("home").executes(context -> 1));
        dispatcher.register(literal("asplayer").redirect(dispatcher.getRoot(), context -> "player"));
        var parsed = context("asplayer home");
        var child = parsed.getChild().copyFor("player");
        assertTrue(CommandRuleBinding.resolve(dispatcher.getRoot(), "home", false).matches(child));
        assertEquals("player", child.getSource());
        assertFalse(CommandRuleBinding.resolve(dispatcher.getRoot(), "asplayer", true).matches(context("home")));
    }

    @Test
    void realAliasAndNamespacedExecutionBindingMatchWithoutNameGuessing() {
        Command<String> action = context -> 1;
        var target = dispatcher.register(literal("home").executes(action));
        dispatcher.register(literal("alias").redirect(target));
        dispatcher.register(literal("provider:home").executes(action));
        dispatcher.register(literal("unrelated:home").executes(context -> 2));
        dispatcher.register(literal("homecoming").executes(context -> 3));
        var binding = CommandRuleBinding.resolve(dispatcher.getRoot(), "home", false);
        assertTrue(binding.matches(context("provider:home")));
        assertFalse(binding.matches(context("unrelated:home")));
        assertFalse(binding.matches(context("homecoming")));
        assertTrue(CommandRuleBinding.resolve(dispatcher.getRoot(), "alias", false).matches(context("home")));
    }

    @Test
    void dispatcherReplacementRequiresNewBindings() {
        dispatcher.register(literal("home").executes(context -> 1));
        var old = CommandRuleBinding.resolve(dispatcher.getRoot(), "home", true);
        var replacement = new CommandDispatcher<String>();
        replacement.register(literal("home").executes(context -> 1));
        var current = replacement.parse("home", "player").getContext().build("home");
        assertFalse(old.matches(current));
        assertTrue(CommandRuleBinding.resolve(replacement.getRoot(), "home", true).matches(current));
    }

    @Test
    void missingPathsRemainUnresolved() {
        dispatcher.register(literal("other").executes(context -> 1));
        assertFalse(CommandRuleBinding.resolve(dispatcher.getRoot(), "home", true).isResolved());
        assertFalse(CommandRuleBinding.resolve(dispatcher.getRoot(), "", true).isResolved());
        assertFalse(CommandRuleBinding.resolve(dispatcher.getRoot(), "other missing", true).isResolved());
    }

    private CommandContext<String> context(String input) {
        return dispatcher.parse(input, "console").getContext().build(input);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<String> literal(String value) {
        return com.mojang.brigadier.builder.LiteralArgumentBuilder.literal(value);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<String, String> argument(
            String name, StringArgumentType type) {
        return com.mojang.brigadier.builder.RequiredArgumentBuilder.argument(name, type);
    }
}
