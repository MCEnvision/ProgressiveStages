package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.enviouse.progressivestages.server.integration.luckperms.LuckPermsAdapter.*;
import static org.junit.jupiter.api.Assertions.*;

class LuckPermsQueriesTest {
    private static final UUID SUBJECT = UUID.randomUUID();
    private static final String MARKER = "progressivestages_bridge";

    @Test
    void independentQueriesRemoveOnlyTheMarkerAndPreserveCurrentContextsAndFlags() {
        Fixture fixture = new Fixture();
        fixture.current = new Query(Map.of("world", Set.of("overworld"), "region", Set.of("market", "town"),
            MARKER, Set.of("active", "other")), QueryMode.NON_CONTEXTUAL, Set.of(Flag.RESOLVE_INHERITANCE));
        SubjectSnapshot snapshot = fixture.queries.snapshot(SUBJECT);
        assertTrue(snapshot.ready());
        assertEquals(Set.of("chef"), snapshot.groups());
        assertEquals(Map.of("world", Set.of("overworld"), "region", Set.of("market", "town")), snapshot.contexts());
        QueryOptions used = fixture.groupQueries.getFirst();
        assertEquals(QueryMode.CONTEXTUAL, used.mode());
        assertEquals(Set.of(Flag.RESOLVE_INHERITANCE), used.flags());
        assertEquals(Set.of("active", "other"), fixture.current.contexts.get(MARKER));
        assertTrue(snapshot.permissions().isEmpty());
    }

    @Test
    void booleanQueriesUseTheSameIndependentContextPolicyAndPreserveAllThreeValues() {
        Fixture fixture = new Fixture();
        fixture.current = new Query(Map.of("world", Set.of("nether"), MARKER, Set.of("active")),
            QueryMode.CONTEXTUAL, Set.of(Flag.RESOLVE_INHERITANCE));
        for (Tristate value : Tristate.values()) {
            fixture.permission = value;
            PermissionResult result = fixture.queries.permission(SUBJECT, "home.set");
            assertTrue(result.ready());
            assertEquals(PermissionValue.valueOf(value.name()), result.value());
        }
        assertEquals(List.of("home.set", "home.set", "home.set"), fixture.requestedPermissions);
        for (QueryOptions query : fixture.permissionQueries) {
            assertEquals(QueryMode.CONTEXTUAL, query.mode());
            assertEquals(Map.of("world", Set.of("nether")), query.context().toMap());
        }
    }

    @Test
    void loadedOfflineUsersUseAuthoritativeStaticContextsWithoutLastWorldState() {
        Fixture fixture = new Fixture();
        fixture.online = false;
        fixture.current = new Query(Map.of("world", Set.of("old_world")), QueryMode.CONTEXTUAL, Set.of());
        fixture.fixed = new Query(Map.of("server", Set.of("professions"), MARKER, Set.of("active")),
            QueryMode.CONTEXTUAL, Set.of(Flag.RESOLVE_INHERITANCE));
        assertEquals(Map.of("server", Set.of("professions")), fixture.queries.snapshot(SUBJECT).contexts());
        assertTrue(fixture.queries.permission(SUBJECT, "home.set").ready());
        assertEquals(Map.of("server", Set.of("professions")), fixture.permissionQueries.getFirst().context().toMap());
    }

    @Test
    void anUnloadedUserIsUnavailableAndNeverStartsABlockingLoad() {
        Fixture fixture = new Fixture();
        fixture.loaded = false;
        assertFalse(fixture.queries.snapshot(SUBJECT).ready());
        assertEquals(PermissionResult.unavailable(), fixture.queries.permission(SUBJECT, "home.set"));
        assertTrue(fixture.groupQueries.isEmpty());
        assertTrue(fixture.permissionQueries.isEmpty());
    }

    @Test
    void snapshotsDeepCopyContextValueSets() {
        Set<String> regions = new HashSet<>(Set.of("market", "town"));
        Map<String, Set<String>> contexts = new HashMap<>(Map.of("region", regions));
        SubjectSnapshot snapshot = new SubjectSnapshot(true, Set.of(), Map.of(), contexts);
        regions.clear();
        contexts.clear();
        assertEquals(Set.of("market", "town"), snapshot.contexts().get("region"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.contexts().get("region").clear());
    }

    @Test
    void outputNodeIdentityUsesTheProvidersCaseInsensitiveContextSemantics() {
        NodeSpec upper = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of("WORLD", "OverWorld"));
        NodeSpec lower = new NodeSpec(NodeKind.PERMISSION, "home.set", Map.of("world", "overworld"));
        assertEquals(lower, upper);
        assertThrows(IllegalArgumentException.class, () -> new NodeSpec(NodeKind.PERMISSION, "home.set",
            Map.of("world", "overworld", "WORLD", "nether")));
    }

