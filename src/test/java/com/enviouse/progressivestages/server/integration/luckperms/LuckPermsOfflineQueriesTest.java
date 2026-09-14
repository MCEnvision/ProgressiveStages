package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.event.user.UserUnloadEvent;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class LuckPermsOfflineQueriesTest {
    private static final UUID SUBJECT = new UUID(0, 81);

    @Test
    void loadsAreBoundedAndCompletedResultsKeepTheirSlotsUntilAcknowledged() {
        Fixture fixture = new Fixture();
        List<UUID> subjects = new ArrayList<>();
        try {
            for (int i = 1; i <= 8; i++) {
                UUID subject = new UUID(0, i);
                subjects.add(subject);
                assertTrue(fixture.request(subject));
                assertNull(fixture.queries.take(subject));
            }
            assertFalse(fixture.request(SUBJECT));
            assertFalse(fixture.request(subjects.getFirst()));
            UUID first = subjects.getFirst();
            fixture.loads.get(first).complete(fixture.user(first));
            var result = fixture.queries.take(first);
            assertTrue(result.snapshot().ready());
            assertFalse(fixture.request(SUBJECT));
            assertTrue(fixture.queries.current(first));
            fixture.queries.complete(first);
            assertTrue(fixture.request(SUBJECT));
        } finally {
            fixture.finishAndClose();
        }
        assertEquals(9, fixture.cleaned.size());
    }

    @Test
    void resultsUseOnlyFixedContextsAndTheRequestedBooleanPermissions() {
        Fixture fixture = new Fixture();
        try {
            assertTrue(fixture.request(SUBJECT));
            assertTrue(fixture.queries.owns(SUBJECT));
            fixture.loads.get(SUBJECT).complete(fixture.user(SUBJECT));
            assertFalse(fixture.queries.owns(SUBJECT));
            var result = fixture.queries.take(SUBJECT);
            assertFalse(result.stale());
            assertEquals(Set.of("chef"), result.snapshot().groups());
            assertEquals(Map.of("server", Set.of("professions")), result.snapshot().contexts());
            assertEquals(Map.of("professions.chef", LuckPermsAdapter.PermissionValue.TRUE,
                "denied", LuckPermsAdapter.PermissionValue.FALSE), result.snapshot().permissions());
            assertEquals(List.of(SUBJECT), fixture.completed);
            assertEquals(List.of(SUBJECT), fixture.cleaned);
        } finally {
            fixture.finishAndClose();
        }
    }

    @Test
    void externallyLoadedUsersAreNotReleasedByTheQuery() {
        Fixture fixture = new Fixture();
        fixture.loaded.put(SUBJECT, fixture.user(SUBJECT));
        try {
            assertTrue(fixture.request(SUBJECT));
            assertTrue(fixture.queries.take(SUBJECT).snapshot().ready());
            assertTrue(fixture.loads.isEmpty());
            assertTrue(fixture.cleaned.isEmpty());
        } finally {
            fixture.finishAndClose();
        }
    }

    @Test
    void changedSubjectsAndGlobalInvalidationRejectCompletedObservations() {
        Fixture fixture = new Fixture();
        UUID second = new UUID(0, 82);
        try {
            fixture.request(SUBJECT);
            fixture.request(second);
            fixture.loads.get(SUBJECT).complete(fixture.user(SUBJECT));
            fixture.loads.get(second).complete(fixture.user(second));
            fixture.queries.invalidate(SUBJECT);
            assertTrue(fixture.queries.take(SUBJECT).stale());
            assertFalse(fixture.queries.current(SUBJECT));
            assertTrue(fixture.queries.current(second));
            fixture.queries.invalidateAll();
            assertFalse(fixture.queries.current(second));
            assertTrue(fixture.queries.take(second).stale());
        } finally {
            fixture.finishAndClose();
        }
    }

    @Test
    void unavailableLoadsAndCleanupFailuresNeverPublishTrustedData() {
        Fixture fixture = new Fixture();
        try {
            fixture.request(SUBJECT);
            fixture.loads.get(SUBJECT).completeExceptionally(new IllegalStateException("Storage unavailable"));
            assertFalse(fixture.queries.take(SUBJECT).snapshot().ready());
            fixture.queries.complete(SUBJECT);
            fixture.request(SUBJECT);
            fixture.failCleanup = true;
            fixture.loads.get(SUBJECT).complete(fixture.user(SUBJECT));
            assertThrows(IllegalStateException.class, () -> fixture.queries.take(SUBJECT));
            assertFalse(fixture.queries.current(SUBJECT));
            assertFalse(fixture.queries.close());
            fixture.failCleanup = false;
            assertTrue(fixture.queries.close());
        } finally {
            fixture.failCleanup = false;
            fixture.finishAndClose();
        }
    }

    @Test
    void ownedCacheLifecycleDoesNotRestartItsOwnQueryButExternalReloadInvalidatesTheResult() throws Exception {
        Fixture fixture = new Fixture();
        ReflectiveLuckPermsAdapter adapter = new ReflectiveLuckPermsAdapter();
        for (var entry : Map.of("api", fixture.api, "offlineQueries", fixture.queries,
            "projectionContexts", new LuckPermsProjectionContexts(fixture.api)).entrySet()) {
            var field = ReflectiveLuckPermsAdapter.class.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(adapter, entry.getValue());
        }
        List<UUID> changed = new ArrayList<>();
        try {
            assertTrue(adapter.subscribeChanges(changed::add, () -> fail("No global event is expected")));
            assertTrue(adapter.requestOffline(SUBJECT, Set.of("professions.chef", "denied"), fixture.completed::add));
            fixture.lifecycle(UserLoadEvent.class, fixture.user(SUBJECT));
            fixture.loads.get(SUBJECT).complete(fixture.user(SUBJECT));
            assertEquals(List.of(SUBJECT), fixture.completed);
            assertTrue(changed.isEmpty());
            assertTrue(adapter.takeOffline(SUBJECT).snapshot().ready());
            assertTrue(adapter.isOfflineCurrent(SUBJECT));
            fixture.lifecycle(UserLoadEvent.class, fixture.user(SUBJECT));
            assertEquals(List.of(SUBJECT), changed);
            assertFalse(adapter.isOfflineCurrent(SUBJECT));
            assertTrue(adapter.takeOffline(SUBJECT).stale());
            assertTrue(adapter.stopListening());
            fixture.lifecycle(UserLoadEvent.class, fixture.user(SUBJECT));
            assertEquals(List.of(SUBJECT), changed);
        } finally {
            fixture.finishAndClose();
            assertTrue(adapter.shutdown());
        }
    }

    @Test
    void shutdownWaitsForOwnedLoadsAndSuppressesTheirLateNotifications() {
        Fixture fixture = new Fixture();
        fixture.request(SUBJECT);
        assertFalse(fixture.queries.close());
        assertFalse(fixture.request(new UUID(0, 83)));
        fixture.loads.get(SUBJECT).complete(fixture.user(SUBJECT));
        assertTrue(fixture.completed.isEmpty());
        assertEquals(List.of(SUBJECT), fixture.cleaned);
        assertNull(fixture.queries.take(SUBJECT));
        assertTrue(fixture.queries.close());
    }

    private static final class Fixture {
        final Map<UUID, CompletableFuture<User>> loads = new HashMap<>();
        final Map<UUID, User> loaded = new HashMap<>();
        final List<UUID> cleaned = new ArrayList<>();
        final List<UUID> completed = new ArrayList<>();
        final LuckPermsOfflineQueries queries;
        final LuckPerms api;
        final Map<Class<?>, Consumer<Object>> handlers = new HashMap<>();
        boolean failCleanup;

        @SuppressWarnings("unchecked")
        Fixture() {
            UserManager users = proxy(UserManager.class, (object, method, args) -> switch (method.getName()) {
                case "getUser" -> loaded.get((UUID) args[0]);
                case "loadUser" -> {
                    var future = new CompletableFuture<User>();
                    loads.put((UUID) args[0], future);
                    yield future;
                }
                case "cleanupUser" -> {
                    if (failCleanup) throw new IllegalStateException("Cleanup failed");
                    cleaned.add(((User) args[0]).getUniqueId());
                    if (handlers.containsKey(UserUnloadEvent.class)) lifecycle(UserUnloadEvent.class, (User) args[0]);
                    yield null;
                }
                default -> throw new AssertionError("Unexpected user storage operation " + method);
            });
            ContextManager contexts = proxy(ContextManager.class, (object, method, args) -> {
                assertEquals("getStaticQueryOptions", method.getName());
                return new LuckPermsQueriesTest.Query(Map.of("server", Set.of("professions"),
                    "progressivestages_bridge", Set.of("active")), QueryMode.NON_CONTEXTUAL,
                    Set.of(Flag.RESOLVE_INHERITANCE)).options();
            });
            EventBus events = proxy(EventBus.class, (object, method, args) -> {
                assertEquals("subscribe", method.getName());
                handlers.put((Class<?>) args[0], (Consumer<Object>) args[1]);
                return proxy(EventSubscription.class, (subscription, operation, parameters) -> {
                    assertEquals("close", operation.getName()); return null;
                });
            });
            api = proxy(LuckPerms.class, (object, method, args) -> switch (method.getName()) {
                case "getUserManager" -> users;
                case "getContextManager" -> contexts;
                case "getEventBus" -> events;
                default -> throw new AssertionError(method);
            });
            queries = new LuckPermsOfflineQueries(api);
        }

        boolean request(UUID subject) { return queries.request(subject, Set.of("professions.chef", "denied"), completed::add); }

        void lifecycle(Class<?> type, User user) {
            handlers.get(type).accept(proxy(type, (event, method, args) -> {
                assertEquals("getUser", method.getName()); return user;
            }));
        }

        User user(UUID subject) {
            CachedPermissionData permission = proxy(CachedPermissionData.class, (object, method, args) -> {
                assertEquals("checkPermission", method.getName());
                assertTrue(Set.of("professions.chef", "denied").contains(args[0]));
                return args[0].equals("denied") ? Tristate.FALSE : Tristate.TRUE;
            });
            CachedDataManager data = proxy(CachedDataManager.class, (object, method, args) -> {
                assertEquals("getPermissionData", method.getName());
                checkQuery((QueryOptions) args[0]);
                return permission;
            });
            Group group = proxy(Group.class, (object, method, args) -> {
                assertEquals("getName", method.getName()); return "chef";
            });
            return proxy(User.class, (object, method, args) -> switch (method.getName()) {
                case "getUniqueId" -> subject;
                case "getInheritedGroups" -> { checkQuery((QueryOptions) args[0]); yield List.of(group); }
                case "getCachedData" -> data;
                default -> throw new AssertionError(method);
            });
        }

        void checkQuery(QueryOptions options) {
            assertEquals(QueryMode.CONTEXTUAL, options.mode());
            assertEquals(Set.of(Flag.RESOLVE_INHERITANCE), options.flags());
            assertEquals(Map.of("server", Set.of("professions")), options.context().toMap());
        }

        void finishAndClose() {
            queries.close();
            loads.forEach((subject, future) -> { if (!future.isDone()) future.complete(user(subject)); });
            assertTrue(queries.close());
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }
}
