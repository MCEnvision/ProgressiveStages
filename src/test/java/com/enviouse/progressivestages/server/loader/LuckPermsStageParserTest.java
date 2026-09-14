package com.enviouse.progressivestages.server.loader;

import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LuckPermsStageParserTest {
    @ParameterizedTest
    @CsvSource({
        "'enabled = \"false\"', luckperms.enabled",
        "'enabled = 1', luckperms.enabled",
        "'inbound_mode = \"forever\"', luckperms.inbound_mode"
    })
    void rejectsMalformedBridgeSettingsWithAnOwningField(String setting, String field) {
        var result = StageFileParser.parseText("[stage]\nid = \"chef\"\n[luckperms]\n" + setting,
            "chef.toml", "test", false);
        assertFalse(result.isSuccess());
        var diagnostic = result.getFieldDiagnostic("stages/chef.toml").orElseThrow();
        assertEquals(field, diagnostic.field());
        assertEquals("stages/chef.toml", diagnostic.file());
        assertTrue(diagnostic.ruleId().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "'groups = \"chef\"', groups",
        "'groups = [1]', groups",
        "'permissions = [true]', permissions",
        "'match = \"some\"', match",
        "'contexts = \"overworld\"', contexts"
    })
    void identifiesMalformedInboundValuesWithoutDroppingThem(String setting, String field) {
        var result = StageFileParser.parseText("[stage]\nid = \"chef\"\n[[luckperms.inbound]]\nid = \"chef_rank\"\n" + setting,
            "chef.toml", "test", false);
        assertFalse(result.isSuccess());
        var diagnostic = result.getFieldDiagnostic("stages/chef.toml").orElseThrow();
        assertEquals("luckperms.inbound[0]." + field, diagnostic.field());
        assertEquals("chef_rank", diagnostic.ruleId().orElseThrow());
    }

    @ParameterizedTest
    @CsvSource({"omitted, true", "true, true", "false, false"})
    void commandDescendantsDefaultAndExplicitValuesAgreeAcrossFormats(String setting, boolean expected) {
        String source = "[stage]\nid = \"chef\"\n[[command_permissions]]\nid = \"time_gate\"\npath = \"time\"\n"
            + (setting.equals("omitted") ? "" : "descendants = " + setting + " # Keep this choice.\n");
        var legacy = StageFileParser.parseText(source, "chef.toml", "test", false);
        var packaged = StagePackageParser.parseContents("test", "stage.toml", "[schema]\nversion = 4\n" + source,
            "rules.toml", "", "progression.toml", "");
        assertTrue(legacy.isSuccess(), legacy.getErrorMessage());
        assertTrue(packaged.isSuccess(), packaged.getErrorMessage());
        assertEquals(expected, legacy.getStageDefinition().getLuckPerms().commandPermissions().getFirst().descendants());
        assertEquals(expected, packaged.getStageDefinition().getLuckPerms().commandPermissions().getFirst().descendants());
    }

    @Test
    void packageAndLegacyValidationRetainTheSameCommandRowFailure() {
        String source = "[stage]\nid = \"chef\"\n[[command_permissions]]\nid = \"home_gate\"\npath = \"sethome\"\ndescendants = \"false\"\n";
        var legacy = StageFileParser.parseText(source, "chef.toml", "test", false);
        var packaged = StagePackageParser.parseContents("test", "stage.toml", "[schema]\nversion = 4\n" + source,
            "rules.toml", "# Existing rules remain separate.\n", "progression.toml", "");
        var expected = legacy.getFieldDiagnostic("stages/chef/stage.toml").orElseThrow();
        var actual = packaged.getFieldDiagnostic("stages/chef/stage.toml").orElseThrow();
        assertEquals("command_permissions[0].descendants", actual.field());
        assertEquals(expected.field(), actual.field());
        assertEquals(expected.ruleId(), actual.ruleId());
        assertEquals("home_gate", actual.ruleId().orElseThrow());
        assertEquals("invalid_type", actual.code());
    }

    @Test
    void ownershipConflictHasTheSameDiagnosticInThePublicApi() {
        String source = "[stage]\nid = \"chef\"\nscope = \"server\"\nteam_stage = false\n";
        var diagnostic = com.enviouse.progressivestages.common.api.ProgressiveStagesAPI.validateStageOptions(
            java.util.Map.of("stages/chef.toml", source), null).getFirst();
        assertEquals("stage.team_stage", diagnostic.field());
        assertEquals("server_override", diagnostic.code());
        assertEquals(com.enviouse.progressivestages.common.stage.FieldDiagnostic.Severity.ERROR, diagnostic.severity());
    }

    @Test
    void parsesInboundOutboundAndCommandRules() {
        String source = """
            [stage]
            id = "profession_chef"
            [luckperms]
            inbound_mode = "permanent"
            [[luckperms.inbound]]
            id = "chef_rank"
            groups = ["chef"]
            permissions = ["profession.chef"]
            match = "any"
            [luckperms.inbound.contexts]
            world = ["minecraft:overworld"]
            [[luckperms.outbound]]
            id = "home"
            kind = "permission"
            value = "neoessentials.teleport.home.set"
            [[command_permissions]]
            id = "home_gate"
            path = "sethome"
            descendants = true
            """;
        StageFileParser.ParseResult result = StageFileParser.parseText(source, "stage.toml", "test", true);
        assertTrue(result.isSuccess(), result.getErrorMessage());
        LuckPermsStageOptions options = result.getStageDefinition().getLuckPerms();
        assertTrue(options.present());
        assertEquals(LuckPermsStageOptions.InboundMode.PERMANENT, options.inboundMode());
        assertEquals(LuckPermsStageOptions.Match.ANY, options.inbound().getFirst().match());
        assertEquals("minecraft:overworld", options.inbound().getFirst().contexts().get("world").getFirst());
        assertEquals("neoessentials.teleport.home.set", options.outbound().getFirst().value());
        assertEquals("sethome", options.commandPermissions().getFirst().path());
    }

    @Test
    void rejectsDuplicateRowsAndReservedContexts() {
        String duplicate = """
            [stage]
            id = "chef"
            [luckperms]
            [[luckperms.inbound]]
            id = "same"
            groups = ["chef"]
            [[luckperms.inbound]]
            id = "same"
            permissions = ["profession.chef"]
            """;
        assertFalse(StageFileParser.parseText(duplicate, "stage.toml", "test", true).isSuccess());
        String reserved = """
            [stage]
            id = "chef"
            [luckperms]
            [[luckperms.inbound]]
            id = "same"
            groups = ["chef"]
            [luckperms.inbound.contexts]
            progressivestages_bridge = ["active"]
            """;
        assertFalse(StageFileParser.parseText(reserved, "stage.toml", "test", true).isSuccess());
        String unsafeKey = """
            [stage]
            id = "chef"
            [luckperms]
            [[luckperms.inbound]]
            id = "same"
            groups = ["chef"]
            [luckperms.inbound.contexts]
            "bad key" = ["active"]
            """;
        assertFalse(StageFileParser.parseText(unsafeKey, "stage.toml", "test", true).isSuccess());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inbound", "outbound"})
    void acceptsExactContextLimitsAndQuotedKeySemantics(String direction) {
        String contexts = "\"server.name\" = " + values(8) + "\nregion = " + values(8)
            + "\nworld = " + values(4) + "\n";
        var legacy = StageFileParser.parseText(contextSource(direction, contexts), "chef.toml", "test", false);
        var packaged = StagePackageParser.parseContents("test", "stage.toml",
            "[schema]\nversion = 4\n" + contextSource(direction, contexts), "rules.toml", "", "progression.toml", "");
        assertTrue(legacy.isSuccess(), legacy.getErrorMessage());
        assertTrue(packaged.isSuccess(), packaged.getErrorMessage());
        var options = legacy.getStageDefinition().getLuckPerms();
        var parsed = direction.equals("inbound") ? options.inbound().getFirst().contexts()
            : options.outbound().getFirst().contexts();
        assertEquals(8, parsed.get("server.name").size());
        assertEquals(256, parsed.values().stream().mapToInt(List::size).reduce(1, (left, right) -> left * right));
        assertEquals(parsed, direction.equals("inbound") ? packaged.getStageDefinition().getLuckPerms().inbound().getFirst().contexts()
            : packaged.getStageDefinition().getLuckPerms().outbound().getFirst().contexts());
        assertThrows(UnsupportedOperationException.class, () -> parsed.put("new", List.of("value")));
        String eightKeys = IntStream.range(0, 8).mapToObj(index -> "key" + index + " = [\"value\"]\n").collect(java.util.stream.Collectors.joining());
        assertTrue(StageFileParser.parseText(contextSource(direction, eightKeys), "chef.toml", "test", false).isSuccess());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inbound", "outbound"})
    void rejectsContextOverflowAndInvalidValuesAtTheContextField(String direction) {
        List<String> invalid = List.of(
            "world = " + values(9),
            "world = " + values(8) + "\nregion = " + values(8) + "\nserver = " + values(8),
            IntStream.range(0, 9).mapToObj(index -> "key" + index + " = [\"value\"]\n").collect(java.util.stream.Collectors.joining()),
            "world = []", "world = [\"\"]", "world = [true]", "\"\" = [\"value\"]",
            "world = [\"" + "x".repeat(257) + "\"]", "progressivestages_bridge = [\"active\"]");
        for (String contexts : invalid) {
            String source = contextSource(direction, contexts);
            var result = StageFileParser.parseText(source, "chef.toml", "test", false);
            assertFalse(result.isSuccess(), contexts);
            var diagnostic = result.getFieldDiagnostic("stages/chef.toml").orElseThrow();
            assertEquals("luckperms." + direction + "[0].contexts", diagnostic.field(), contexts);
            assertEquals("mapping", diagnostic.ruleId().orElseThrow());
            var packaged = StagePackageParser.parseContents("test", "stage.toml", "[schema]\nversion = 4\n" + source,
                "rules.toml", "", "progression.toml", "");
            assertEquals(diagnostic.field(), packaged.getFieldDiagnostic("stage.toml").orElseThrow().field());
        }
    }

    @Test
    void appliesTheSameContextBoundsToProgrammaticDefinitions() {
        Map<String, List<String>> valid = Map.of("world", List.of("one", "two"));
        assertEquals(valid, new LuckPermsStageOptions.InboundRule("mapping", List.of("chef"), List.of(),
            LuckPermsStageOptions.Match.ALL, valid).contexts());
        var tooMany = Map.of("world", IntStream.range(0, 9).mapToObj(Integer::toString).toList());
        assertThrows(IllegalArgumentException.class, () -> new LuckPermsStageOptions.InboundRule("mapping", List.of("chef"), List.of(),
            LuckPermsStageOptions.Match.ALL, tooMany));
        assertThrows(IllegalArgumentException.class, () -> new LuckPermsStageOptions.OutboundRule("mapping",
            LuckPermsStageOptions.OutboundKind.PERMISSION, "profession.chef", tooMany));
    }

    private static String contextSource(String direction, String contexts) {
        return "[stage]\nid = \"chef\"\n[[luckperms." + direction + "]]\nid = \"mapping\"\n"
            + (direction.equals("inbound") ? "groups = [\"chef\"]\n" : "kind = \"permission\"\nvalue = \"profession.chef\"\n")
            + "[luckperms." + direction + ".contexts]\n" + contexts;
    }

    private static String values(int count) {
        return IntStream.range(0, count).mapToObj(index -> "\"value" + index + "\"")
            .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }
}
