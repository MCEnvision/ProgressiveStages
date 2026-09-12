package com.enviouse.progressivestages.server.enforcement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandPermissionGateTest {
    @Test
    void matchesLiteralDescendantsAndNamespaces() {
        assertTrue(CommandPermissionGate.matchesPath("sethome", List.of("sethome"), true));
        assertTrue(CommandPermissionGate.matchesPath("sethome", List.of("sethome", "name"), true));
        assertTrue(CommandPermissionGate.matchesPath("sethome", List.of("neoessentials:sethome"), false));
        assertTrue(CommandPermissionGate.matchesPath("neoessentials:sethome", List.of("sethome"), false));
        assertFalse(CommandPermissionGate.matchesPath("sethome", List.of("other"), true));
        assertFalse(CommandPermissionGate.matchesPath("foo", List.of("foo", "bar"), false));
    }
}
