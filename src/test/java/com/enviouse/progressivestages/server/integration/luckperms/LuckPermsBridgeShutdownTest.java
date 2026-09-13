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
            assertTrue(adapter.effectiveSnapshot(subject).groups().contains("chef"));
            bridge.shutdown();
            assertFalse(adapter.effectiveSnapshot(subject).groups().contains("chef"));
            assertTrue(adapter.effectiveSnapshot(subject).groups().contains("external"));
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
            assertEquals(PermissionValue.UNDEFINED, adapter.effectivePermission(subject, "home.set"));
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

    @Test
    void failedCalculatorShutdownRetainsTheAdapterAfterNodeCleanup() throws Exception {
        LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        boolean[] allowShutdown = {false};
        boolean[] invalidated = {false};
        var adapter = new LuckPermsAdapter() {
            @Override public State state() { return State.READY; }
            @Override public SubjectSnapshot snapshot(UUID subject) { return SubjectSnapshot.unavailable(); }
            @Override public boolean groupExists(String group) { return false; }
            @Override public boolean invalidateProjections() { invalidated[0] = true; return true; }
            @Override public MutationResult addTransient(UUID subject, NodeKind kind, String value,
                                                          Map<String, String> contexts, String owner) {
                return MutationResult.APPLIED;
            }
            @Override public MutationResult removeTransient(UUID subject, NodeKind kind, String value,
                                                             Map<String, String> contexts, String owner) {
                assertTrue(invalidated[0], "Contexts must be invalid before node cleanup");
                return MutationResult.APPLIED;
            }
            @Override public boolean shutdown() { return allowShutdown[0]; }
        };
        bridge.setAdapterForTests(adapter);
        try {
            assertTrue(tracker(bridge).reconcile(new UUID(0, 61), Map.of("home",
                new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of())), adapter, (owner, adding, result) -> {}));
            bridge.shutdown();
            assertTrue(invalidated[0]);
            assertThrows(IllegalStateException.class, () -> bridge.setAdapterForTests(new InMemoryLuckPermsAdapter()));
            allowShutdown[0] = true;
            bridge.shutdown();
            bridge.setAdapterForTests(new InMemoryLuckPermsAdapter());
        } finally {
            allowShutdown[0] = true;
            bridge.shutdown();
        }
    }
}
