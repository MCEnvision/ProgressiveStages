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
import net.luckperms.api.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

final class LuckPermsEventSubscriptions implements AutoCloseable {
    private final EventBus events;
    private final Consumer<UUID> subjectChanged;
    private final Runnable allChanged;
    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();
    private boolean registered;
    private boolean closed;

    LuckPermsEventSubscriptions(Object api, Consumer<UUID> subjectChanged, Runnable allChanged) {
        events = ((LuckPerms) api).getEventBus();
        this.subjectChanged = subjectChanged;
        this.allChanged = allChanged;
    }

    void register() {
        synchronized (this) {
            if (closed || registered) return;
            registered = true;
        }
        subscribe(UserLoadEvent.class, event -> subjectChanged.accept(event.getUser().getUniqueId()));
        subscribe(UserUnloadEvent.class, event -> subjectChanged.accept(event.getUser().getUniqueId()));
        subscribe(NodeMutateEvent.class, event -> {
            if (event.getTarget() instanceof User user) subjectChanged.accept(user.getUniqueId());
            else allChanged.run();
        });
        subscribe(GroupLoadEvent.class, event -> allChanged.run());
        subscribe(GroupCreateEvent.class, event -> allChanged.run());
        subscribe(GroupDeleteEvent.class, event -> allChanged.run());
        subscribe(GroupLoadAllEvent.class, event -> allChanged.run());
        subscribe(PostSyncEvent.class, event -> allChanged.run());
        subscribe(ConfigReloadEvent.class, event -> allChanged.run());
    }

    private <T extends LuckPermsEvent> void subscribe(Class<T> type, Consumer<T> handler) {
        EventSubscription<T> subscription = events.subscribe(type, event -> {
            synchronized (this) {
                if (!closed) handler.accept(event);
            }
        });
        subscriptions.add(subscription);
    }

    @Override
    public void close() {
        synchronized (this) {
            closed = true;
        }
        RuntimeException failure = null;
        for (var iterator = subscriptions.iterator(); iterator.hasNext();) {
            try {
                iterator.next().close();
                iterator.remove();
            } catch (RuntimeException | LinkageError exception) {
                if (failure == null) failure = new IllegalStateException("LuckPerms event cleanup is incomplete");
                failure.addSuppressed(exception);
            }
        }
        if (failure != null) throw failure;
    }
}
