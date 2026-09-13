package com.enviouse.progressivestages.server.integration.luckperms;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;
import static org.junit.jupiter.api.Assertions.*;

class OutboundNodeTrackerTest {
    private static final UUID SUBJECT = UUID.randomUUID();
    private static final NodeSpec HOME = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of());
    private static final NodeSpec WARP = new NodeSpec(NodeKind.PERMISSION, "warp.set", Map.of());
    private final OutboundNodeTracker tracker = new OutboundNodeTracker();
    private final Adapter adapter = new Adapter();
    private final List<MutationResult> observations = new ArrayList<>();

    @Test
    void editingTheSameRowRemovesItsOldPermissionBeforeAddingTheReplacement() {
        assertTrue(reconcile(Map.of("chef|home|0", HOME)));
        adapter.calls.clear();
        assertTrue(reconcile(Map.of("chef|home|0", WARP)));
        assertEquals(List.of("remove home.set", "add warp.set"), adapter.calls);
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "home.set"));
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "warp.set"));
        assertTrue(tracker.cleanup(adapter));
    }

    @Test
    void overlappingRowsKeepTheirOwnReferencesThroughAnEditAndRemoval() {
        assertTrue(reconcile(Map.of("chef", HOME, "builder", HOME)));
        assertTrue(reconcile(Map.of("chef", WARP, "builder", HOME)));
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "home.set"));
        assertTrue(reconcile(Map.of("chef", WARP)));
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "home.set"));
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "warp.set"));
        assertTrue(tracker.cleanup(adapter));
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "warp.set"));
    }

    @Test
    void aFailedRemovalPreventsReplacementAndRemainsAvailableForRetry() {
        assertTrue(reconcile(Map.of("chef", HOME)));
        adapter.removeFails = true;
        adapter.calls.clear();
        assertFalse(reconcile(Map.of("chef", WARP)));
        assertEquals(List.of("remove home.set"), adapter.calls);
        assertFalse(tracker.cleanup(adapter));
        adapter.removeFails = false;
        assertTrue(reconcile(Map.of("chef", WARP)));
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "home.set"));
        assertTrue(tracker.cleanup(adapter));
    }

    @Test
    void failedAdditionsRetryAndAnAmbiguousWriteIsCleanedBeforeReplacement() {
        adapter.addFailsAfterWrite = true;
        assertFalse(reconcile(Map.of("chef", HOME)));
        assertEquals(List.of(MutationResult.FAILED), observations);
        adapter.addFailsAfterWrite = false;
        assertTrue(reconcile(Map.of("chef", HOME)));
        assertEquals(List.of(MutationResult.FAILED, MutationResult.APPLIED), observations);
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "home.set"));
        adapter.calls.clear();
        assertTrue(reconcile(Map.of("chef", WARP)));
        assertEquals(List.of("remove home.set", "add warp.set"), adapter.calls);
        assertTrue(tracker.cleanup(adapter));
    }

    @Test
    void contextAndKindChangesReplaceTheExactContribution() {
        adapter.delegate.context(SUBJECT, "world", "overworld");
        NodeSpec world = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of("world", "overworld"));
        NodeSpec otherWorld = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of("world", "nether"));
        assertTrue(reconcile(Map.of("chef", world)));
        assertTrue(reconcile(Map.of("chef", otherWorld)));
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "home.set"));
        assertTrue(reconcile(Map.of("chef", new NodeSpec(NodeKind.GROUP, "home.set", Map.of()))));
        assertTrue(adapter.snapshot(SUBJECT).groups().contains("home.set"));
        assertTrue(tracker.cleanup(adapter));
        assertFalse(adapter.snapshot(SUBJECT).groups().contains("home.set"));
    }

    @Test
    void independentPermissionsAndNegativeValuesSurviveOwnedCleanup() {
        adapter.delegate.permission(SUBJECT, "home.set", PermissionValue.FALSE);
        adapter.delegate.permission(SUBJECT, "warp.set", PermissionValue.TRUE);
        assertTrue(reconcile(Map.of("chef", HOME, "builder", WARP)));
        assertEquals(PermissionValue.FALSE, adapter.permission(SUBJECT, "home.set"));
        assertTrue(tracker.cleanup(adapter));
        assertEquals(PermissionValue.FALSE, adapter.permission(SUBJECT, "home.set"));
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "warp.set"));
    }

    @Test
    void staleWritesStopBeforeTheNextNodeAndRetainExactCleanupOwnership() {
        var current = new java.util.concurrent.atomic.AtomicBoolean(true);
        var desired = new java.util.LinkedHashMap<String, NodeSpec>();
        desired.put("chef", HOME);
        desired.put("builder", WARP);
        adapter.afterAdd = () -> current.set(false);
        assertFalse(tracker.reconcile(SUBJECT, desired, adapter, (owner, adding, result) -> {}, current::get));
        assertEquals(List.of("add home.set"), adapter.calls);
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "home.set"));
        assertTrue(tracker.cleanup(adapter));
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "home.set"));
        assertEquals(List.of("add home.set", "remove home.set"), adapter.calls);
    }

    @Test
    void reentrantCleanupCannotLoseAnInFlightContribution() {
        adapter.afterAdd = () -> assertFalse(tracker.cleanup(adapter));
        assertTrue(reconcile(Map.of("chef", HOME)));
        assertEquals(PermissionValue.TRUE, adapter.permission(SUBJECT, "home.set"));
        assertTrue(tracker.cleanup(adapter));
        assertEquals(PermissionValue.UNDEFINED, adapter.permission(SUBJECT, "home.set"));
        assertEquals(List.of("add home.set", "remove home.set"), adapter.calls);
    }

    private boolean reconcile(Map<String, NodeSpec> desired) {
        return tracker.reconcile(SUBJECT, desired, adapter, (owner, adding, result) -> observations.add(result));
    }

    private static final class Adapter implements LuckPermsAdapter {
        final InMemoryLuckPermsAdapter delegate = new InMemoryLuckPermsAdapter();
        final List<String> calls = new ArrayList<>();
        boolean removeFails;
        Runnable afterAdd;
        boolean addFailsAfterWrite;
        @Override public State state() { return delegate.state(); }
        @Override public SubjectSnapshot snapshot(UUID subject) { return delegate.effectiveSnapshot(subject); }
        @Override public PermissionValue permission(UUID subject, String value) { return delegate.effectivePermission(subject, value); }
        @Override public boolean groupExists(String group) { return delegate.groupExists(group); }
        @Override public MutationResult addTransient(UUID subject, NodeKind kind, String value,
                                                      Map<String, String> contexts, String owner) {
            calls.add("add " + value);
            MutationResult result = delegate.addTransient(subject, kind, value, contexts, owner);
            if (afterAdd != null) {
                Runnable action = afterAdd;
                afterAdd = null;
                action.run();
            }
            return addFailsAfterWrite ? MutationResult.FAILED : result;
        }
        @Override public MutationResult removeTransient(UUID subject, NodeKind kind, String value,
                                                         Map<String, String> contexts, String owner) {
            calls.add("remove " + value);
            return removeFails ? MutationResult.FAILED : delegate.removeTransient(subject, kind, value, contexts, owner);
        }
    }
}
