package com.enviouse.progressivestages.server.integration.luckperms;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;
import static org.junit.jupiter.api.Assertions.*;

class LuckPermsBridgeShutdownTest {
    @Test
    void shutdownWithdrawsOverlappingOutputAndPreservesExternalMembership() throws Exception {
        LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        UUID subject = UUID.randomUUID();
        var adapter = new InMemoryLuckPermsAdapter().member(subject, "external");
        bridge.setAdapterForTests(adapter);
        try {
            var node = new NodeSpec(NodeKind.GROUP, "chef", Map.of());
            assertTrue(tracker(bridge).reconcile(subject, Map.of("first", node, "second", node), adapter,
                (owner, adding, result) -> {}));
            assertTrue(adapter.snapshot(subject).groups().contains("chef"));
            bridge.shutdown();
            assertFalse(adapter.snapshot(subject).groups().contains("chef"));
            assertTrue(adapter.snapshot(subject).groups().contains("external"));
        } finally {
            bridge.shutdown();
        }
    }

    @Test
    void failedCleanupCannotBeDiscardedByReplacingTheAdapter() throws Exception {
        LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        UUID subject = UUID.randomUUID();
        var adapter = new InMemoryLuckPermsAdapter();
        bridge.setAdapterForTests(adapter);
        try {
            var node = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of());
            assertTrue(tracker(bridge).reconcile(subject, Map.of("chef", node), adapter,
                (owner, adding, result) -> {}));
            adapter.state(State.FAILED);
            bridge.shutdown();
            assertThrows(IllegalStateException.class, () -> bridge.setAdapterForTests(new InMemoryLuckPermsAdapter()));
            adapter.state(State.READY);
            bridge.shutdown();
            assertEquals(PermissionValue.UNDEFINED, adapter.permission(subject, "home.set"));
            bridge.setAdapterForTests(new InMemoryLuckPermsAdapter());
        } finally {
            adapter.state(State.READY);
            bridge.shutdown();
        }
    }

    private static OutboundNodeTracker tracker(LuckPermsBridge bridge) throws Exception {
        var field = LuckPermsBridge.class.getDeclaredField("outboundNodes");
        field.setAccessible(true);
        return (OutboundNodeTracker) field.get(bridge);
    }
}
