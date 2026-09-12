package com.enviouse.progressivestages.server.loader;

import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}
