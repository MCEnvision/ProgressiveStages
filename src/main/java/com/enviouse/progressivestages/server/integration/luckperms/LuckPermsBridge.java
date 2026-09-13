package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.server.enforcement.InteractionCaptureManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** optional luckperms bridge with attributable source grants and bounded reconciliation. */
public final class LuckPermsBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SUBJECTS_PER_TICK = 16;
    private static final int MAX_QUEUE = 256;
    private static LuckPermsBridge INSTANCE;

    private final SubjectReconciliationQueue dirty = new SubjectReconciliationQueue(MAX_QUEUE);
    private final OutboundNodeTracker outboundNodes = new OutboundNodeTracker();
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
    public static void disconnect(ServerPlayer player) {
        LuckPermsBridge bridge = getInstance();
        synchronized (bridge) {
            bridge.dirty.remove(player.getUUID());
            bridge.dirty.requestRescan();
            if (bridge.adapter != null && !bridge.outboundNodes.reconcile(player.getUUID(), Map.of(),
                    bridge.adapter, (owner, adding, result) -> {})) {
                LOGGER.warn("LuckPerms output cleanup is incomplete after disconnect. Owned references are retained.");
            }
        }
    }
    public static void reconcileAll() {
        LuckPermsBridge bridge = getInstance();
        if (bridge.server != null) bridge.server.getPlayerList().getPlayers().forEach(bridge::reconcileSubject);
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
        if (adapter != null && (!outboundNodes.cleanup(adapter) || !adapter.cleanupTransientNodes())) {
            throw new IllegalStateException("Owned LuckPerms output cleanup is incomplete");
        }
        if (adapter != null) adapter.shutdown();
        adapter = replacement == null ? ReflectiveLuckPermsAdapter.create() : replacement;
    }

    private synchronized void bind(MinecraftServer value) {
        if (!shutdownOwnedOutput()) return;
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
        shutdownOwnedOutput();
    }

    private boolean shutdownOwnedOutput() {
        if (stageSubscription != null) {
            try { stageSubscription.close(); } catch (Exception ignored) {}
            stageSubscription = null;
        }
        boolean cleaned = adapter == null || outboundNodes.cleanup(adapter);
        if (adapter != null) {
            cleaned &= adapter.cleanupTransientNodes();
            adapter.shutdown();
        }
        if (registered) {
            NeoForge.EVENT_BUS.unregister(this);
            registered = false;
        }
        dirty.clear();
        if (cleaned) adapter = null;
        else LOGGER.warn("LuckPerms output cleanup is incomplete. The bridge remains unavailable until cleanup succeeds.");
        server = null;
        return cleaned;
    }

    private synchronized void markDirty(UUID subject) {
        dirty.request(subject);
    }

    private void drain(MinecraftServer current) {
        if (server != current || adapter == null) return;
        List<ServerPlayer> players = current.getPlayerList().getPlayers();
        int count = 0;
        while (count++ < MAX_SUBJECTS_PER_TICK) {
            UUID id;
            synchronized (this) {
                id = dirty.poll(players::size, index -> players.get(index).getUUID());
                if (id == null) return;
            }
            ServerPlayer player = current.getPlayerList().getPlayer(id);
            if (player != null) reconcileSubject(player);
        }
    }

    private void reconcileSubject(ServerPlayer player) {
        if (player == null || adapter == null) return;
        StageManager.getInstance().withdrawObsoletePermissionOwners(player);
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
        StageManager manager = StageManager.getInstance();
        UUID subject = player.getUUID();
        var activeRows = new java.util.HashSet<String>();
        for (LuckPermsStageOptions.InboundRule row : options.inbound()) {
            String synchronizedSource = new PermissionStageSource(subject, row.id(), false).label();
            boolean permanent = options.inboundMode() == LuckPermsStageOptions.InboundMode.PERMANENT;
            String selected = new PermissionStageSource(subject, row.id(), permanent).label();
            boolean eligible = ready && options.present() && options.enabled() && matches(subject, row, snapshot)
                && manager.canGrantStageFromSource(player, definition.getId());
            if (eligible) {
                activeRows.add(row.id());
                boolean added = manager.grantStageFromSource(player, definition.getId(), selected, StageCause.PERMISSION);
                if (!added && !manager.getStageSources(player, definition.getId()).contains(selected)) {
                    record(player, definition.getId(), row.id(), false, "dependency_denied");
                }
            }
            if (!eligible || permanent) {
                manager.revokeStageFromSource(player, definition.getId(), synchronizedSource, StageCause.PERMISSION);
            }
        }
        for (String label : manager.getStageSources(player, definition.getId())) {
            PermissionStageSource.parse(label).filter(source -> source.subject().equals(subject)
                && !source.permanent() && !activeRows.contains(source.row())).ifPresent(source ->
                    manager.revokeStageFromSource(player, definition.getId(), label, StageCause.PERMISSION));
        }
    }

    private boolean matches(UUID player, LuckPermsStageOptions.InboundRule row,
                            LuckPermsAdapter.SubjectSnapshot snapshot) {
        List<Boolean> conditions = new ArrayList<>();
        for (String group : row.groups()) {
            conditions.add(snapshot.groups().contains(group));
        }
        for (String permission : row.permissions()) {
            LuckPermsAdapter.PermissionResult result = adapter.permissionResult(player, permission);
            conditions.add(result.ready() && result.value() == LuckPermsAdapter.PermissionValue.TRUE);
        }
        boolean conditionMatch = row.match() == LuckPermsStageOptions.Match.ANY
            ? conditions.stream().anyMatch(Boolean.TRUE::equals)
            : conditions.stream().allMatch(Boolean.TRUE::equals);
        if (!conditionMatch) return false;
        return contextMatches(row.contexts(), snapshot.contexts());
    }

    private void reconcileOutbound(ServerPlayer player, LuckPermsAdapter.SubjectSnapshot snapshot, boolean ready) {
        UUID id = player.getUUID();
        Map<String, LuckPermsAdapter.NodeSpec> desired = new LinkedHashMap<>();
        if (ready) {
            for (StageId stageId : StageManager.getInstance().getStages(player)) {
                StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
                if (definition == null || !definition.getLuckPerms().present()
                        || !definition.getLuckPerms().enabled()) continue;
                for (LuckPermsStageOptions.OutboundRule row : definition.getLuckPerms().outbound()) {
                    if (!contextMatches(row.contexts(), snapshot.contexts())) continue;
                    if (row.kind() == LuckPermsStageOptions.OutboundKind.GROUP
                            && !adapter.groupExists(row.value())) continue;
                    if (row.kind() == LuckPermsStageOptions.OutboundKind.PERMISSION) {
                        var permission = adapter.permissionResult(id, row.value());
                        if (!permission.ready() || permission.value() == LuckPermsAdapter.PermissionValue.FALSE) {
                            record(player, stageId, row.id(), false,
                                permission.ready() ? "permission_false" : "provider_unavailable");
                            continue;
                        }
                    }
                    int contextIndex = 0;
                    for (Map<String, String> contexts : contextCombinations(row.contexts())) {
                        String key = stageId + "|" + row.id() + "|" + contextIndex++;
                        desired.put(key, new LuckPermsAdapter.NodeSpec(
                            row.kind() == LuckPermsStageOptions.OutboundKind.GROUP
                                ? LuckPermsAdapter.NodeKind.GROUP : LuckPermsAdapter.NodeKind.PERMISSION,
                            row.value(), contexts));
                    }
                }
            }
        }
        outboundNodes.reconcile(id, desired, adapter, (owner, adding, result) -> {
            int separator = owner.indexOf('|');
            StageId stage = separator > 0 ? StageId.tryParse(owner.substring(0, separator)) : null;
            String row = separator > 0 ? owner.substring(separator + 1) : owner;
            String reason = result == LuckPermsAdapter.MutationResult.APPLIED
                ? (adding ? "owned_node_added" : "owned_node_removed")
                : "owned_node_" + result.name().toLowerCase(java.util.Locale.ROOT);
            record(player, stage, row, adding, reason);
        });
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
                                          Map<String, Set<String>> actual) {
        for (var entry : required.entrySet()) {
            Set<String> values = actual.getOrDefault(entry.getKey().toLowerCase(java.util.Locale.ROOT), Set.of());
            if (entry.getValue().stream().map(value -> value.toLowerCase(java.util.Locale.ROOT))
                    .noneMatch(values::contains)) return false;
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
