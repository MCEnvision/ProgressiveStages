package com.enviouse.progressivestages.server.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;
import net.luckperms.api.event.group.GroupLoadAllEvent;
import net.luckperms.api.event.group.GroupLoadEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.sync.ConfigReloadEvent;
import net.luckperms.api.event.sync.PostSyncEvent;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.event.user.UserUnloadEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class LuckPermsEventSubscriptionsTest {
    private static final UUID SUBJECT = new UUID(0, 71);

    @Test
    void userEventsCaptureOnlyTheSubjectAndGroupEventsRequestRescan() {
        Fixture fixture = new Fixture();
        try (var listeners = fixture.listeners) {
            listeners.register();
            listeners.register();
            assertEquals(9, fixture.handlers.size());
            User user = value(User.class, Map.of("getUniqueId", SUBJECT));
            fixture.fire(UserLoadEvent.class, Map.of("getUser", user));
            fixture.fire(UserUnloadEvent.class, Map.of("getUser", user));
            fixture.fire(NodeMutateEvent.class, Map.of("getTarget", user));
            assertEquals(List.of(SUBJECT, SUBJECT, SUBJECT), fixture.subjects);
            assertEquals(0, fixture.rescans);
            fixture.fire(NodeMutateEvent.class, Map.of("getTarget", value(Group.class, Map.of())));
            for (var type : List.of(GroupLoadEvent.class, GroupCreateEvent.class, GroupDeleteEvent.class,
                GroupLoadAllEvent.class, PostSyncEvent.class, ConfigReloadEvent.class)) {
                fixture.fire(type, Map.of());
            }
            assertEquals(7, fixture.rescans);
        }
        assertTrue(fixture.handlers.stream().allMatch(handler -> handler.closes == 1));
        fixture.fire(UserLoadEvent.class, Map.of());
        fixture.fire(PostSyncEvent.class, Map.of());
        assertEquals(3, fixture.subjects.size());
        assertEquals(7, fixture.rescans);
        fixture.listeners.register();
        assertEquals(9, fixture.handlers.size());
    }

    @Test
    void failedDetachmentDisablesCallbacksAndRetriesOnlyTheFailedSubscription() {
        Fixture fixture = new Fixture();
        fixture.listeners.register();
        fixture.handlers.get(2).failClose = true;
        try {
            assertThrows(IllegalStateException.class, fixture.listeners::close);
            assertTrue(fixture.handlers.stream().allMatch(handler -> handler.closes == 1));
            fixture.fire(NodeMutateEvent.class, Map.of());
            assertTrue(fixture.subjects.isEmpty());
            assertEquals(0, fixture.rescans);
        } finally {
            fixture.handlers.get(2).failClose = false;
            fixture.listeners.close();
        }
        assertEquals(2, fixture.handlers.get(2).closes);
        assertEquals(10, fixture.handlers.stream().mapToInt(handler -> handler.closes).sum());
    }

    @Test
    void partialRegistrationRetainsEveryReturnedSubscriptionForCleanup() {
        Fixture fixture = new Fixture();
        fixture.failRegistrationAt = 2;
        assertThrows(IllegalStateException.class, fixture.listeners::register);
        fixture.listeners.close();
        assertEquals(2, fixture.handlers.size());
        assertTrue(fixture.handlers.stream().allMatch(handler -> handler.closes == 1));
        fixture.fire(UserLoadEvent.class, Map.of());
        assertTrue(fixture.subjects.isEmpty());
    }

    private static <T> T value(Class<T> type, Map<String, ?> values) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
            (proxy, method, args) -> {
                if (values.containsKey(method.getName())) return values.get(method.getName());
                throw new AssertionError("Unexpected provider access " + method);
            }));
    }

    private static final class Handler {
        Class<? extends LuckPermsEvent> type;
        Consumer<LuckPermsEvent> callback;
        int closes;
        boolean failClose;
    }

    private static final class Fixture {
        final List<Handler> handlers = new ArrayList<>();
        final List<UUID> subjects = new ArrayList<>();
        final LuckPermsEventSubscriptions listeners;
        int rescans;
        int failRegistrationAt = -1;

        @SuppressWarnings("unchecked")
        Fixture() {
            EventBus bus = (EventBus) Proxy.newProxyInstance(EventBus.class.getClassLoader(),
                new Class<?>[] {EventBus.class}, (proxy, method, args) -> {
                    assertEquals("subscribe", method.getName());
                    assertEquals(2, args.length);
                    if (handlers.size() == failRegistrationAt) throw new IllegalStateException("Registration failed");
                    Handler handler = new Handler();
                    handler.type = (Class<? extends LuckPermsEvent>) args[0];
                    handler.callback = (Consumer<LuckPermsEvent>) args[1];
                    handlers.add(handler);
                    return Proxy.newProxyInstance(EventSubscription.class.getClassLoader(),
                        new Class<?>[] {EventSubscription.class}, (subscription, operation, parameters) -> {
                            assertEquals("close", operation.getName());
                            handler.closes++;
                            if (handler.failClose) throw new IllegalStateException("Detachment failed");
                            return null;
                        });
                });
            listeners = new LuckPermsEventSubscriptions(value(LuckPerms.class, Map.of("getEventBus", bus)),
                subjects::add, () -> rescans++);
        }

        void fire(Class<? extends LuckPermsEvent> type, Map<String, ?> properties) {
            handlers.stream().filter(handler -> handler.type == type).findFirst().orElseThrow()
                .callback.accept(value(type, properties));
        }
    }
}
