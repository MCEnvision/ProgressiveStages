package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilderRegistry;
import net.luckperms.api.node.metadata.NodeMetadataKey;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;
import static org.junit.jupiter.api.Assertions.*;

class LuckPermsTransientNodesTest {
    private static final UUID SUBJECT = UUID.randomUUID();
    private static final NodeSpec HOME = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of());

    @Test
    void transientReferencesAreIdempotentAndOnlyTheLastOwnerRemovesTheNode() {
        Fixture fixture = new Fixture();
        assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, HOME, "chef"));
        assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, HOME, "chef"));
        assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, HOME, "builder"));
        assertEquals(1, fixture.nodes.size());
        assertEquals(MutationResult.APPLIED, fixture.store.remove(SUBJECT, HOME, "stranger"));
        assertEquals(MutationResult.APPLIED, fixture.store.remove(SUBJECT, HOME, "chef"));
        assertEquals(1, fixture.nodes.size());
        assertEquals(MutationResult.APPLIED, fixture.store.remove(SUBJECT, HOME, "builder"));
        assertTrue(fixture.nodes.isEmpty());
        assertTrue(fixture.store.cleanup());
    }

    @Test
    void anEqualAdministrativeNodeIsNeitherAdoptedNorRemoved() {
        Fixture fixture = new Fixture();
        Node administrative = fixture.external(HOME);
        fixture.nodes.add(administrative);
        assertEquals(MutationResult.CONFLICT, fixture.store.add(SUBJECT, HOME, "chef"));
        assertTrue(fixture.store.cleanup());
        assertSame(administrative, fixture.nodes.getFirst());
    }

    @Test
    void administrativeReplacementBetweenGrantAndRevokeSurvives() {
        Fixture fixture = new Fixture();
        assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, HOME, "chef"));
        Node administrative = fixture.external(HOME);
        fixture.nodes.clear();
        fixture.nodes.add(administrative);
        assertEquals(MutationResult.APPLIED, fixture.store.remove(SUBJECT, HOME, "chef"));
        assertSame(administrative, fixture.nodes.getFirst());
        assertTrue(fixture.store.cleanup());
    }

    @Test
    void aProviderExceptionAfterInsertionRetainsEnoughOwnershipForCleanup() {
        Fixture fixture = new Fixture();
        fixture.throwAfterAdd = true;
        assertThrows(IllegalStateException.class, () -> fixture.store.add(SUBJECT, HOME, "chef"));
        assertEquals(1, fixture.nodes.size());
        assertTrue(fixture.store.cleanup());
        assertTrue(fixture.nodes.isEmpty());
    }

    @Test
    void rejectedAdditionCanRetryWithoutBeingReportedAsApplied() {
        Fixture fixture = new Fixture();
        fixture.rejectAdd = true;
        assertEquals(MutationResult.FAILED, fixture.store.add(SUBJECT, HOME, "chef"));
        assertTrue(fixture.nodes.isEmpty());
        fixture.rejectAdd = false;
        assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, HOME, "chef"));
        assertEquals(1, fixture.nodes.size());
        assertTrue(fixture.store.cleanup());
    }

    @Test
    void cleanupFailureAndUnavailableUserKeepTheOwnedNodeForRetry() {
        Fixture fixture = new Fixture();
        assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, HOME, "chef"));
        fixture.rejectClear = true;
        assertFalse(fixture.store.cleanup());
        assertEquals(1, fixture.nodes.size());
        fixture.loaded = false;
        assertEquals(MutationResult.UNAVAILABLE, fixture.store.remove(SUBJECT, HOME, "chef"));
        assertFalse(fixture.store.cleanup());
        fixture.loaded = true;
        fixture.rejectClear = false;
        assertTrue(fixture.store.cleanup());
        assertTrue(fixture.nodes.isEmpty());
    }

    @Test
    void nodeKindAndContextsHaveIndependentOwnership() {
        Fixture fixture = new Fixture();
        NodeSpec group = new NodeSpec(NodeKind.GROUP, "home.set", Map.of());
        NodeSpec world = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of("world", "nether"));
        for (NodeSpec spec : List.of(HOME, group, world)) {
            assertEquals(MutationResult.APPLIED, fixture.store.add(SUBJECT, spec, "chef"));
        }
        assertEquals(3, fixture.nodes.size());
        assertEquals(MutationResult.APPLIED, fixture.store.remove(SUBJECT, world, "chef"));
        assertEquals(2, fixture.nodes.size());
        assertTrue(fixture.store.cleanup());
        assertTrue(fixture.nodes.isEmpty());
    }

    private static final class Fixture {
        final List<Node> nodes = new ArrayList<>();
        boolean loaded = true;
        boolean rejectAdd;
        boolean throwAfterAdd;
        boolean rejectClear;
        final LuckPermsTransientNodes store;

        Fixture() {
            NodeMap data = proxy(NodeMap.class, (proxy, method, args) -> switch (method.getName()) {
                case "toCollection" -> List.copyOf(nodes);
                case "add" -> {
                    if (rejectAdd) yield DataMutateResult.FAIL;
                    Node node = (Node) args[0];
                    if (nodes.contains(node)) yield DataMutateResult.FAIL_ALREADY_HAS;
                    nodes.add(node);
                    if (throwAfterAdd) throw new IllegalStateException("Provider failed after mutation");
                    yield DataMutateResult.SUCCESS;
                }
                case "clear" -> {
                    if (!rejectClear) {
                        @SuppressWarnings("unchecked") Predicate<Node> predicate = (Predicate<Node>) args[0];
                        nodes.removeIf(predicate);
                    }
                    yield null;
                }
                default -> throw new AssertionError("Unexpected node map access " + method);
            });
            User user = proxy(User.class, (proxy, method, args) -> {
                if (method.getName().equals("transientData")) return data;
                throw new AssertionError("Persistent data or unexpected user access " + method);
            });
            UserManager users = proxy(UserManager.class, (proxy, method, args) -> {
                if (method.getName().equals("getUser")) return loaded ? user : null;
                throw new AssertionError("Persistent save or unexpected manager access " + method);
            });
            NodeBuilderRegistry builders = proxy(NodeBuilderRegistry.class, (proxy, method, args) -> switch (method.getName()) {
                case "forPermission" -> builder(false);
                case "forInheritance" -> builder(true);
                default -> throw new AssertionError(method);
            });
            LuckPerms api = proxy(LuckPerms.class, (proxy, method, args) -> switch (method.getName()) {
                case "getUserManager" -> users;
                case "getNodeBuilderRegistry" -> builders;
                default -> throw new AssertionError(method);
            });
            store = new LuckPermsTransientNodes(api);
        }

        Node external(NodeSpec spec) {
            Map<String, String> contexts = new HashMap<>(spec.contexts());
            contexts.put("progressivestages_bridge", "active");
            return new NodeState(spec.kind() == NodeKind.GROUP ? "group." + spec.value() : spec.value(),
                contexts, Map.of()).node(spec.kind() == NodeKind.GROUP);
        }
    }

    private static Object builder(boolean group) {
        Map<String, String> contexts = new HashMap<>();
        Map<String, Object> metadata = new HashMap<>();
        String[] key = {""};
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "group" -> key[0] = "group." + args[0];
                case "permission" -> key[0] = (String) args[0];
                case "withContext" -> contexts.put((String) args[0], (String) args[1]);
                case "withMetadata" -> metadata.put(((NodeMetadataKey<?>) args[0]).name(), args[1]);
                case "build" -> { return new NodeState(key[0], Map.copyOf(contexts), Map.copyOf(metadata)).node(group); }
                default -> throw new AssertionError(method);
            }
            return proxy;
        };
        return group ? proxy(InheritanceNode.Builder.class, handler) : proxy(PermissionNode.Builder.class, handler);
    }

    private record NodeState(String key, Map<String, String> contexts, Map<String, Object> metadata)
            implements InvocationHandler {
        Node node(boolean group) {
            return group ? proxy(InheritanceNode.class, this) : proxy(PermissionNode.class, this);
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getMetadata" -> Optional.ofNullable(metadata.get(((NodeMetadataKey<?>) args[0]).name()));
                case "getKey" -> key;
                case "getValue" -> true;
                case "hashCode" -> key.hashCode() + contexts.hashCode();
                case "equals" -> args[0] != null && Proxy.isProxyClass(args[0].getClass())
                    && Proxy.getInvocationHandler(args[0]) instanceof NodeState other
                    && key.equals(other.key) && contexts.equals(other.contexts);
                case "toString" -> key + contexts;
                default -> throw new AssertionError(method);
            };
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
