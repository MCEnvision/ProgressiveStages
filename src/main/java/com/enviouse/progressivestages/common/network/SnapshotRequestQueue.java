package com.enviouse.progressivestages.common.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

final class SnapshotRequestQueue {
    private static final int COOLDOWN_TICKS = 20;
    private final Map<UUID, Request> requests = new HashMap<>();

    boolean request(UUID player, long revision) {
        Request existing = requests.get(player);
        if (existing == null) {
            requests.put(player, new Request());
            return true;
        }
        long base = Math.max(0, revision);
        existing.revision = existing.pending && existing.revision == 0 ? 0 : base;
        existing.pending = true;
        return false;
    }

    void tick(BiConsumer<UUID, Long> sender) {
        var iterator = requests.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Request request = entry.getValue();
            if (--request.ticks > 0) continue;
            if (!request.pending) {
                iterator.remove();
                continue;
            }
            request.ticks = COOLDOWN_TICKS;
            request.pending = false;
            sender.accept(entry.getKey(), request.revision);
        }
    }

    void clear(UUID player) { requests.remove(player); }
    void clear() { requests.clear(); }

    private static final class Request {
        int ticks = COOLDOWN_TICKS;
        boolean pending;
        long revision;
    }
}