    @Test
    void reservedContextAliasesAndDuplicateCaseVariantsAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            com.enviouse.progressivestages.common.config.LuckPermsStageOptions.normalizeContexts(
                Map.of("ProgressiveStages_Bridge", List.of("active"))));
        assertThrows(IllegalArgumentException.class, () ->
            com.enviouse.progressivestages.common.config.LuckPermsStageOptions.normalizeContexts(
                Map.of("World", List.of("overworld"), "world", List.of("nether"))));
    }

    @Test
    void providerFailureCannotBeMistakenForAnAuthoritativeUndefinedPermission() {
        Fixture fixture = new Fixture();
        fixture.failPermission = true;
        assertThrows(IllegalStateException.class, () -> fixture.queries.permission(SUBJECT, "home.set"));
        fixture.failPermission = false;
        fixture.permission = Tristate.UNDEFINED;
        assertEquals(new PermissionResult(true, PermissionValue.UNDEFINED), fixture.queries.permission(SUBJECT, "home.set"));
    }

    private static final class Fixture {
        boolean loaded = true;
        boolean online = true;
        boolean failPermission;
        Tristate permission = Tristate.TRUE;
        Query current = new Query(Map.of(), QueryMode.CONTEXTUAL, Set.of());
        Query fixed = new Query(Map.of(), QueryMode.CONTEXTUAL, Set.of());
        final List<QueryOptions> groupQueries = new ArrayList<>();
        final List<QueryOptions> permissionQueries = new ArrayList<>();
        final List<String> requestedPermissions = new ArrayList<>();
        final LuckPermsQueries queries;

        Fixture() {
            CachedPermissionData permissions = proxy(CachedPermissionData.class, (proxy, method, args) -> {
                if (method.getName().equals("checkPermission")) {
                    if (failPermission) throw new IllegalStateException("Provider query failed");
                    requestedPermissions.add((String) args[0]);
                    return permission;
                }
                throw new AssertionError("Unexpected permission enumeration " + method);
            });
            CachedDataManager cached = proxy(CachedDataManager.class, (proxy, method, args) -> {
                if (method.getName().equals("getPermissionData") && args.length == 1) {
                    permissionQueries.add((QueryOptions) args[0]);
                    return permissions;
                }
                throw new AssertionError(method);
            });
            User user = proxy(User.class, (proxy, method, args) -> switch (method.getName()) {
                case "getInheritedGroups" -> {
                    QueryOptions query = (QueryOptions) args[0];
                    groupQueries.add(query);
                    yield query.context().containsKey(MARKER) ? List.of(group("chef"), group("bridge_only")) : List.of(group("chef"));
                }
                case "getCachedData" -> cached;
                default -> throw new AssertionError(method);
            });
            UserManager users = proxy(UserManager.class, (proxy, method, args) -> {
                if (method.getName().equals("getUser")) return loaded ? user : null;
                throw new AssertionError("Unexpected user load or mutation " + method);
            });
            ContextManager contexts = proxy(ContextManager.class, (proxy, method, args) -> switch (method.getName()) {
                case "getQueryOptions" -> online ? Optional.of(current.options()) : Optional.empty();
                case "getStaticQueryOptions" -> fixed.options();
                default -> throw new AssertionError(method);
            });
            LuckPerms api = proxy(LuckPerms.class, (proxy, method, args) -> switch (method.getName()) {
                case "getUserManager" -> users;
                case "getContextManager" -> contexts;
                default -> throw new AssertionError(method);
            });
            queries = new LuckPermsQueries(api);
        }
    }

    record Query(Map<String, Set<String>> contexts, QueryMode mode, Set<Flag> flags) {
        QueryOptions options() {
            return proxy(QueryOptions.class, (proxy, method, args) -> switch (method.getName()) {
                case "mode" -> mode;
                case "flags" -> flags;
                case "context" -> context(contexts, false);
                case "toBuilder" -> builder();
                default -> throw new AssertionError(method);
            });
        }

        private QueryOptions.Builder builder() {
            QueryMode[] selectedMode = {mode};
            ContextSet[] selectedContexts = {context(contexts, false)};
            return proxy(QueryOptions.Builder.class, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "mode" -> selectedMode[0] = (QueryMode) args[0];
                    case "context" -> selectedContexts[0] = (ContextSet) args[0];
                    case "build" -> { return new Query(selectedContexts[0].toMap(), selectedMode[0], flags).options(); }
                    default -> throw new AssertionError(method);
                }
                return proxy;
            });
        }
    }

    private static ContextSet context(Map<String, Set<String>> source, boolean mutable) {
        Map<String, Set<String>> values = new HashMap<>();
        source.forEach((key, entries) -> values.put(key, Set.copyOf(entries)));
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "mutableCopy" -> context(values, true);
            case "immutableCopy" -> context(values, false);
            case "toMap" -> Map.copyOf(values);
            case "containsKey" -> values.containsKey(args[0]);
            case "removeAll" -> {
                assertTrue(mutable);
                values.remove(args[0]);
                yield null;
            }
            default -> throw new AssertionError(method);
        };
        return mutable ? proxy(MutableContextSet.class, handler) : proxy(ImmutableContextSet.class, handler);
    }

    private static Group group(String name) {
        return proxy(Group.class, (proxy, method, args) -> {
            if (method.getName().equals("getName")) return name;
            throw new AssertionError(method);
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
