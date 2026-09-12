package com.enviouse.progressivestages.server.loader;

import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LuckPermsStageParserTest {
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
