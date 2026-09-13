package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
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
    private final java.util.concurrent.atomic.AtomicLong inputRevision = new java.util.concurrent.atomic.AtomicLong();
    private final OutboundNodeTracker outboundNodes = new OutboundNodeTracker();
    private final Map<UUID, StageManager.OfflinePermissionContext> offlineContexts = new LinkedHashMap<>();
    private final SubjectReconciliationQueue.ScanSource scanSource = new SubjectReconciliationQueue.ScanSource() {
        private int onlineIndex;
        private UUID offlineAfter;
        private boolean offlineFinished;
        @Override public void reset() { onlineIndex = 0; offlineAfter = null; offlineFinished = false; }
        @Override public UUID next() {
            if (server == null) return null;
            List<ServerPlayer> online = server.getPlayerList().getPlayers();
            if (onlineIndex < online.size()) return online.get(onlineIndex++).getUUID();
            if (offlineFinished) return null;
            offlineAfter = StageManager.getInstance().nextPermissionSubject(offlineAfter);
            offlineFinished = offlineAfter == null;
            return offlineAfter;
        }
    };
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
            bridge.dirty.request(player.getUUID());
            if (bridge.adapter != null) {
                bridge.adapter.invalidateOffline(player.getUUID());
                boolean complete = bridge.adapter.invalidateProjection(player.getUUID());
                complete &= bridge.outboundNodes.reconcile(player.getUUID(), Map.of(),
                    bridge.adapter, (owner, adding, result) -> {});
                if (!complete) LOGGER.warn("LuckPerms output cleanup is incomplete after disconnect. Owned references are retained.");
            }
        }
    }
    public static void reconcileAll() {
        LuckPermsBridge bridge = getInstance();
        synchronized (bridge) {
            if (bridge.adapter != null && !bridge.adapter.invalidateProjections()) {
                LOGGER.warn("LuckPerms projection invalidation is incomplete after reload.");
            }
            if (bridge.adapter != null) bridge.adapter.invalidateOffline();
            bridge.dirty.requestRescan();
        }
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
        if (!closeAdapter()) {
            throw new IllegalStateException("Owned LuckPerms output cleanup is incomplete");
        }
        adapter = replacement == null ? ReflectiveLuckPermsAdapter.create() : replacement;
        offlineContexts.clear();
        adapter.subscribeChanges(this::providerChanged, this::providerChanged);
    }

    private boolean closeAdapter() {
        inputRevision.incrementAndGet();
        if (adapter == null) return true;
        boolean complete = adapter.stopListening();
        complete &= adapter.invalidateProjections();
        complete &= outboundNodes.cleanup(adapter);
        complete &= adapter.cleanupTransientNodes();
        complete &= adapter.shutdown();
        return complete;
    }

    private synchronized void bind(MinecraftServer value) {
        if (!shutdownOwnedOutput()) return;
        server = value;
        adapter = ReflectiveLuckPermsAdapter.create();
        adapter.subscribeChanges(this::providerChanged, this::providerChanged);
        dirty.requestRescan();
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
        boolean cleaned = closeAdapter();
        if (registered) {
            NeoForge.EVENT_BUS.unregister(this);
            registered = false;
        }
        dirty.clear();
        offlineContexts.clear();
        if (cleaned) adapter = null;
        else LOGGER.warn("LuckPerms output cleanup is incomplete. The bridge remains unavailable until cleanup succeeds.");
        server = null;
        return cleaned;
    }

    private void providerChanged(UUID subject) {
        inputRevision.incrementAndGet();
        dirty.request(subject);
    }

    private void providerChanged() {
        inputRevision.incrementAndGet();
        dirty.requestRescan();
    }

    private synchronized void markDirty(UUID subject) {
        if (subject != null && adapter != null) adapter.invalidateProjection(subject);
        dirty.request(subject);
    }

    private void drain(MinecraftServer current) {
        if (server != current || adapter == null) return;
        int count = 0;
        while (count++ < MAX_SUBJECTS_PER_TICK) {
            UUID id;
            synchronized (this) {
                id = dirty.poll(scanSource);
                if (id == null) return;
            }
            ServerPlayer player = current.getPlayerList().getPlayer(id);
            if (player != null) {
                if (offlineContexts.remove(id) != null) adapter.invalidateOffline(id);
                if (adapter.takeOffline(id) != null) adapter.completeOffline(id);
                reconcileSubject(player);
            } else reconcileOffline(id);
        }
    }

    private void reconcileOffline(UUID subject) {
        StageManager manager = StageManager.getInstance();
        if (adapter.state() != LuckPermsAdapter.State.READY) {
            manager.deactivateOfflinePermissionSources(subject);
            return;
        }
        var context = offlineContexts.get(subject);
        if (context == null) {
            if (adapter.takeOffline(subject) != null) adapter.completeOffline(subject);
            if (offlineContexts.size() >= 8) { dirty.request(subject); return; }
            Set<String> permissions = new java.util.HashSet<>();
            for (StageId stage : StageOrder.getInstance().getOrderedStages()) {
                StageOrder.getInstance().getStageDefinition(stage).orElseThrow().getLuckPerms().inbound()
                    .forEach(row -> permissions.addAll(row.permissions()));
            }
            context = manager.captureOfflinePermissionContext(subject);
            manager.deactivateOfflinePermissionSources(subject);
            if (adapter.requestOffline(subject, permissions, dirty::request)) offlineContexts.put(subject, context);
            else dirty.request(subject);
            return;
        }
        LuckPermsAdapter.OfflineResult result = adapter.takeOffline(subject);
        if (result == null) return;
        offlineContexts.remove(subject);
        if (result.stale()) { adapter.completeOffline(subject); dirty.request(subject); return; }
        if (!result.snapshot().ready()) { adapter.completeOffline(subject); return; }
        Map<StageId, Set<String>> desired = new LinkedHashMap<>();
        Map<StageId, Map<PermissionStageSource, StageManager.PermissionObservation>> observations = new LinkedHashMap<>();
        for (StageId stage : StageOrder.getInstance().getOrderedStages()) {
            LuckPermsStageOptions options = StageOrder.getInstance().getStageDefinition(stage).orElseThrow().getLuckPerms();
            if (!options.present() || !options.enabled()) continue;
            for (var row : options.inbound()) {
                var source = new PermissionStageSource(subject, row.id(),
                    options.inboundMode() == LuckPermsStageOptions.InboundMode.PERMANENT);
                var observation = PermissionEligibility.observe(row, result.snapshot());
                if (observation != null) {
                    observations.computeIfAbsent(stage, ignored -> new LinkedHashMap<>()).put(source, observation);
                    if (observation.eligible() && contextMatches(row.contexts(), result.snapshot().contexts())) {
                        desired.computeIfAbsent(stage, ignored -> new java.util.HashSet<>()).add(source.label());
                    }
                }
            }
        }
        try {
            if (!manager.reconcileOfflinePermissionSources(context, desired, observations, () -> adapter.isOfflineCurrent(subject))) dirty.request(subject);
        } finally {
            adapter.completeOffline(subject);
        }
    }

    private void reconcileSubject(ServerPlayer player) {
        if (player == null || adapter == null) return;
        if (!player.server.isSameThread()) {
            dirty.request(player.getUUID());
            return;
        }
        StageManager manager = StageManager.getInstance();
        manager.expirePermissionSources(player);
        LuckPermsAdapter inputAdapter = adapter;
        var providerState = inputAdapter.state();
        boolean providerReady = providerState == LuckPermsAdapter.State.READY;
        long ticket = providerReady ? inputAdapter.prepareProjection(player.getUUID(), player) : -1;
        if (providerReady && ticket < 0) {
            dirty.request(player.getUUID());
            return;
        }
        if (!providerReady) inputAdapter.invalidateProjection(player.getUUID());
        long revision = inputRevision.get();
        long membership = TeamProvider.getInstance().membershipRevision();
        long definitionsRevision = StageFileLoader.getInstance().getCompiledSnapshot().revision();
        long stagesRevision = manager.getMutationRevision();
        List<StageDefinition> definitions = StageOrder.getInstance().getOrderedStages().stream()
            .map(id -> StageOrder.getInstance().getStageDefinition(id).orElseThrow()).toList();
        var owners = new LinkedHashMap<StageId, com.enviouse.progressivestages.common.stage.OwnerRef>();
        definitions.forEach(definition -> owners.put(definition.getId(), manager.getStageOwner(player, definition.getId())));
        var input = OnlinePermissionInput.capture(inputAdapter, player.getUUID(), definitions, providerReady);
        boolean current = adapter == inputAdapter && inputAdapter.state() == providerState
            && inputRevision.get() == revision && TeamProvider.getInstance().membershipRevision() == membership
            && StageFileLoader.getInstance().getCompiledSnapshot().revision() == definitionsRevision
            && manager.getMutationRevision() == stagesRevision
            && definitions.equals(StageOrder.getInstance().getOrderedStages().stream()
                .map(id -> StageOrder.getInstance().getStageDefinition(id).orElseThrow()).toList())
            && owners.entrySet().stream().allMatch(entry -> entry.getValue().equals(manager.getStageOwner(player, entry.getKey())));
        if (!current) {
            dirty.request(player.getUUID());
            return;
        }
        if (providerReady && !input.snapshot().ready()) dirty.request(player.getUUID());
        manager.withdrawObsoletePermissionOwners(player);
        for (StageDefinition definition : definitions) {
            reconcileInbound(player, definition, input.snapshot(), input.observations().getOrDefault(definition.getId(), Map.of()));
        }
        reconcileOutbound(player, input.snapshot(), input.snapshot().ready(), ticket);
    }

    private void reconcileInbound(ServerPlayer player, StageDefinition definition,
                                  LuckPermsAdapter.SubjectSnapshot snapshot, Map<String, StageManager.PermissionObservation> observations) {
        LuckPermsStageOptions options = definition.getLuckPerms();
        StageManager manager = StageManager.getInstance();
        UUID subject = player.getUUID();
        var activeRows = new java.util.HashSet<String>();
        for (LuckPermsStageOptions.InboundRule row : options.inbound()) {
            String synchronizedSource = new PermissionStageSource(subject, row.id(), false).label();
            boolean permanent = options.inboundMode() == LuckPermsStageOptions.InboundMode.PERMANENT;
            String selected = new PermissionStageSource(subject, row.id(), permanent).label();
            var source = new PermissionStageSource(subject, row.id(), permanent);
            var observation = observations.get(row.id());
            var owner = manager.getStageOwner(player, definition.getId());
            if (observation != null) manager.observePermissionEligibility(owner, definition.getId(), source, observation);
            String denial = manager.permissionEpisodeDenial(owner, definition.getId(), source);
            boolean eligible = observation != null && observation.eligible() && denial.isEmpty()
                && contextMatches(row.contexts(), snapshot.contexts())
                && manager.canGrantStageFromSource(player, definition.getId());
            if (!denial.isEmpty()) {
                record(player, definition.getId(), row.id(), false, denial);
                manager.revokeStageFromSource(player, definition.getId(),
                    new PermissionStageSource(subject, row.id(), true).label(), StageCause.PERMISSION);
            }
            if (eligible) {
                activeRows.add(row.id());
                boolean added = manager.grantStageFromSource(player, definition.getId(), selected, StageCause.PERMISSION);
                if (!added && !manager.getStageSources(player, definition.getId()).contains(selected)) {
                    String reason = manager.permissionEpisodeDenial(owner, definition.getId(), source);
                    record(player, definition.getId(), row.id(), false, reason.isEmpty() ? "dependency_denied" : reason);
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

    StageManager.PermissionObservation observe(UUID player, LuckPermsStageOptions.InboundRule row,
                                                       LuckPermsAdapter.SubjectSnapshot snapshot) {
        Map<String, LuckPermsAdapter.PermissionValue> values = new LinkedHashMap<>();
        for (String permission : row.permissions()) {
            LuckPermsAdapter.PermissionResult result = adapter.permissionResult(player, permission);
            if (!result.ready()) return null;
            values.put(permission, result.value());
        }
        return PermissionEligibility.observe(row, new LuckPermsAdapter.SubjectSnapshot(
            snapshot.ready(), snapshot.groups(), values, snapshot.contexts()));
    }

    private void reconcileOutbound(ServerPlayer player, LuckPermsAdapter.SubjectSnapshot snapshot, boolean ready,
                                   long ticket) {
        UUID id = player.getUUID();
        Map<String, LuckPermsAdapter.NodeSpec> desired = new LinkedHashMap<>();
        if (ready && ticket >= 0) {
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
        boolean complete = outboundNodes.reconcile(id, desired, adapter, (owner, adding, result) -> {
            int separator = owner.indexOf('|');
            StageId stage = separator > 0 ? StageId.tryParse(owner.substring(0, separator)) : null;
            String row = separator > 0 ? owner.substring(separator + 1) : owner;
            String reason = result == LuckPermsAdapter.MutationResult.APPLIED
                ? (adding ? "owned_node_added" : "owned_node_removed")
                : "owned_node_" + result.name().toLowerCase(java.util.Locale.ROOT);
            record(player, stage, row, adding, reason);
        });
        if (complete && ready && adapter.state() == LuckPermsAdapter.State.READY && ticket >= 0 && !desired.isEmpty()) {
            if (!adapter.publishProjection(id, ticket)) dirty.request(id);
        } else {
            adapter.invalidateProjection(id);
            if (!complete || ready && ticket < 0) dirty.request(id);
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

    static boolean contextMatches(Map<String, List<String>> required,
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
        if (event.getEntity() instanceof ServerPlayer player) {
            inputRevision.incrementAndGet();
            markDirty(player.getUUID());
        }
    }
}
