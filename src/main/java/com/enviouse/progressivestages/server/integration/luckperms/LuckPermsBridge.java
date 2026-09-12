package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageMutationResult;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.server.enforcement.InteractionCaptureManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/** optional luckperms bridge with attributable source grants and bounded reconciliation. */
public final class LuckPermsBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SUBJECTS_PER_TICK = 16;
    private static final int MAX_QUEUE = 256;
    private static final String SOURCE_PREFIX = "luckperms:";
    private static LuckPermsBridge INSTANCE;

    private final Queue<UUID> dirty = new ArrayDeque<>();
    private final Set<UUID> queued = new HashSet<>();
    private final Map<UUID, Map<String, OutboundEntry>> outboundManifest = new HashMap<>();
    private boolean rescanRequested;
    private MinecraftServer server;
    private LuckPermsAdapter adapter;
    private AutoCloseable stageSubscription;
    private boolean registered;

    private LuckPermsBridge() {}

    public static LuckPermsBridge getInstance() {
        if (INSTANCE == null) INSTANCE = new LuckPermsBridge();
        return INSTANCE;
    }

    public static void initialize(MinecraftServer server) { getInstance().bind(server); }
    public static void tick(MinecraftServer server) { getInstance().drain(server); }
    public static void reconcile(ServerPlayer player) { getInstance().reconcileSubject(player); }
    public static void reconcileAll() {
        LuckPermsBridge bridge = getInstance();
        if (bridge.server != null) bridge.server.getPlayerList().getPlayers().forEach(bridge::markAndReconcile);
    }
    public static StageCapabilitiesView capabilities() { return getInstance().capabilitiesView(); }
    public static boolean groupExists(String group) {
        LuckPermsBridge bridge = getInstance();
        return bridge.adapter != null && bridge.adapter.groupExists(group);
    }

    public static com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus groupStatus(String group) {
        LuckPermsBridge bridge = getInstance();
        return bridge.adapter == null || bridge.adapter.state() != LuckPermsAdapter.State.READY
            ? com.enviouse.progressivestages.common.stage.StageCapabilities.GroupStatus.UNKNOWN
            : bridge.adapter.groupStatus(group);
    }

    public synchronized void setAdapterForTests(LuckPermsAdapter replacement) {
        adapter = replacement == null ? ReflectiveLuckPermsAdapter.create() : replacement;
    }

    private synchronized void bind(MinecraftServer value) {
        shutdown();
        server = value;
        adapter = ReflectiveLuckPermsAdapter.create();
        try {
            stageSubscription = StageManager.getInstance().subscribeCommittedStageChanges(result -> {
                for (UUID subject : result.affectedPlayers()) markDirty(subject);
            });
        } catch (RuntimeException exception) {
            LOGGER.warn("failed to subscribe luckperms bridge", exception);
        }
        if (!registered) {
            NeoForge.EVENT_BUS.register(this);
            registered = true;
        }
    }

    public synchronized void shutdown() {
        if (stageSubscription != null) {
            try { stageSubscription.close(); } catch (Exception ignored) {}
            stageSubscription = null;
        }
        if (adapter != null) adapter.shutdown();
        if (registered) {
            NeoForge.EVENT_BUS.unregister(this);
            registered = false;
        }
        dirty.clear();
        queued.clear();
        outboundManifest.clear();
        rescanRequested = false;
        adapter = null;
        server = null;
    }

    private synchronized void markDirty(UUID subject) {
        if (subject == null || queued.contains(subject)) return;
        if (dirty.size() >= MAX_QUEUE) {
            dirty.clear();
            queued.clear();
            rescanRequested = true;
        }
        dirty.add(subject);
        queued.add(subject);
    }

    private void drain(MinecraftServer current) {
        if (server != current || adapter == null) return;
        synchronized (this) {
            if (rescanRequested) {
                rescanRequested = false;
                for (ServerPlayer player : current.getPlayerList().getPlayers()) markDirty(player.getUUID());
            }
        }
        int count = 0;
        while (count++ < MAX_SUBJECTS_PER_TICK) {
            UUID id;
            synchronized (this) {
                id = dirty.poll();
                if (id == null) return;
                queued.remove(id);
            }
            ServerPlayer player = current.getPlayerList().getPlayer(id);
            if (player != null) reconcileSubject(player);
        }
    }

    private void markAndReconcile(ServerPlayer player) { reconcileSubject(player); }

    private void reconcileSubject(ServerPlayer player) {
        if (player == null || adapter == null) return;
        LuckPermsAdapter.SubjectSnapshot snapshot = adapter.snapshot(player.getUUID());
        boolean ready = adapter.state() == LuckPermsAdapter.State.READY && snapshot.ready();
        for (StageId stageId : StageOrder.getInstance().getOrderedStages()) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
            if (definition == null) continue;
            reconcileInbound(player, definition, snapshot, ready);
        }
        reconcileOutbound(player, snapshot, ready);
    }

    private void reconcileInbound(ServerPlayer player, StageDefinition definition,
                                  LuckPermsAdapter.SubjectSnapshot snapshot, boolean ready) {
        LuckPermsStageOptions options = definition.getLuckPerms();
        if (!options.present() || options.inbound().isEmpty()) return;
        for (LuckPermsStageOptions.InboundRule row : options.inbound()) {
            String synchronizedSource = source(LuckPermsStageOptions.InboundMode.SYNCHRONIZED, row.id());
            String permanentSource = source(LuckPermsStageOptions.InboundMode.PERMANENT, row.id());
            boolean eligible = ready && options.enabled() && matches(player.getUUID(), row, snapshot);
            String selected = source(options.inboundMode(), row.id());
            if (eligible) {
                if (!StageManager.getInstance().grantStageFromSource(player, definition.getId(), selected,
                        StageCause.TRIGGER)) {
                    record(player, definition.getId(), row.id(), false, "dependency_denied");
                }
                if (!selected.equals(synchronizedSource)) {
                    StageManager.getInstance().revokeStageFromSource(player, definition.getId(), synchronizedSource,
                        StageCause.TRIGGER);
                }
            } else if (options.inboundMode() == LuckPermsStageOptions.InboundMode.SYNCHRONIZED) {
                StageManager.getInstance().revokeStageFromSource(player, definition.getId(), synchronizedSource,
                    StageCause.TRIGGER);
            }
            if (!eligible && options.inboundMode() == LuckPermsStageOptions.InboundMode.PERMANENT) {
                StageManager.getInstance().revokeStageFromSource(player, definition.getId(), synchronizedSource,
                    StageCause.TRIGGER);
            }
            if (eligible && options.inboundMode() == LuckPermsStageOptions.InboundMode.PERMANENT) {
                StageManager.getInstance().grantStageFromSource(player, definition.getId(), permanentSource,
                    StageCause.TRIGGER);
            }
        }
    }

    private static String source(LuckPermsStageOptions.InboundMode mode, String row) {
        return SOURCE_PREFIX + mode.label() + ":" + row;
    }

    private boolean matches(UUID player, LuckPermsStageOptions.InboundRule row,
                            LuckPermsAdapter.SubjectSnapshot snapshot) {
        List<Boolean> conditions = new ArrayList<>();
        for (String group : row.groups()) {
            conditions.add(snapshot.groups().contains(group) && !isBridgeOwned(player, group));
        }
        for (String permission : row.permissions()) {
            LuckPermsAdapter.PermissionValue value = adapter.permission(player, permission);
            conditions.add(value == LuckPermsAdapter.PermissionValue.TRUE && !isBridgeOwned(player, permission));
        }
        boolean conditionMatch = row.match() == LuckPermsStageOptions.Match.ANY
            ? conditions.stream().anyMatch(Boolean.TRUE::equals)
            : conditions.stream().allMatch(Boolean.TRUE::equals);
        if (!conditionMatch) return false;
        for (var context : row.contexts().entrySet()) {
            String actual = snapshot.contexts().get(context.getKey());
            if (actual == null || !context.getValue().contains(actual)) return false;
        }
        return true;
    }

    private boolean isBridgeOwned(UUID player, String value) {
        Map<String, OutboundEntry> owned = outboundManifest.getOrDefault(player, Map.of());
        return owned.values().stream().anyMatch(entry -> entry.value().equals(value));
    }

    private void reconcileOutbound(ServerPlayer player, LuckPermsAdapter.SubjectSnapshot snapshot, boolean ready) {
        UUID id = player.getUUID();
        Map<String, OutboundEntry> desired = new LinkedHashMap<>();
        if (ready) {
            for (StageId stageId : StageManager.getInstance().getStages(player)) {
                StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
                if (definition == null || !definition.getLuckPerms().present()
                        || !definition.getLuckPerms().enabled()) continue;
                for (LuckPermsStageOptions.OutboundRule row : definition.getLuckPerms().outbound()) {
                    if (!contextMatches(row.contexts(), snapshot.contexts())) continue;
                    if (row.kind() == LuckPermsStageOptions.OutboundKind.GROUP
                            && !adapter.groupExists(row.value())) continue;
                    int contextIndex = 0;
                    for (Map<String, String> contexts : contextCombinations(row.contexts())) {
                        String key = stageId + "|" + row.id() + "|" + contextIndex++;
                        OutboundEntry entry = new OutboundEntry(
                            row.kind() == LuckPermsStageOptions.OutboundKind.GROUP
                                ? LuckPermsAdapter.NodeKind.GROUP : LuckPermsAdapter.NodeKind.PERMISSION,
                            row.value(), contexts, key);
                        desired.put(key, entry);
                        Map<String, OutboundEntry> previous = outboundManifest.getOrDefault(id, Map.of());
                        boolean nodeAlreadyOwned = previous.values().stream().anyMatch(existing -> existing.sameNode(entry));
                        if (!nodeAlreadyOwned) {
                            adapter.addTransient(id, entry.kind(), entry.value(), entry.contexts(), entry.ownerKey());
                            record(player, stageId, row.id(), true, "owned_node_added");
                        }
                    }
                }
            }
        }
        Map<String, OutboundEntry> previous = outboundManifest.getOrDefault(id, Map.of());
        for (Map.Entry<String, OutboundEntry> previousEntry : previous.entrySet()) {
            OutboundEntry entry = previousEntry.getValue();
            if (desired.containsKey(previousEntry.getKey())) continue;
            if (desired.values().stream().anyMatch(existing -> existing.sameNode(entry))) continue;
            adapter.removeTransient(id, entry.kind(), entry.value(), entry.contexts(), entry.ownerKey());
            int separator = previousEntry.getKey().indexOf('|');
            StageId stage = separator > 0 ? StageId.tryParse(previousEntry.getKey().substring(0, separator)) : null;
            String row = separator > 0 ? previousEntry.getKey().substring(separator + 1) : previousEntry.getKey();
            record(player, stage, row, false, "owned_node_removed");
        }
        outboundManifest.put(id, desired);
    }

    private record OutboundEntry(LuckPermsAdapter.NodeKind kind, String value,
                                 Map<String, String> contexts, String ownerKey) {
        private boolean sameNode(OutboundEntry other) {
            return kind == other.kind && value.equals(other.value) && contexts.equals(other.contexts);
        }
    }

    private static List<Map<String, String>> contextCombinations(Map<String, List<String>> contexts) {
        List<Map<String, String>> combinations = new ArrayList<>();
        combinations.add(new LinkedHashMap<>());
        for (var context : contexts.entrySet()) {
            List<Map<String, String>> expanded = new ArrayList<>();
            for (Map<String, String> current : combinations) {
                for (String value : context.getValue()) {
                    Map<String, String> next = new LinkedHashMap<>(current);
                    next.put(context.getKey(), value);
                    expanded.add(next);
                }
            }
            combinations = expanded;
        }
        return combinations;
    }

    private static boolean contextMatches(Map<String, List<String>> required,
                                          Map<String, String> actual) {
        for (var entry : required.entrySet()) {
            String value = actual.get(entry.getKey());
            if (value == null || !entry.getValue().contains(value)) return false;
        }
        return true;
    }

    private void record(ServerPlayer player, StageId stage, String row, boolean desired, String reason) {
        if (player == null || stage == null) return;
        InteractionCaptureManager.recordPermission(player, stage, row, desired, reason,
            adapter == null ? "absent" : adapter.state().name().toLowerCase(java.util.Locale.ROOT));
    }

    private StageCapabilitiesView capabilitiesView() {
        LuckPermsAdapter.State state = adapter == null ? LuckPermsAdapter.State.ABSENT : adapter.state();
        return new StageCapabilitiesView(state.name().toLowerCase(java.util.Locale.ROOT), MAX_QUEUE,
            MAX_SUBJECTS_PER_TICK, dirty.size());
    }

    public record StageCapabilitiesView(String state, int queueLimit, int subjectsPerTick, int queuedSubjects) {}

    @SubscribeEvent
    public void onPermissionsChanged(PermissionsChangedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) markDirty(player.getUUID());
    }
}
