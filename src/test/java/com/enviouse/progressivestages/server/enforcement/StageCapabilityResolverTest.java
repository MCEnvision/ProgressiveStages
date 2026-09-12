package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.ProgressiveStagesAPI;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageCapabilities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.enviouse.progressivestages.common.stage.StageCapabilities.*;
import static org.junit.jupiter.api.Assertions.*;

class StageCapabilityResolverTest {
    @Test
    void draftReferencesIncludeBothDirectionsAndOnlyLoadEachGroupOnce() {
        var calls = new AtomicInteger();
        var stage = stage(true, true, true);
        var capabilities = StageCapabilityResolver.resolve(List.of(stage, stage), TeamProviderStatus.ABSENT,
            LuckPermsStatus.READY, group -> { calls.incrementAndGet(); return GroupStatus.MISSING; }, null, 14);
        assertEquals(2, calls.get());
        assertEquals(Map.of("chef", GroupStatus.MISSING, "seller", GroupStatus.MISSING), capabilities.configuredGroupStatus());
        assertEquals(14, capabilities.definitionRevision());
        assertTrue(capabilities.configuredCommandStatus().isEmpty());
        var diagnostics = ProgressiveStagesAPI.validateStageOptions(stage, "stages/chef/stage.toml", capabilities);
        assertTrue(diagnostics.stream().anyMatch(d -> d.field().equals("luckperms.inbound") && d.ruleId().orElse("").equals("rank") && d.code().equals("missing_group")));
        assertTrue(diagnostics.stream().anyMatch(d -> d.field().equals("luckperms.outbound") && d.ruleId().orElse("").equals("seller") && d.code().equals("missing_group")));
        assertTrue(diagnostics.stream().anyMatch(d -> d.code().equals("command_unobserved")));
        assertTrue(diagnostics.stream().allMatch(d -> d.severity().name().equals("WARNING")));
    }

    @Test
    void absentDisabledAndStartingProvidersDoNotInventMissingGroups() {
        for (var state : List.of(LuckPermsStatus.ABSENT, LuckPermsStatus.DISABLED, LuckPermsStatus.STARTING, LuckPermsStatus.FAILED)) {
            var stage = stage(true, true, true);
            var capabilities = StageCapabilityResolver.resolve(List.of(stage), TeamProviderStatus.DISABLED, state,
                group -> { fail("Unavailable provider queried"); return null; }, null, 1);
            assertEquals(GroupStatus.UNKNOWN, capabilities.configuredGroupStatus().get("chef"));
            var diagnostics = ProgressiveStagesAPI.validateStageOptions(stage, "chef.toml", capabilities);
            assertTrue(diagnostics.stream().anyMatch(d -> d.code().equals("provider_unavailable")));
            assertFalse(diagnostics.stream().anyMatch(d -> d.code().equals("missing_group")));
        }
    }

    @Test
    void commandOnlyStagesUseTheLiveDispatcherWithoutLuckPerms() {
        var dispatcher = new CommandDispatcher<String>();
        dispatcher.register(LiteralArgumentBuilder.<String>literal("home").executes(context -> 1));
        var stage = stage(false, false, false);
        var capabilities = StageCapabilityResolver.resolve(List.of(stage), TeamProviderStatus.ABSENT, LuckPermsStatus.ABSENT,
            ignored -> GroupStatus.UNKNOWN, dispatcher.getRoot(), 1);
        assertEquals(CommandStatus.RESOLVED, capabilities.configuredCommandStatus().get("home"));
        assertTrue(ProgressiveStagesAPI.validateStageOptions(stage, "chef.toml", capabilities).isEmpty());
        var missing = StageCapabilityResolver.resolve(List.of(stage), TeamProviderStatus.ABSENT, LuckPermsStatus.ABSENT,
            ignored -> GroupStatus.UNKNOWN, new CommandDispatcher<String>().getRoot(), 2);
        assertEquals(CommandStatus.MISSING, missing.configuredCommandStatus().get("home"));
        assertTrue(ProgressiveStagesAPI.validateStageOptions(stage, "chef.toml", missing).stream().anyMatch(d -> d.code().equals("missing_command")));
    }

    @Test
    void literalPathsThroughDistinctArgumentBranchesReportAmbiguity() {
        var dispatcher = new CommandDispatcher<String>();
        for (String argument : List.of("first", "second")) {
            dispatcher.register(LiteralArgumentBuilder.<String>literal("root")
                .then(RequiredArgumentBuilder.<String, String>argument(argument, StringArgumentType.word())
                    .then(LiteralArgumentBuilder.<String>literal("home").executes(context -> 1))));
        }
        var options = new LuckPermsStageOptions(false, true, null, List.of(), List.of(),
            List.of(new LuckPermsStageOptions.CommandPermissionRule("home", "root home", false)));
        var stage = StageDefinition.builder(StageId.of("chef")).teamStage(false).luckPerms(options).build();
        var capabilities = StageCapabilityResolver.resolve(List.of(stage), TeamProviderStatus.ABSENT, LuckPermsStatus.ABSENT,
            ignored -> GroupStatus.UNKNOWN, dispatcher.getRoot(), 1);
        assertEquals(CommandStatus.AMBIGUOUS, capabilities.configuredCommandStatus().get("root home"));
        assertTrue(ProgressiveStagesAPI.validateStageOptions(stage, "chef.toml", capabilities).stream().anyMatch(d -> d.code().equals("ambiguous_command")));
    }

    private static StageDefinition stage(boolean present, boolean enabled, boolean mappings) {
        var options = new LuckPermsStageOptions(present, enabled, null,
            mappings ? List.of(new LuckPermsStageOptions.InboundRule("rank", List.of("chef"), List.of(), null, Map.of())) : List.of(),
            mappings ? List.of(new LuckPermsStageOptions.OutboundRule("seller", LuckPermsStageOptions.OutboundKind.GROUP, "seller", Map.of()),
                new LuckPermsStageOptions.OutboundRule("chef", LuckPermsStageOptions.OutboundKind.GROUP, "chef", Map.of())) : List.of(),
            List.of(new LuckPermsStageOptions.CommandPermissionRule("home", "home", false)));
        return StageDefinition.builder(StageId.of("chef")).teamStage(false).luckPerms(options).build();
    }
}
