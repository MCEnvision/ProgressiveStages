package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;
import static org.junit.jupiter.api.Assertions.*;

class LuckPermsInboundQueryTest {
    private static final UUID SUBJECT = UUID.randomUUID();

    @Test
    void independentMembershipRemainsEligibleWhenTheSameGroupIsAlsoAnOutput() throws Exception {
        LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        var adapter = new InMemoryLuckPermsAdapter().member(SUBJECT, "chef");
        bridge.setAdapterForTests(adapter);
        try {
            var field = LuckPermsBridge.class.getDeclaredField("outboundNodes");
            field.setAccessible(true);
            var tracker = (OutboundNodeTracker) field.get(bridge);
            assertTrue(tracker.reconcile(SUBJECT,
                Map.of("profession|rank|0", new NodeSpec(NodeKind.GROUP, "chef", Map.of())), adapter,
                (owner, adding, result) -> {}));
            assertTrue(matches(bridge, row(List.of("chef"), List.of(), Map.of()), adapter.snapshot(SUBJECT)));
        } finally {
            bridge.shutdown();
        }
    }

    @Test
    void bridgeOnlyGroupsAndPermissionsAreNotIndependentEligibility() throws Exception {
        LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        var adapter = new InMemoryLuckPermsAdapter();
        bridge.setAdapterForTests(adapter);
        try {
            adapter.addTransient(SUBJECT, NodeKind.GROUP, "chef", Map.of(), "rank");
            adapter.addTransient(SUBJECT, NodeKind.PERMISSION, "professions.chef", Map.of(), "permission");
            assertTrue(adapter.effectiveSnapshot(SUBJECT).groups().contains("chef"));
            assertEquals(PermissionValue.TRUE, adapter.effectivePermission(SUBJECT, "professions.chef"));
            assertFalse(matches(bridge, row(List.of("chef"), List.of(), Map.of()), adapter.snapshot(SUBJECT)));
            var permission = row(List.of(), List.of("professions.chef"), Map.of());
            assertFalse(matches(bridge, permission, adapter.snapshot(SUBJECT)));
            adapter.permission(SUBJECT, "professions.chef", PermissionValue.TRUE);
            assertTrue(matches(bridge, permission, adapter.snapshot(SUBJECT)));
            adapter.permission(SUBJECT, "professions.chef", PermissionValue.FALSE);
            assertFalse(matches(bridge, permission, adapter.snapshot(SUBJECT)));
        } finally {
            bridge.shutdown();
        }
    }

    @Test
    void contextsMatchAnyValueWithinAKeyAndRequireEveryConfiguredKey() throws Exception {
        LuckPermsBridge bridge = LuckPermsBridge.getInstance();
        var adapter = new InMemoryLuckPermsAdapter().member(SUBJECT, "chef");
        bridge.setAdapterForTests(adapter);
        try {
            var snapshot = new SubjectSnapshot(true, Set.of("chef"), Map.of(),
                Map.of("region", Set.of("town", "market"), "world", Set.of("overworld")));
            assertTrue(matches(bridge, row(List.of("chef"), List.of(),
                Map.of("region", List.of("market", "outpost"), "world", List.of("overworld"))), snapshot));
            assertTrue(matches(bridge, row(List.of("chef"), List.of(),
                Map.of("REGION", List.of("Market"), "WORLD", List.of("OverWorld"))), snapshot));
            assertFalse(matches(bridge, row(List.of("chef"), List.of(),
                Map.of("region", List.of("market"), "world", List.of("nether"))), snapshot));
            assertFalse(matches(bridge, row(List.of("chef"), List.of(),
                Map.of("server", List.of("professions"))), snapshot));
        } finally {
            bridge.shutdown();
        }
    }

    private static LuckPermsStageOptions.InboundRule row(List<String> groups, List<String> permissions,
                                                         Map<String, List<String>> contexts) {
        return new LuckPermsStageOptions.InboundRule("chef", groups, permissions,
            LuckPermsStageOptions.Match.ALL, contexts);
    }

    private static boolean matches(LuckPermsBridge bridge, LuckPermsStageOptions.InboundRule row,
                                    SubjectSnapshot snapshot) throws Exception {
        var method = LuckPermsBridge.class.getDeclaredMethod("matches", UUID.class,
            LuckPermsStageOptions.InboundRule.class, SubjectSnapshot.class);
        method.setAccessible(true);
        return (boolean) method.invoke(bridge, SUBJECT, row, snapshot);
    }
}
