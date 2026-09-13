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

    @Test
    void eligibilityHistoryIgnoresOrderingButDistinguishesContextsAndMissingObservations() {
        var first = row(List.of("chef", "baker"), List.of("cook", "serve"), Map.of("world", List.of("overworld", "nether")));
        var reordered = row(List.of("baker", "chef"), List.of("serve", "cook"), Map.of("world", List.of("nether", "overworld")));
        var positive = new SubjectSnapshot(true, Set.of("chef", "baker"), Map.of("cook", PermissionValue.TRUE,
            "serve", PermissionValue.TRUE), Map.of("world", Set.of("overworld")));
        var observation = PermissionEligibility.observe(first, positive);
        assertTrue(observation.eligible());
        assertEquals(observation, PermissionEligibility.observe(reordered, positive));
        var lost = new SubjectSnapshot(true, Set.of(), Map.of("cook", PermissionValue.FALSE,
            "serve", PermissionValue.UNDEFINED), positive.contexts());
        assertFalse(PermissionEligibility.observe(first, lost).eligible());
        assertEquals(observation.fingerprint(), PermissionEligibility.observe(first, lost).fingerprint());
        var otherWorld = new SubjectSnapshot(true, lost.groups(), lost.permissions(), Map.of("world", Set.of("nether")));
        assertNotEquals(observation.fingerprint(), PermissionEligibility.observe(first, otherWorld).fingerprint());
        assertNull(PermissionEligibility.observe(first, SubjectSnapshot.unavailable()));
        assertNull(PermissionEligibility.observe(first, new SubjectSnapshot(true, Set.of(), Map.of(), Map.of())));
    }

    @Test
    void onlineCollectionReadsEachPermissionOnceAndDiscardsPartialProviderResults() {
        var stage = com.enviouse.progressivestages.common.api.StageId.parse("test:online_input");
        var first = row(List.of(), List.of("cook", "serve"), Map.of());
        var repeated = new LuckPermsStageOptions.InboundRule("repeated", List.of(), List.of("cook"),
            LuckPermsStageOptions.Match.ALL, Map.of());
        var definition = com.enviouse.progressivestages.common.config.StageDefinition.builder(stage)
            .luckPerms(new LuckPermsStageOptions(true, true, LuckPermsStageOptions.InboundMode.SYNCHRONIZED,
                List.of(first, repeated), List.of(), List.of())).build();
        var delegate = new InMemoryLuckPermsAdapter().permission(SUBJECT, "cook", PermissionValue.TRUE)
            .permission(SUBJECT, "serve", PermissionValue.TRUE);
        var calls = new java.util.ArrayList<String>();
        boolean[] available = {true};
        LuckPermsAdapter adapter = (LuckPermsAdapter) java.lang.reflect.Proxy.newProxyInstance(
            LuckPermsAdapter.class.getClassLoader(), new Class<?>[] {LuckPermsAdapter.class}, (proxy, method, arguments) -> {
                if (method.getName().equals("permissionResult")) {
                    String permission = (String) arguments[1];
                    calls.add(permission);
                    if (!available[0] && permission.equals("serve")) return PermissionResult.unavailable();
                }
                return method.invoke(delegate, arguments);
            });
        var input = OnlinePermissionInput.capture(adapter, SUBJECT, List.of(definition), true);
        assertEquals(List.of("cook", "serve"), calls);
        assertTrue(input.observations().get(stage).values().stream().allMatch(observation -> observation.eligible()));
        assertThrows(UnsupportedOperationException.class, () -> input.observations().get(stage).clear());
        available[0] = false;
        var incomplete = OnlinePermissionInput.capture(adapter, SUBJECT, List.of(definition), true);
        assertFalse(incomplete.snapshot().ready());
        assertTrue(incomplete.observations().isEmpty());
    }

    private static LuckPermsStageOptions.InboundRule row(List<String> groups, List<String> permissions,
                                                         Map<String, List<String>> contexts) {
        return new LuckPermsStageOptions.InboundRule("chef", groups, permissions,
            LuckPermsStageOptions.Match.ALL, contexts);
    }

    private static boolean matches(LuckPermsBridge bridge, LuckPermsStageOptions.InboundRule row,
                                    SubjectSnapshot snapshot) throws Exception {
        var observation = bridge.observe(SUBJECT, row, snapshot);
        return observation != null && observation.eligible() && LuckPermsBridge.contextMatches(row.contexts(), snapshot.contexts());
    }
}
