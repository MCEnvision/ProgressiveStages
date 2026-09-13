package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageChangeEvent;
import com.enviouse.progressivestages.common.api.StageChangeType;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.api.StagesBulkChangedEvent;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.data.TeamStageData;
import com.enviouse.progressivestages.common.network.NetworkHandler;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.common.util.TextUtil;
import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Core stage management logic.
 * Handles granting, revoking, and checking stages.
 */
public class StageManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static StageManager INSTANCE;
    private MinecraftServer server;
    private long mutationRevision;
    private final CopyOnWriteArrayList<Consumer<StageMutationResult>> committedListeners = new CopyOnWriteArrayList<>();

    /** v2.4: synthetic "team" that holds server-wide ({@code scope = "server"}) stages shared by everyone. */
    public static final UUID SERVER_TEAM = new UUID(0L, 0L);

    /** One concrete stage removal from its real persistence owner. */
    private record RevokedStage(UUID owner, StageId stageId) {}

    private record GrantResult(List<StageId> granted, List<StageId> replaced, String denial) {
        private GrantResult {
            granted = List.copyOf(granted);
            replaced = List.copyOf(replaced);
            denial = denial == null ? "" : denial;
        }
    }

    private static boolean isServerScoped(StageId stageId) {
        return StageOrder.getInstance().getStageDefinition(stageId)
            .map(StageDefinition::isServerScope).orElse(false);
    }

    private static UUID storageTeam(UUID subjectTeam, StageId stageId) {
        return isServerScoped(stageId) ? SERVER_TEAM : subjectTeam;
    }

    private static OwnerRef owner(ServerPlayer player, StageId stageId) {
        return StageOwnership.owner(player, stageId);
    }

    private static boolean hasOwned(TeamStageData data, OwnerRef owner, StageId stageId) {
        return owner.kind() == OwnerKind.PERSONAL
            ? data.hasPersonalStage(owner.id(), stageId)
            : data.hasStage(owner.id(), stageId);
    }

    private static boolean hasIndependentOwnership(TeamStageData data, OwnerRef owner, StageId stageId) {
        if (!hasOwned(data, owner, stageId)) return false;
        Set<String> sources = data.getSources(owner, stageId);
        return sources.isEmpty() || sources.contains("independent");
    }

    private static boolean grantOwned(TeamStageData data, OwnerRef owner, StageId stageId) {
        return owner.kind() == OwnerKind.PERSONAL
            ? data.grantPersonalStage(owner.id(), stageId)
            : data.grantStage(owner.id(), stageId);
    }

    private static boolean revokeOwned(TeamStageData data, OwnerRef owner, StageId stageId) {
        return owner.kind() == OwnerKind.PERSONAL
            ? data.revokePersonalStage(owner.id(), stageId)
            : data.revokeStage(owner.id(), stageId);
    }

    private static boolean grantOwnedFromSource(TeamStageData data, OwnerRef owner,
                                                StageId stageId, String source) {
        return owner.kind() == OwnerKind.PERSONAL
            ? data.grantPersonalStageFromSource(owner.id(), stageId, source)
            : data.grantStageFromSource(owner.id(), stageId, source);
    }

    private void markMutation(boolean changed) {
        if (changed) mutationRevision++;
    }

    private void captureProgression(ServerPlayer player, StageId stageId, Set<StageId> before,
                                    Set<StageId> after, StageCause cause, String reason) {
        if (player == null || stageId == null) return;
        OwnerRef resolved = owner(player, stageId);
        int recipients = switch (resolved.kind()) {
            case SERVER -> server == null ? 1 : server.getPlayerList().getPlayers().size();
            case TEAM -> TeamProvider.getInstance().getTeamMembersForOwner(resolved.id(), player).size();
            case PERSONAL -> 1;
        };
        com.enviouse.progressivestages.server.enforcement.InteractionCaptureManager.recordProgression(
            player, stageId, before, after, cause == null ? "unknown" : cause.name().toLowerCase(Locale.ROOT),
            reason, recipients);
    }

    public AutoCloseable subscribeCommittedStageChanges(Consumer<StageMutationResult> listener) {
        Objects.requireNonNull(listener, "listener");
        committedListeners.add(listener);
        return () -> committedListeners.remove(listener);
    }

    private void publishMutation(ServerPlayer player, StageId requested, boolean changed, String reason,
                                 Set<OwnerRef> owners) {
        Set<UUID> players = affectedPlayers(player, changed, owners);
        StageMutationResult result = new StageMutationResult(changed, reason, mutationRevision, owners,
            Set.copyOf(players));
        for (Consumer<StageMutationResult> listener : committedListeners) {
            try { listener.accept(result); } catch (RuntimeException exception) {
                LOGGER.warn("Stage mutation listener failed", exception);
            }
        }
    }

    private Set<UUID> affectedPlayers(ServerPlayer player, boolean changed, Set<OwnerRef> owners) {
        Set<UUID> players = new LinkedHashSet<>();
        if (player != null) players.add(player.getUUID());
        if (changed && server != null && owners != null) {
            for (OwnerRef affected : owners) {
                switch (affected.kind()) {
                    case SERVER -> server.getPlayerList().getPlayers()
                        .forEach(candidate -> players.add(candidate.getUUID()));
                    case TEAM -> {
                        if (player != null) TeamProvider.getInstance().getTeamMembersForOwner(affected.id(), player)
                            .forEach(candidate -> players.add(candidate.getUUID()));
                        else for (ServerPlayer candidate : server.getPlayerList().getPlayers()) {
                            TeamProvider provider = TeamProvider.getInstance();
                            if (provider.getTeamId(candidate).equals(affected.id())
                                || provider.getFtbTeamId(candidate).equals(affected.id())) players.add(candidate.getUUID());
                        }
                    }
                    case PERSONAL -> {
                        if (player != null || server.getPlayerList().getPlayer(affected.id()) != null) players.add(affected.id());
                    }
                }
            }
        }
        return players;
    }

    public StageMutationResult mutateStage(StageActorContext context, StageId stageId,
                                           StageOperation operation, StageCause cause) {
        if (context == null || stageId == null || operation == null || server == null) {
            return new StageMutationResult(false, "invalid_context", mutationRevision, Set.of(), Set.of());
        }
        if (!server.isSameThread()) {
            return new StageMutationResult(false, "wrong_thread", mutationRevision, Set.of(), Set.of());
        }
        ServerPlayer player = server.getPlayerList().getPlayer(context.actorId());
        if (player == null) return new StageMutationResult(false, "actor_offline", mutationRevision, Set.of(), Set.of());
        if (context.membershipRevision() != TeamProvider.getInstance().membershipRevision()) {
            return new StageMutationResult(false, "stale_membership", mutationRevision, Set.of(), Set.of(player.getUUID()));
        }
        if (!owner(player, stageId).equals(context.owner())) {
            return new StageMutationResult(false, "owner_mismatch", mutationRevision, Set.of(), Set.of(player.getUUID()));
        }
        if (context.definitionRevision() != StageFileLoader.getInstance().getCompiledSnapshot().revision()) {
            return new StageMutationResult(false, "stale_revision", mutationRevision, Set.of(), Set.of(player.getUUID()));
        }
        Set<StageId> before = getStages(player);
        long beforeRevision = mutationRevision;
        if (operation == StageOperation.GRANT) grantStageWithCause(player, stageId, cause);
        else revokeStageWithCause(player, stageId, cause);
        Set<StageId> after = getStages(player);
        Set<OwnerRef> affectedOwners = new LinkedHashSet<>();
        affectedOwners.add(owner(player, stageId));
        Set<StageId> changedStages = new LinkedHashSet<>(before);
        changedStages.addAll(after);
        changedStages.removeIf(stage -> before.contains(stage) && after.contains(stage));
        changedStages.forEach(stage -> affectedOwners.add(owner(player, stage)));
        boolean changed = mutationRevision != beforeRevision;
        return new StageMutationResult(changed, changed ? "committed" : "already_owned", mutationRevision,
            affectedOwners, affectedPlayers(player, changed, affectedOwners));
    }

    public static StageManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StageManager();
        }
        return INSTANCE;
    }

    private StageManager() {}

    /**
     * Initialize the stage manager with the server
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.mutationRevision = 0L;
    }

    public void shutdown(MinecraftServer stoppingServer) {
        if (this.server == stoppingServer) {
            this.server = null;
            this.mutationRevision = 0L;
        }
    }

    /**
     * Get the team stage data storage
     */
    private TeamStageData getTeamStageData() {
        if (server == null) {
            return new TeamStageData();
        }
        ServerLevel overworld = server.overworld();
        return overworld.getData(StageAttachments.TEAM_STAGES);
    }

    /**
     * Check if a player has a specific stage
     */
    public boolean hasStage(ServerPlayer player, StageId stageId) {
        if (player == null || stageId == null) return false;
        TeamStageData data = getTeamStageData();
        return data.hasEffectiveStage(owner(player, stageId), stageId);
    }

    public boolean hasStoredStage(ServerPlayer player, StageId stageId) {
        return player != null && stageId != null && hasOwned(getTeamStageData(), owner(player, stageId), stageId);
    }

    public boolean hasIndependentStage(ServerPlayer player, StageId stageId) {
        return player != null && stageId != null
            && hasIndependentOwnership(getTeamStageData(), owner(player, stageId), stageId);
    }

    public OwnerRef getStageOwner(ServerPlayer player, StageId stageId) {
        return owner(player, stageId);
    }

    /**
     * Check if a team has a specific stage
     */
    public boolean hasStage(UUID teamId, StageId stageId) {
        TeamStageData data = getTeamStageData();
        // v2.4: server-wide stages live under SERVER_TEAM and count for every team.
        OwnerRef team = new OwnerRef(SERVER_TEAM.equals(teamId) ? OwnerKind.SERVER : OwnerKind.TEAM, teamId);
        return data.hasEffectiveStage(team, stageId)
            || data.hasEffectiveStage(new OwnerRef(OwnerKind.SERVER, SERVER_TEAM), stageId);
    }

    /**
     * Grant a stage to a player (optionally with prerequisites based on config)
     * Also grants to all team members if team mode is enabled.
     * Uses COMMAND as the default cause.
     */
    public void grantStage(ServerPlayer player, StageId stageId) {
        grantStageWithCause(player, stageId, StageCause.COMMAND);
    }

    /** grant a stage from a named derived source without conflating it with independent access. */
    public boolean grantStageFromSource(ServerPlayer player, StageId stageId, String source,
                                        StageCause cause) {
        if (player == null || stageId == null || source == null || source.isBlank()
                || !StageOrder.getInstance().stageExists(stageId)) return false;
        OwnerRef stageOwner = owner(player, stageId);
        TeamStageData data = getTeamStageData();
        Set<StageId> before = getStages(player);
        if (!canGrantStageFromSource(player, stageId)) return false;
        if (!preparePermissionGrant(stageOwner, stageId, source)) return false;
        boolean alreadyOwned = hasOwned(data, stageOwner, stageId);
        Set<String> previousSources = data.getSources(stageOwner, stageId);
        Set<String> previousEffectiveSources = data.getEffectiveSources(stageOwner, stageId);
        grantOwnedFromSource(data, stageOwner, stageId, source);
        boolean added = !previousSources.equals(data.getSources(stageOwner, stageId))
            || !previousEffectiveSources.equals(data.getEffectiveSources(stageOwner, stageId));
        if (!added) return false;
        markMutation(true);
        boolean permissionSource = PermissionStageSource.parse(source).isPresent();
        if (!alreadyOwned && !permissionSource) {
            fireStageChangeEvent(player, stageOwner.id(), stageId, StageChangeType.GRANTED, cause);
        }
        restorePermissionClock(stageOwner, stageId, source);
        syncStageView(player, stageId);
        if ((alreadyOwned || permissionSource) && !before.contains(stageId) && hasStage(player, stageId)) {
            for (UUID recipient : affectedPlayers(player, true, Set.of(stageOwner))) {
                ServerPlayer online = server.getPlayerList().getPlayer(recipient);
                if (online != null) fireBulkChangedEvent(online, StagesBulkChangedEvent.Reason.OTHER);
            }
        }
        publishMutation(player, stageId, true, "source_added", Set.of(stageOwner));
        captureProgression(player, stageId, before, getStages(player), cause, "source_added");
        return true;
    }

    public record PermissionObservation(String fingerprint, boolean eligible) {}

    public boolean observePermissionEligibility(OwnerRef owner, StageId stage, PermissionStageSource source,
                                                PermissionObservation observation) {
        if (observation == null) return false;
        TeamStageData data = getTeamStageData();
        PermissionEpisode previous = data.getPermissionEpisode(owner, stage, source);
        if (previous == null && !observation.eligible()) return false;
        PermissionEpisode next = previous == null
            ? new PermissionEpisode(owner, stage, source.subject(), source.row(), observation.fingerprint(), true, false, 0, 0)
            : previous.observe(observation.fingerprint(), observation.eligible());
        boolean changed = data.putPermissionEpisode(next);
        markMutation(changed);
        return changed;
    }

    public void expirePermissionSources(ServerPlayer player) {
        if (player == null || server == null) return;
        long now = System.currentTimeMillis();
        TeamStageData data = getTeamStageData();
        for (StageId stage : getStoredStages(player)) {
            OwnerRef resolved = owner(player, stage);
            for (String label : data.getSources(resolved, stage)) {
                var source = PermissionStageSource.parse(label);
                PermissionEpisode episode = source.isEmpty() ? null : data.getPermissionEpisode(resolved, stage, source.get());
                if (episode != null && episode.expired(now)) revokeStageFromSource(player, stage, label, StageCause.PERMISSION);
            }
        }
    }

    public String permissionEpisodeDenial(OwnerRef owner, StageId stage, PermissionStageSource source) {
        PermissionEpisode episode = getTeamStageData().getPermissionEpisode(owner, stage, source);
        if (episode == null) return "";
        if (episode.suppressed()) return "suppressed_episode";
        return episode.expired(System.currentTimeMillis()) ? "expired_episode" : "";
    }

    private boolean preparePermissionGrant(OwnerRef owner, StageId stage, String label) {
        var source = PermissionStageSource.parse(label);
        if (source.isEmpty()) return true;
        TeamStageData data = getTeamStageData();
        PermissionEpisode episode = data.getPermissionEpisode(owner, stage, source.get());
        if (episode == null) return true;
        if (!episode.positive() || episode.suppressed()) return false;
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stage).orElseThrow();
        PermissionEpisode acquired = episode.acquire(System.currentTimeMillis(),
            hasOwned(data, owner, stage) ? grantTime(owner, stage) : 0, definition.getDurationMillis());
        markMutation(data.putPermissionEpisode(acquired));
        return !acquired.expired(System.currentTimeMillis());
    }

    private void restorePermissionClock(OwnerRef owner, StageId stage, String label) {
        var source = PermissionStageSource.parse(label);
        PermissionEpisode episode = source.isEmpty() ? null : getTeamStageData().getPermissionEpisode(owner, stage, source.get());
        if (episode != null && episode.acquiredAt() > 0 && server != null) {
            var clocks = com.enviouse.progressivestages.server.triggers.StageRegressionData.get(server);
            long current = clocks.getGrantTime(owner, stage);
            long earliest = Long.MAX_VALUE;
            boolean independent = false;
            for (String active : getTeamStageData().getEffectiveSources(owner, stage)) {
                var parsed = PermissionStageSource.parse(active);
                PermissionEpisode other = parsed.isEmpty() ? null : getTeamStageData().getPermissionEpisode(owner, stage, parsed.get());
                if (other == null) independent = true;
                else if (other.acquiredAt() > 0) earliest = Math.min(earliest, other.acquiredAt());
            }
            if (independent && current > 0) earliest = Math.min(earliest, current);
            if (earliest != Long.MAX_VALUE && current != earliest) clocks.markGranted(owner, stage, earliest);
        }
    }

    public boolean withdrawObsoletePermissionOwners(ServerPlayer player) {
        if (player == null || server == null) return false;
        TeamStageData data = getTeamStageData();
        Set<OwnerRef> affectedOwners = new LinkedHashSet<>();
        for (TeamStageData.PermissionContribution contribution : data.getPermissionContributions(player.getUUID())) {
            if (contribution.source().permanent()) continue;
            StageId stage = contribution.stage();
            if (StageOrder.getInstance().stageExists(stage) && owner(player, stage).equals(contribution.owner())) continue;
            if (data.revokeStageFromSource(contribution.owner(), stage, contribution.source().label())) {
                affectedOwners.add(contribution.owner());
            }
        }
        if (affectedOwners.isEmpty()) return false;
        markMutation(true);
        for (UUID recipient : affectedPlayers(player, true, affectedOwners)) {
            ServerPlayer online = server.getPlayerList().getPlayer(recipient);
            if (online != null) {
                syncToPlayer(online);
                fireBulkChangedEvent(online, StagesBulkChangedEvent.Reason.OTHER);
            }
        }
        publishMutation(player, null, true, "source_owner_changed", Set.copyOf(affectedOwners));
        return true;
    }

    public boolean canGrantStageFromSource(ServerPlayer player, StageId stageId) {
        if (player == null || stageId == null) return false;
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        if (definition == null) return false;
        Set<StageId> effective = getStages(player);
        if (!StageOrder.getInstance().getMissingDependencies(effective, stageId).isEmpty()) return false;
        StageSlotResolver.Decision slot = slotDecision(player, definition, effective);
        return slot.allowed() && slot.replacements().isEmpty()
            && (!definition.isPurchasable() || hasOwned(getTeamStageData(), owner(player, stageId), stageId));
    }

    public record OfflinePermissionContext(UUID subject, long definitionRevision, Map<StageId, OwnerRef> owners,
                                            Map<StageId, StageDefinition> definitions, long membershipRevision) {
        public OfflinePermissionContext { owners = Map.copyOf(owners); definitions = Map.copyOf(definitions); }

        public OfflinePermissionContext(UUID subject, long definitionRevision, Map<StageId, OwnerRef> owners,
                                         Map<StageId, StageDefinition> definitions) {
            this(subject, definitionRevision, owners, definitions, TeamProvider.getInstance().membershipRevision());
        }
    }

    public OfflinePermissionContext captureOfflinePermissionContext(UUID subject) {
        Map<StageId, OwnerRef> owners = new LinkedHashMap<>();
        Map<StageId, StageDefinition> definitions = new LinkedHashMap<>();
        for (StageId stage : StageOrder.getInstance().getOrderedStages()) {
            StageOwnership.offlineOwner(subject, stage).ifPresent(owner -> owners.put(stage, owner));
            definitions.put(stage, StageOrder.getInstance().getStageDefinition(stage).orElseThrow());
        }
        return new OfflinePermissionContext(subject, StageFileLoader.getInstance().getCompiledSnapshot().revision(), owners, definitions);
    }

    public UUID nextPermissionSubject(UUID previous) {
        return getTeamStageData().nextPermissionSubject(previous);
    }

    public void deactivateOfflinePermissionSources(UUID subject) {
        if (server == null) return;
        notifyOfflinePermissionChange(getTeamStageData().deactivatePermissionSources(subject), "source_pending");
    }

    public boolean reconcileOfflinePermissionSources(OfflinePermissionContext context, Map<StageId, Set<String>> desired,
                                                     java.util.function.BooleanSupplier current) {
        return reconcileOfflinePermissionSources(context, desired, Map.of(), current);
    }

    public boolean reconcileOfflinePermissionSources(OfflinePermissionContext context, Map<StageId, Set<String>> desired,
            Map<StageId, Map<PermissionStageSource, PermissionObservation>> observations,
            java.util.function.BooleanSupplier current) {
        if (server == null || server.getPlayerList().getPlayer(context.subject()) != null
            || !current.getAsBoolean() || !context.equals(captureOfflinePermissionContext(context.subject()))) return false;
        TeamStageData data = getTeamStageData();
        List<PermissionEpisode> previousEpisodes = data.getPermissionEpisodes(context.subject());
        Set<OwnerRef> changed = new LinkedHashSet<>();
        long now = System.currentTimeMillis();
        for (var contribution : data.getPermissionContributions(context.subject())) {
            PermissionEpisode episode = data.getPermissionEpisode(contribution.owner(), contribution.stage(), contribution.source());
            if (episode != null && episode.expired(now)
                && data.revokeStageFromSource(contribution.owner(), contribution.stage(), contribution.source().label())) {
                changed.add(contribution.owner());
            }
        }
        observations.forEach((stage, rows) -> {
            OwnerRef owner = context.owners().get(stage);
            if (owner != null) rows.forEach((source, observation) -> {
                if (source.subject().equals(context.subject())) observePermissionEligibility(owner, stage, source, observation);
            });
        });
        List<TeamStageData.PermissionContribution> added = new ArrayList<>();
        for (TeamStageData.PermissionContribution contribution : data.getPermissionContributions(context.subject())) {
            if (contribution.source().permanent()) continue;
            if (!contribution.owner().equals(context.owners().get(contribution.stage()))
                || !desired.getOrDefault(contribution.stage(), Set.of()).contains(contribution.source().label())) {
                if (data.revokeStageFromSource(contribution.owner(), contribution.stage(), contribution.source().label())) {
                    changed.add(contribution.owner());
                }
            }
        }
        for (StageId stage : StageOrder.getInstance().getOrderedStages()) {
            OwnerRef resolved = context.owners().get(stage);
            if (resolved == null) continue;
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(stage).orElseThrow();
            Set<String> sources = desired.getOrDefault(stage, Set.of());
            Set<StageId> effective = new HashSet<>();
            context.owners().forEach((id, owner) -> { if (data.hasEffectiveStage(owner, id)) effective.add(id); });
            StageSlotResolver.Decision slot = StageSlotResolver.resolve(definition, effective,
                id -> StageOrder.getInstance().getStageDefinition(id), id -> grantTime(context.owners().get(id), id));
            boolean qualified = StageOrder.getInstance().getMissingDependencies(effective, stage).isEmpty()
                && slot.allowed() && slot.replacements().isEmpty()
                && (!definition.isPurchasable() || hasOwned(data, resolved, stage));
            if (qualified) {
                for (String source : sources) {
                    var parsed = PermissionStageSource.parse(source);
                    if (parsed.isEmpty() || !parsed.get().subject().equals(context.subject())) continue;
                    if (!preparePermissionGrant(resolved, stage, source)) {
                        if (data.revokeStageFromSource(resolved, stage, source)) changed.add(resolved);
                        continue;
                    }
                    Set<String> before = data.getSources(resolved, stage);
                    Set<String> activeBefore = data.getEffectiveSources(resolved, stage);
                    grantOwnedFromSource(data, resolved, stage, source);
                    if (!before.contains(source)) added.add(new TeamStageData.PermissionContribution(resolved, stage, parsed.get()));
                    if (!before.equals(data.getSources(resolved, stage))
                        || !activeBefore.equals(data.getEffectiveSources(resolved, stage))) changed.add(resolved);
                }
            } else {
                for (String source : data.getSources(resolved, stage)) {
                    PermissionStageSource.parse(source).filter(parsed -> parsed.subject().equals(context.subject())
                        && !parsed.permanent()).ifPresent(parsed -> {
                            if (data.revokeStageFromSource(resolved, stage, source)) changed.add(resolved);
                        });
                }
            }
        }
        boolean valid = current.getAsBoolean() && context.equals(captureOfflinePermissionContext(context.subject()));
        if (!valid) {
            data.restorePermissionEpisodes(context.subject(), previousEpisodes);
            for (var contribution : added) {
                data.revokeStageFromSource(contribution.owner(), contribution.stage(), contribution.source().label());
            }
            changed.addAll(data.deactivatePermissionSources(context.subject()));
        }
        if (valid) {
            for (var contribution : data.getPermissionContributions(context.subject())) {
                if (data.getEffectiveSources(contribution.owner(), contribution.stage()).contains(contribution.source().label())) {
                    restorePermissionClock(contribution.owner(), contribution.stage(), contribution.source().label());
                }
            }
        }
        notifyOfflinePermissionChange(changed, valid ? "source_reconciled" : "source_pending");
        return valid;
    }

    private void notifyOfflinePermissionChange(Set<OwnerRef> owners, String reason) {
        if (owners.isEmpty() || server == null) return;
        markMutation(true);
        for (UUID recipient : affectedPlayers(null, true, owners)) {
            ServerPlayer online = server.getPlayerList().getPlayer(recipient);
            if (online != null) {
                syncToPlayer(online);
                fireBulkChangedEvent(online, StagesBulkChangedEvent.Reason.OTHER);
            }
        }
        publishMutation(null, null, true, reason, Set.copyOf(owners));
    }

    public Set<String> getStageSources(ServerPlayer player, StageId stageId) {
        if (player == null || stageId == null) return Set.of();
        return getTeamStageData().getSources(owner(player, stageId), stageId);
    }

    /** remove one derived source while retaining independent or other derived access. */
    public boolean revokeStageFromSource(ServerPlayer player, StageId stageId, String source,
                                         StageCause cause) {
        if (player == null || stageId == null || source == null || source.isBlank()) return false;
        Set<StageId> before = getStages(player);
        OwnerRef stageOwner = owner(player, stageId);
        TeamStageData data = getTeamStageData();
        Set<String> sources = data.getSources(stageOwner, stageId);
        if (!sources.contains(source)) return false;
        if (!hasOwned(data, stageOwner, stageId)) return false;
        if (sources.size() == 1) {
            boolean removed = data.revokeStageFromSource(stageOwner, stageId, source);
            if (!removed) return false;
            markMutation(true);
            if (PermissionStageSource.parse(source).isEmpty()) {
                fireStageChangeEvent(player, stageOwner.id(), stageId, StageChangeType.REVOKED, cause);
            } else {
                for (UUID recipient : affectedPlayers(player, true, Set.of(stageOwner))) {
                    ServerPlayer online = server.getPlayerList().getPlayer(recipient);
                    if (online != null) fireBulkChangedEvent(online, StagesBulkChangedEvent.Reason.OTHER);
                }
            }
            syncStageView(player, stageId);
            publishMutation(player, stageId, true, "source_removed", Set.of(stageOwner));
            captureProgression(player, stageId, before, getStages(player), cause, "source_removed");
            return true;
        }
        boolean removed = data.revokeStageFromSource(stageOwner, stageId, source);
        if (removed) {
            markMutation(true);
            syncStageView(player, stageId);
            publishMutation(player, stageId, true, "source_removed", Set.of(stageOwner));
        }
        return removed;
    }

    /**
     * Grant a stage to a player with a specific cause.
     * Also grants to all team members if team mode is enabled.
     * Fires StageChangeEvent for each newly granted stage.
     *
     * <p>v1.3: If linear_progression is enabled, auto-grants missing dependencies.
     * Otherwise, stage is granted directly (use for triggers/rewards that should fail silently on missing deps).
     *
     * @param player The player to grant the stage to
     * @param stageId The stage to grant
     * @param cause The reason for the grant
     */
    public void grantStageWithCause(ServerPlayer player, StageId stageId, StageCause cause) {
        Set<StageId> before = getStages(player);
        TeamStageData data = getTeamStageData();
        OwnerRef directOwner = owner(player, stageId);
        if (hasIndependentOwnership(data, directOwner, stageId)) {
            boolean allowed = cause == StageCause.COMMAND && data.allowPermissionEpisodes(directOwner, stageId);
            markMutation(allowed);
            Set<String> beforeSources = data.getSources(directOwner, stageId);
            grantOwnedFromSource(data, directOwner, stageId, "independent");
            boolean sourceAdded = !beforeSources.equals(data.getSources(directOwner, stageId));
            markMutation(sourceAdded);
            if (allowed || sourceAdded) {
                publishMutation(player, stageId, true, "source_added", Set.of(directOwner));
                captureProgression(player, stageId, before, before, cause, "source_added");
            }
            return;
        }
        // For automatic grants (triggers, rewards), check dependencies unless linear_progression is on
        if (!StageConfig.isLinearProgression()) {
            List<StageId> missing = getMissingDependencies(player, stageId);
            if (!missing.isEmpty()) {
                LOGGER.debug("[ProgressiveStages] Cannot grant stage '{}' to {}: missing dependencies: {}",
                    stageId, player.getName().getString(), missing);
                // Notify the player so quest rewards / triggers don't silently fail
                String missingStr = missing.stream()
                    .map(id -> id.getPath())
                    .collect(java.util.stream.Collectors.joining(", "));
                String template = StageConfig.getMsgMissingDependencies();
                player.sendSystemMessage(TextUtil.parseColorCodes(
                    template.replace("{stage}", stageId.getPath())
                            .replace("{dependencies}", missingStr)));
                publishMutation(player, stageId, false, "dependency_denied", Set.of(owner(player, stageId)));
                captureProgression(player, stageId, before, before, cause, "dependency_denied");
                return;
            }
        }

        GrantResult result = grantStageToActorInternal(player, stageId, false);
        if (!result.denial().isBlank()) {
            player.sendSystemMessage(TextUtil.parseColorCodes("&c" + result.denial()));
            publishMutation(player, stageId, false, "slot_denied", Set.of(owner(player, stageId)));
            captureProgression(player, stageId, before, before, cause, "slot_denied");
            return;
        }
        markMutation(!result.granted().isEmpty() || !result.replaced().isEmpty());

        for (StageId replaced : result.replaced()) {
            UUID owner = owner(player, replaced).id();
            fireStageChangeEvent(player, owner, replaced,
                StageChangeType.REVOKED, StageCause.GROUP_POLICY);
            refundPurchasedStage(player, new RevokedStage(owner, replaced));
        }

        // Fire events for each newly granted stage
        for (StageId granted : result.granted()) {
            if (cause == StageCause.COMMAND) markMutation(getTeamStageData().allowPermissionEpisodes(owner(player, granted), granted));
            fireStageChangeEvent(player, owner(player, granted).id(), granted, StageChangeType.GRANTED, cause);
            applyRewardsOnce(player, granted);
            OwnerRef stageOwner = owner(player, granted);
            if (stageOwner.kind() == OwnerKind.PERSONAL) sendUnlockMessagesToPlayer(player, granted);
            else if (stageOwner.kind() == OwnerKind.TEAM) {
                sendUnlockMessages(stageOwner.id(), List.of(granted));
            }
        }

        if (java.util.stream.Stream.concat(result.granted().stream(), result.replaced().stream())
                .anyMatch(StageManager::isServerScoped)) syncAllPlayers();
        else {
            Set<UUID> teamOwners = java.util.stream.Stream.concat(result.granted().stream(), result.replaced().stream())
                .map(id -> owner(player, id)).filter(ref -> ref.kind() == OwnerKind.TEAM)
                .map(OwnerRef::id).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            if (teamOwners.isEmpty()) syncToPlayer(player);
            else teamOwners.forEach(id -> syncToTeamMembers(id, player));
        }
        Set<OwnerRef> affectedOwners = new LinkedHashSet<>();
        affectedOwners.add(owner(player, stageId));
        result.granted().forEach(id -> affectedOwners.add(owner(player, id)));
        result.replaced().forEach(id -> affectedOwners.add(owner(player, id)));
        publishMutation(player, stageId, !result.granted().isEmpty() || !result.replaced().isEmpty(),
            result.granted().isEmpty() ? "already_owned" : "committed", affectedOwners);
        captureProgression(player, stageId, before, getStages(player), cause,
            result.granted().isEmpty() ? "already_owned" : "committed");
    }

    /** v3.0: apply a newly-granted stage's [rewards] ONCE, to the player who earned/bought it. */
    private void applyRewardsOnce(ServerPlayer player, StageId stageId) {
        StageOrder.getInstance().getStageDefinition(stageId).ifPresent(d ->
            com.enviouse.progressivestages.server.enforcement.StageRewardApplier.apply(player, d));
    }

    /**
     * v3.0: record that {@code player}'s team actually PAID for a stage (called from the purchase
     * handler), so {@code refund_percent} only refunds stages that were bought — not ones earned via
     * trigger/command/quest, which would otherwise mint free items on every revoke/expiry.
     */
    public void markPurchased(ServerPlayer player, StageId stageId) {
        if (player.server == null) return;
        UUID storeTeam = owner(player, stageId).id();
        com.enviouse.progressivestages.server.triggers.StagePurchaseData.get(player.server)
            .markPaid(storeTeam, stageId);
    }

    /**
     * Grant a stage to a team (optionally with dependencies based on config)
     * Uses COMMAND as the default cause (legacy method, prefer grantStageWithCause)
     */
    public void grantStageToTeam(UUID teamId, StageId stageId) {
        grantStageToTeamInternal(teamId, stageId, false);
    }

    /**
     * Internal method that grants stages and returns newly granted stages.
     *
     * @param teamId The subject team. Each granted stage is stored according to its own scope.
     * @param stageId The stage to grant
     * @param bypassDependencies If true, skip dependency checks (admin bypass)
     */
    private GrantResult grantStageToTeamInternal(UUID teamId, StageId stageId, boolean bypassDependencies) {
        if (!StageOrder.getInstance().stageExists(stageId)) {
            LOGGER.warn("Attempted to grant non-existent stage: {}", stageId);
            return new GrantResult(List.of(), List.of(), "Stage does not exist. " + stageId);
        }

        TeamStageData data = getTeamStageData();
        Set<StageId> toGrant = new LinkedHashSet<>();

        // Auto-grant only the dependency branches needed by this stage's policy. For example, an
        // `any` stage follows one declared branch instead of accidentally granting every branch.
        if (!bypassDependencies && StageConfig.isLinearProgression()) {
            Set<StageId> effectiveOwned = new LinkedHashSet<>(getStages(teamId));
            collectRequiredGrantPlan(stageId, effectiveOwned, toGrant, new HashSet<>());
        } else {
            toGrant.add(stageId);
        }

        Set<StageId> initial = new LinkedHashSet<>(getStages(teamId));
        Set<StageId> simulated = new LinkedHashSet<>(initial);
        LinkedHashSet<StageId> replaced = new LinkedHashSet<>();
        for (StageId id : toGrant) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(id).orElse(null);
            StageSlotResolver.Decision decision = slotDecision(teamId, definition, simulated);
            if (!decision.allowed()) {
                return new GrantResult(List.of(), List.of(), decision.explanation());
            }
            decision.replacements().forEach(replaced::add);
            simulated.removeAll(decision.replacements());
            simulated.add(id);
        }

        List<StageId> removedStages = replaced.stream().filter(initial::contains)
            .filter(id -> !simulated.contains(id)).toList();
        for (StageId id : removedStages) {
            data.revokeStage(storageTeam(teamId, id), id);
            LOGGER.debug("Replaced stage {} for subject team {}", id, teamId);
        }
        List<StageId> newlyGranted = toGrant.stream().filter(simulated::contains)
            .filter(id -> !initial.contains(id)).toList();
        for (StageId id : newlyGranted) {
            UUID owner = storageTeam(teamId, id);
            data.grantStage(owner, id);
            LOGGER.debug("Granted stage {} to storage owner {} subject team {}", id, owner, teamId);
        }

        if (!newlyGranted.isEmpty()) {
            List<StageId> serverStages = newlyGranted.stream().filter(StageManager::isServerScoped).toList();
            List<StageId> teamStages = newlyGranted.stream().filter(s -> !isServerScoped(s)).toList();
            if (!serverStages.isEmpty()) sendUnlockMessages(SERVER_TEAM, serverStages);
            if (!teamStages.isEmpty()) sendUnlockMessages(teamId, teamStages);
        }

        markMutation(!newlyGranted.isEmpty() || !removedStages.isEmpty());

        return new GrantResult(newlyGranted, removedStages, "");
    }

    /** Grant using the requesting actor's per-stage owner, preserving team APIs above. */
    private GrantResult grantStageToActorInternal(ServerPlayer player, StageId stageId,
                                                  boolean bypassDependencies) {
        if (!StageOrder.getInstance().stageExists(stageId)) {
            LOGGER.warn("Attempted to grant non-existent actor stage.");
            return new GrantResult(List.of(), List.of(), "Stage does not exist. " + stageId);
        }
        TeamStageData data = getTeamStageData();
        Set<StageId> effectiveOwned = new LinkedHashSet<>(getStages(player));
        Set<StageId> toGrant = new LinkedHashSet<>();
        if (!bypassDependencies && StageConfig.isLinearProgression()) {
            Set<StageId> grantPlanOwned = new LinkedHashSet<>(effectiveOwned);
            if (!hasIndependentOwnership(data, owner(player, stageId), stageId)) grantPlanOwned.remove(stageId);
            collectRequiredGrantPlan(stageId, grantPlanOwned, toGrant, new HashSet<>());
        } else {
            toGrant.add(stageId);
        }
        Set<StageId> initial = new LinkedHashSet<>(effectiveOwned);
        Set<StageId> simulated = new LinkedHashSet<>(initial);
        LinkedHashSet<StageId> replaced = new LinkedHashSet<>();
        for (StageId id : toGrant) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(id).orElse(null);
            StageSlotResolver.Decision decision = slotDecision(player, definition, simulated);
            if (!decision.allowed()) return new GrantResult(List.of(), List.of(), decision.explanation());
            replaced.addAll(decision.replacements());
            simulated.removeAll(decision.replacements());
            simulated.add(id);
        }
        List<StageId> removed = replaced.stream().filter(initial::contains)
            .filter(id -> !simulated.contains(id)).toList();
        for (StageId id : removed) revokeOwned(data, owner(player, id), id);
        List<StageId> newlyGranted = toGrant.stream().filter(simulated::contains)
            .filter(id -> !hasIndependentOwnership(data, owner(player, id), id)).toList();
        for (StageId id : newlyGranted) grantOwned(data, owner(player, id), id);
        return new GrantResult(newlyGranted, removed, "");
    }

    private void sendUnlockMessagesToPlayer(ServerPlayer player, StageId stageId) {
        StageOrder.getInstance().getStageDefinition(stageId).ifPresent(def -> {
            def.getUnlockMessage().ifPresent(message -> player.sendSystemMessage(TextUtil.parseColorCodes(message)));
            com.enviouse.progressivestages.server.enforcement.UnlockEffectsApplier.apply(player, def);
            if (StageConfig.isPlayLockSound()) {
                player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.5f);
            }
        });
    }

    public StageSlotResolver.Decision getSlotDecision(ServerPlayer player, StageDefinition definition) {
        return slotDecision(player, definition, getStages(player));
    }

    private StageSlotResolver.Decision slotDecision(ServerPlayer player, StageDefinition definition,
                                                     Set<StageId> owned) {
        return StageSlotResolver.resolve(definition, owned,
            id -> StageOrder.getInstance().getStageDefinition(id),
            id -> grantTime(owner(player, id), id));
    }

    private StageSlotResolver.Decision slotDecision(UUID teamId, StageDefinition definition,
                                                     Set<StageId> owned) {
        return StageSlotResolver.resolve(definition, owned,
            id -> StageOrder.getInstance().getStageDefinition(id),
            id -> grantTime(teamId, id));
    }

    private long grantTime(UUID teamId, StageId stageId) {
        UUID storage = storageTeam(teamId, stageId);
        return grantTime(new OwnerRef(SERVER_TEAM.equals(storage) ? OwnerKind.SERVER : OwnerKind.TEAM, storage), stageId);
    }

    private long grantTime(OwnerRef owner, StageId stageId) {
        if (server == null) return -1L;
        return com.enviouse.progressivestages.server.triggers.StageRegressionData.get(server).getGrantTime(owner, stageId);
    }

    private void collectRequiredGrantPlan(StageId stageId, Set<StageId> effectiveOwned,
                                          Set<StageId> plan, Set<StageId> visiting) {
        if (effectiveOwned.contains(stageId) || plan.contains(stageId) || !visiting.add(stageId)) return;
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        if (definition == null) {
            visiting.remove(stageId);
            return;
        }

        int ownedDirect = 0;
        for (StageId dependency : definition.getDependencies()) {
            if (effectiveOwned.contains(dependency) || plan.contains(dependency)) ownedDirect++;
        }
        int needed = Math.max(0, definition.getDependencyCount() - ownedDirect);
        for (StageId dependency : definition.getDependencies()) {
            if (needed <= 0) break;
            if (effectiveOwned.contains(dependency) || plan.contains(dependency)) continue;
            collectRequiredGrantPlan(dependency, effectiveOwned, plan, visiting);
            if (plan.contains(dependency)) needed--;
        }

        Set<StageId> prospective = new HashSet<>(effectiveOwned);
        prospective.addAll(plan);
        if (definition.dependenciesSatisfied(prospective)) plan.add(stageId);
        else LOGGER.warn("Cannot build an automatic grant plan for {}: its dependency policy is unsatisfiable", stageId);
        visiting.remove(stageId);
    }

    /**
     * Check if granting a stage would require missing dependencies.
     * Used for admin bypass confirmation flow.
     *
     * @param player The player to check
     * @param stageId The target stage
     * @return List of missing dependency stage IDs (empty if no missing deps)
     */
    public List<StageId> getMissingDependencies(ServerPlayer player, StageId stageId) {
        // Use the effective set (includes server-wide stages) so they satisfy dependencies.
        Set<StageId> currentStages = getStages(player);
        return StageOrder.getInstance().getMissingDependencies(currentStages, stageId);
    }

    /**
     * Grant a stage bypassing dependency checks (admin override).
     */
    public void grantStageBypassDependencies(ServerPlayer player, StageId stageId, StageCause cause) {
        if (hasIndependentStage(player, stageId)) {
            grantStageWithCause(player, stageId, cause);
            return;
        }
        Set<StageId> before = getStages(player);
        GrantResult result = grantStageToActorInternal(player, stageId, true);
        if (!result.denial().isBlank()) {
            player.sendSystemMessage(TextUtil.parseColorCodes("&c" + result.denial()));
            publishMutation(player, stageId, false, "slot_denied", Set.of(owner(player, stageId)));
            captureProgression(player, stageId, before, before, cause, "slot_denied");
            return;
        }
        markMutation(!result.granted().isEmpty() || !result.replaced().isEmpty());

        for (StageId replaced : result.replaced()) {
            UUID owner = owner(player, replaced).id();
            fireStageChangeEvent(player, owner, replaced,
                StageChangeType.REVOKED, StageCause.GROUP_POLICY);
            refundPurchasedStage(player, new RevokedStage(owner, replaced));
        }

        // Fire events for each newly granted stage
        for (StageId granted : result.granted()) {
            if (cause == StageCause.COMMAND) markMutation(getTeamStageData().allowPermissionEpisodes(owner(player, granted), granted));
            fireStageChangeEvent(player, owner(player, granted).id(), granted, StageChangeType.GRANTED, cause);
            applyRewardsOnce(player, granted);
            OwnerRef stageOwner = owner(player, granted);
            if (stageOwner.kind() == OwnerKind.PERSONAL) sendUnlockMessagesToPlayer(player, granted);
            else if (stageOwner.kind() == OwnerKind.TEAM) {
                sendUnlockMessages(stageOwner.id(), List.of(granted));
            }
        }

        if (java.util.stream.Stream.concat(result.granted().stream(), result.replaced().stream())
                .anyMatch(StageManager::isServerScoped)) syncAllPlayers();
        else {
            Set<UUID> teamOwners = java.util.stream.Stream.concat(result.granted().stream(), result.replaced().stream())
                .map(id -> owner(player, id)).filter(ref -> ref.kind() == OwnerKind.TEAM)
                .map(OwnerRef::id).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            if (teamOwners.isEmpty()) syncToPlayer(player);
            else teamOwners.forEach(id -> syncToTeamMembers(id, player));
        }
        Set<OwnerRef> affectedOwners = new LinkedHashSet<>();
        affectedOwners.add(owner(player, stageId));
        result.granted().forEach(id -> affectedOwners.add(owner(player, id)));
        result.replaced().forEach(id -> affectedOwners.add(owner(player, id)));
        publishMutation(player, stageId, !result.granted().isEmpty() || !result.replaced().isEmpty(),
            result.granted().isEmpty() ? "already_owned" : "committed", affectedOwners);
        captureProgression(player, stageId, before, getStages(player), cause,
            result.granted().isEmpty() ? "already_owned" : "committed");
    }

    public UUID getStorageOwner(ServerPlayer player, StageId stageId) {
        return owner(player, stageId).id();
    }

    public boolean grantTemporaryStage(ServerPlayer player, StageId stageId, StageCause cause) {
        Set<StageId> before = getStages(player);
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        if (definition == null) return false;
        UUID teamId = owner(player, stageId).id();
        StageSlotResolver.Decision decision = slotDecision(player, definition, getStages(player));
        if (!decision.allowed()) return false;
        for (StageId replaced : decision.replacements()) {
            OwnerRef replacedOwner = owner(player, replaced);
            if (revokeOwned(getTeamStageData(), replacedOwner, replaced)) {
                fireStageChangeEvent(player, replacedOwner.id(), replaced,
                    StageChangeType.REVOKED, StageCause.GROUP_POLICY);
                refundPurchasedStage(player, new RevokedStage(replacedOwner.id(), replaced));
            }
        }
        OwnerRef stageOwner = owner(player, stageId);
        if (stageOwner.kind() == OwnerKind.PERSONAL) {
            if (!getTeamStageData().grantPersonalStageFromSource(stageOwner.id(), stageId, "temporary")) return false;
        } else if (!getTeamStageData().grantStageFromSource(stageOwner.id(), stageId, "temporary")) return false;
        markMutation(true);
        fireStageChangeEvent(player, stageOwner.id(), stageId, StageChangeType.GRANTED, cause);
        if (isServerScoped(stageId) || decision.replacements().stream().anyMatch(StageManager::isServerScoped)) {
            syncAllPlayers();
        }
        else if (stageOwner.kind() == OwnerKind.TEAM) syncToTeamMembers(teamId, player);
        else syncToPlayer(player);
        captureProgression(player, stageId, before, getStages(player), cause, "committed");
        publishMutation(player, stageId, true, "committed", Set.of(stageOwner));
        return true;
    }

    public boolean revokeTemporaryStage(ServerPlayer player, StageId stageId, StageCause cause) {
        return revokeTemporaryStage(player, getStorageOwner(player, stageId), stageId, cause);
    }

    public boolean revokeTemporaryStage(ServerPlayer player, UUID owner,
                                        StageId stageId, StageCause cause) {
        Set<StageId> before = getStages(player);
        if (!StageOrder.getInstance().stageExists(stageId) || owner == null) return false;
        OwnerRef expected = owner(player, stageId);
        if (!expected.id().equals(owner)) return false;
        boolean removed = getTeamStageData().revokeStageFromSource(expected, stageId, "temporary");
        if (!removed) return false;
        boolean changed = !before.equals(getStages(player));
        markMutation(removed || changed);
        if (changed) {
            fireStageChangeEvent(player, expected.id(), stageId, StageChangeType.REVOKED, cause);
            if (isServerScoped(stageId)) syncAllPlayers();
            else if (expected.kind() == OwnerKind.TEAM) syncToTeamMembers(owner, player);
            else syncToPlayer(player);
        }
        captureProgression(player, stageId, before, getStages(player), cause,
            changed ? "committed" : "already_owned");
        publishMutation(player, stageId, changed, changed ? "committed" : "already_owned", Set.of(expected));
        return true;
    }

    /**
     * Revoke a stage from a player (optionally with dependents based on config)
     * Also revokes from all team members if team mode is enabled.
     * Uses COMMAND as the default cause.
     */
    public void revokeStage(ServerPlayer player, StageId stageId) {
        revokeStageWithCause(player, stageId, StageCause.COMMAND);
    }

    /**
     * Revoke a stage from a player with a specific cause.
     * Also revokes from all team members if team mode is enabled.
     * Fires StageChangeEvent for each revoked stage.
     *
     * @param player The player to revoke the stage from
     * @param stageId The stage to revoke
     * @param cause The reason for the revocation
     */
    public void revokeStageWithCause(ServerPlayer player, StageId stageId, StageCause cause) {
        if (player == null || stageId == null || !StageOrder.getInstance().stageExists(stageId)) return;
        Set<StageId> before = getStages(player);
        boolean serverScoped = isServerScoped(stageId);
        OwnerRef rootOwner = owner(player, stageId);
        boolean suppressed = getTeamStageData().suppressPermissionEpisodes(rootOwner, stageId);
        markMutation(suppressed);
        UUID teamId = rootOwner.kind() == OwnerKind.TEAM ? rootOwner.id()
            : TeamProvider.getInstance().getTeamId(player);
        List<RevokedStage> revoked = rootOwner.kind() == OwnerKind.PERSONAL
            ? revokeStageFromActorInternal(player, stageId)
            : revokeStageFromTeamInternal(teamId, stageId);
        markMutation(!revoked.isEmpty());

        // Fire one event for each concrete storage owner. A server-stage cascade can revoke the
        // same team-scoped dependent from many teams; collapsing those into one event leaves
        // regression clocks and integration state stale for every other team.
        for (RevokedStage change : revoked) {
            StageId revokedStage = change.stageId();
            ServerPlayer affectedPlayer = onlineRepresentative(change.owner(), player).orElse(player);
            fireStageChangeEvent(affectedPlayer, change.owner(), revokedStage, StageChangeType.REVOKED, cause);
            refundPurchasedStage(player, change);
        }

        boolean affectedMultipleTeams = serverScoped || rootOwner.kind() == OwnerKind.TEAM && revoked.stream()
            .anyMatch(change -> SERVER_TEAM.equals(change.owner()) || !teamId.equals(change.owner()));
        if (affectedMultipleTeams) syncAllPlayers();
        else if (revoked.stream().anyMatch(change -> owner(player, change.stageId()).kind() == OwnerKind.TEAM)) syncToTeamMembers(teamId, player);
        else syncToPlayer(player);
        Set<OwnerRef> affectedOwners = revoked.stream().map(change -> owner(player, change.stageId()))
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (suppressed) affectedOwners.add(rootOwner);
        boolean changed = suppressed || !revoked.isEmpty();
        publishMutation(player, stageId, changed, changed ? "committed" : "already_owned", Set.copyOf(affectedOwners));
        captureProgression(player, stageId, before, getStages(player), cause,
            changed ? "committed" : "already_owned");
    }

    private void refundPurchasedStage(ServerPlayer player, RevokedStage change) {
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(change.stageId()).orElse(null);
        if (definition == null || !definition.isPurchasable() || definition.getCost().refundPercent() <= 0
                || player.server == null) return;
        var purchaseData = com.enviouse.progressivestages.server.triggers.StagePurchaseData.get(player.server);
        if (!purchaseData.isPaid(change.owner(), change.stageId())) return;
        Optional<ServerPlayer> recipient = onlineRepresentative(change.owner(), player);
        if (recipient.isPresent() && purchaseData.consumePaid(change.owner(), change.stageId())) {
            refundCost(recipient.get(), definition.getCost());
        } else {
            purchaseData.deferRefund(change.owner(), change.stageId());
        }
    }

    /** v3.0: return refund_percent of a purchased stage's item/xp cost to the player. */
    private void refundCost(ServerPlayer player, com.enviouse.progressivestages.common.config.StageCost cost) {
        int pct = cost.refundPercent();
        int xp = cost.xpLevels() * pct / 100;
        if (xp > 0) player.giveExperienceLevels(xp);
        for (var ic : cost.items()) {
            int give = ic.count() * pct / 100;
            if (give <= 0) continue;
            net.minecraft.world.item.Item item =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(ic.item()).orElse(null);
            if (item == null) continue;
            int max = Math.max(1, new net.minecraft.world.item.ItemStack(item).getMaxStackSize());
            int remaining = give;
            while (remaining > 0) {
                int n = Math.min(remaining, max);
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, n);
                if (!player.getInventory().add(stack) || !stack.isEmpty()) player.drop(stack, false);
                remaining -= n;
            }
        }
    }

    /**
     * Revoke a stage from a team (optionally with successors based on config)
     * Uses COMMAND as the default cause (legacy method, prefer revokeStageWithCause)
     */
    public void revokeStageFromTeam(UUID teamId, StageId stageId) {
        revokeStageFromTeamInternal(teamId, stageId);
    }

    private List<RevokedStage> revokeStageFromActorInternal(ServerPlayer player, StageId stageId) {
        if (!StageOrder.getInstance().stageExists(stageId)) return Collections.emptyList();
        TeamStageData data = getTeamStageData();
        OwnerRef rootOwner = owner(player, stageId);
        if (!revokeOwned(data, rootOwner, stageId)) return Collections.emptyList();
        List<RevokedStage> revoked = new ArrayList<>();
        revoked.add(new RevokedStage(rootOwner.id(), stageId));
        StageDefinition rootDefinition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        boolean cascade = StageConfig.isLinearProgression()
            || (rootDefinition != null && rootDefinition.getRevoke().cascade());
        if (!cascade) return revoked;
        Set<StageId> remaining = new LinkedHashSet<>(getStages(player));
        remaining.remove(stageId);
        for (StageId dependent : StageOrder.getInstance().getAllDependents(stageId)) {
            StageDefinition definition = StageOrder.getInstance().getStageDefinition(dependent).orElse(null);
            if (definition == null || definition.dependenciesSatisfied(remaining)) continue;
            OwnerRef dependentOwner = owner(player, dependent);
            if (!dependentOwner.equals(rootOwner)) continue;
            if (revokeOwned(data, dependentOwner, dependent)) {
                revoked.add(new RevokedStage(dependentOwner.id(), dependent));
                remaining.remove(dependent);
            }
        }
        return revoked;
    }

    /**
     * Internal method that revokes stages and returns revoked stage ids. Each stage uses its own
     * storage scope. Cascading from a server-wide prerequisite removes team-scoped dependents from
     * every team because the prerequisite disappeared globally.
     */
    private List<RevokedStage> revokeStageFromTeamInternal(UUID teamId, StageId stageId) {
        if (!StageOrder.getInstance().stageExists(stageId)) {
            LOGGER.warn("Attempted to revoke non-existent stage: {}", stageId);
            return Collections.emptyList();
        }

        TeamStageData data = getTeamStageData();
        // Cascade to dependents when linear progression is on globally, OR the stage opts in via
        // its per-stage revoke_cascade flag. Policy-aware cascading preserves dependents whose
        // `any`/`at_least` prerequisites remain satisfied through another branch.
        boolean cascade = StageConfig.isLinearProgression()
            || StageOrder.getInstance().getStageDefinition(stageId)
                .map(d -> d.getRevoke().cascade()).orElse(false);
        List<RevokedStage> revoked = new ArrayList<>();
        Deque<RevokedStage> pending = new ArrayDeque<>();
        Set<RevokedStage> queued = new HashSet<>();
        RevokedStage root = new RevokedStage(storageTeam(teamId, stageId), stageId);
        pending.add(root);
        queued.add(root);

        while (!pending.isEmpty()) {
            RevokedStage change = pending.removeFirst();
            if (!data.revokeStage(change.owner(), change.stageId())) continue;
            revoked.add(change);
            if (!cascade) continue;

            for (StageId dependent : StageOrder.getInstance().getDependents(change.stageId())) {
                StageDefinition definition = StageOrder.getInstance().getStageDefinition(dependent).orElse(null);
                if (definition == null) continue;
                if (definition.isServerScope()) {
                    // Server stages have one concrete owner. Use the initiating team as the subject
                    // context for the unusual (but supported) case of a global stage depending on a
                    // team-scoped stage.
                    if (data.hasStage(SERVER_TEAM, dependent)
                            && !definition.dependenciesSatisfied(effectiveStages(data, teamId))) {
                        RevokedStage next = new RevokedStage(SERVER_TEAM, dependent);
                        if (queued.add(next)) pending.addLast(next);
                    }
                    continue;
                }

                Collection<UUID> owners = SERVER_TEAM.equals(change.owner())
                    ? new ArrayList<>(data.getAllTeamIds()) : List.of(change.owner());
                for (UUID owner : owners) {
                    if (SERVER_TEAM.equals(owner) || !data.hasStage(owner, dependent)) continue;
                    if (!definition.dependenciesSatisfied(effectiveStages(data, owner))) {
                        RevokedStage next = new RevokedStage(owner, dependent);
                        if (queued.add(next)) pending.addLast(next);
                    }
                }
            }
        }

        markMutation(!revoked.isEmpty());
        return revoked;
    }

    private static Set<StageId> effectiveStages(TeamStageData data, UUID teamId) {
        OwnerRef owner = new OwnerRef(SERVER_TEAM.equals(teamId) ? OwnerKind.SERVER : OwnerKind.TEAM, teamId);
        Set<StageId> result = new LinkedHashSet<>(data.getEffectiveStages(owner));
        if (!SERVER_TEAM.equals(teamId)) result.addAll(data.getEffectiveStages(new OwnerRef(OwnerKind.SERVER, SERVER_TEAM)));
        return result;
    }

    /** Prefer the initiating player when they belong to the owner; otherwise find an online member. */
    private Optional<ServerPlayer> onlineRepresentative(UUID owner, ServerPlayer initiator) {
        if (SERVER_TEAM.equals(owner)) return Optional.of(initiator);
        if (TeamProvider.getInstance().getTeamMembersForOwner(owner, initiator).contains(initiator)) {
            return Optional.of(initiator);
        }
        if (server == null) return Optional.empty();
        return TeamProvider.getInstance().getTeamMembersForOwner(owner, initiator).stream().findFirst();
    }

    /**
     * Get all stages for a player
     */
    public Set<StageId> getStages(ServerPlayer player) {
        return resolvedStages(player, false);
    }

    public Set<StageId> getStoredStages(ServerPlayer player) {
        return resolvedStages(player, true);
    }

    private Set<StageId> resolvedStages(ServerPlayer player, boolean includeInactive) {
        if (player == null) return Set.of();
        TeamStageData data = getTeamStageData();
        Set<StageId> result = new LinkedHashSet<>();
        for (StageId stage : data.getPersonalStages(player.getUUID())) {
            OwnerRef resolved = owner(player, stage);
            if (resolved.kind() == OwnerKind.PERSONAL
                    && (includeInactive || data.hasEffectiveStage(resolved, stage))) result.add(stage);
        }
        for (UUID teamId : data.getAllTeamIds()) {
            for (StageId stage : data.getStages(teamId)) {
                OwnerRef resolved = owner(player, stage);
                if (resolved.kind() == OwnerKind.TEAM && resolved.id().equals(teamId)
                        && (includeInactive || data.hasEffectiveStage(resolved, stage))) result.add(stage);
            }
        }
        for (StageId stage : data.getStages(SERVER_TEAM)) {
            OwnerRef resolved = owner(player, stage);
            if (resolved.kind() == OwnerKind.SERVER
                    && (includeInactive || data.hasEffectiveStage(resolved, stage))) result.add(stage);
        }
        return Collections.unmodifiableSet(result);
    }

    /** Return an immutable effective view and the current committed mutation revision. */
    public EffectiveStageSnapshot getEffectiveSnapshot(ServerPlayer player) {
        if (player == null) return new EffectiveStageSnapshot(new UUID(0L, 0L), mutationRevision, Set.of(), Map.of());
        Set<StageId> stages = getStages(player);
        Map<StageId, Set<StageSourceKind>> sources = new LinkedHashMap<>();
        TeamStageData data = getTeamStageData();
        for (StageId stage : stages) addSources(sources, stage, data.getEffectiveSources(owner(player, stage), stage));
        return new EffectiveStageSnapshot(player.getUUID(), mutationRevision, stages, sources);
    }

    private static void addSources(Map<StageId, Set<StageSourceKind>> target, StageId stage,
                                   Set<String> labels) {
        Set<StageSourceKind> resolved = target.computeIfAbsent(stage, ignored -> new LinkedHashSet<>());
        if (labels == null || labels.isEmpty()) {
            resolved.add(StageSourceKind.INDEPENDENT);
            return;
        }
        labels.forEach(label -> resolved.add(StageSourceKind.fromLabel(label)));
    }

    public long getMutationRevision() { return mutationRevision; }

    /** refresh effective stage and lock views after an external source mutation. */
    public void syncStageView(ServerPlayer player, StageId stageId) {
        if (player == null) return;
        OwnerRef resolved = owner(player, stageId);
        if (resolved.kind() == OwnerKind.SERVER) syncAllPlayers();
        else if (resolved.kind() == OwnerKind.TEAM) syncToTeamMembers(resolved.id(), player);
        else syncToPlayer(player);
    }

    /**
     * Get all stages for a team
     */
    public Set<StageId> getStages(UUID teamId) {
        TeamStageData data = getTeamStageData();
        Set<StageId> server = data.getEffectiveStages(new OwnerRef(OwnerKind.SERVER, SERVER_TEAM));
        Set<StageId> team = data.getEffectiveStages(new OwnerRef(
            SERVER_TEAM.equals(teamId) ? OwnerKind.SERVER : OwnerKind.TEAM, teamId));
        if (server.isEmpty() || teamId.equals(SERVER_TEAM)) return team; // fast path / server view
        // v2.4: union server-wide stages into every team's effective stage set.
        Set<StageId> union = new LinkedHashSet<>(team);
        union.addAll(server);
        return Collections.unmodifiableSet(union);
    }

    /**
     * Get the highest stage a player has reached
     */
    public Optional<StageId> getCurrentStage(ServerPlayer player) {
        StageId highest = null;
        int highestDepth = -1;
        for (StageId stageId : getStages(player)) {
            int depth = StageOrder.getInstance().getAllDependencies(stageId).size();
            if (depth > highestDepth) {
                highestDepth = depth;
                highest = stageId;
            }
        }
        return Optional.ofNullable(highest);
    }

    /**
     * Grant the starting stages to a new player.
     * v1.3: Supports multiple starting stages.
     */
    public void grantStartingStage(ServerPlayer player) {
        List<String> startingStageIds = StageConfig.getStartingStages();
        if (startingStageIds == null || startingStageIds.isEmpty()) {
            return;
        }

        Set<StageId> currentStages = getStoredStages(player);

        // Only grant starting stages if player has no stages yet, unless reapply is enabled.
        // (data.grantStage already short-circuits when the team already has the stage.)
        if (!currentStages.isEmpty() && !StageConfig.isReapplyStartingStagesOnLogin()) {
            return;
        }

        // Grant all starting stages (bypass dependency checks for starting stages)
        for (String stageIdStr : startingStageIds) {
            if (stageIdStr == null || stageIdStr.isBlank()) {
                continue;
            }
            StageId stageId = StageId.tryParse(stageIdStr);
            if (stageId == null) {
                LOGGER.warn("Skipping invalid starting stage entry '{}'", stageIdStr);
                continue;
            }
            if (StageOrder.getInstance().stageExists(stageId)) {
                grantStageBypassDependencies(player, stageId, StageCause.STARTING_STAGE);
                LOGGER.debug("Granted starting stage {} to player {}", stageId, player.getName().getString());
            } else {
                LOGGER.warn("Starting stage {} does not exist, skipping", stageIdStr);
            }
        }
    }

    /**
     * Sync stage data to all online team members
     */
    private void syncToTeamMembers(UUID teamId, ServerPlayer requester) {
        if (server == null) return;

        Set<ServerPlayer> members = TeamProvider.getInstance().getTeamMembersForOwner(teamId, requester);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (members.contains(player)) {
                NetworkHandler.sendStageSync(player, getStages(player));
                // v2.4: re-apply [attribute] modifiers for this member after the team's stages changed.
                com.enviouse.progressivestages.server.enforcement.StageAttributeApplier.reconcile(player);
                // v2.5: re-send advancements so newly (un)gated ones (dis)appear without a relog.
                com.enviouse.progressivestages.server.enforcement.AdvancementHider.resyncIfNeeded(player);
                com.enviouse.progressivestages.server.enforcement.EntityPresenceEnforcer.syncClientState(player, true);
            }
        }
    }

    private void syncToPlayer(ServerPlayer player) {
        if (player == null) return;
        NetworkHandler.sendStageSync(player, getStages(player));
        com.enviouse.progressivestages.server.enforcement.StageAttributeApplier.reconcile(player);
        com.enviouse.progressivestages.server.enforcement.AdvancementHider.resyncIfNeeded(player);
        com.enviouse.progressivestages.server.enforcement.EntityPresenceEnforcer.syncClientState(player, true);
    }

    /** v2.4: sync + attribute-reconcile EVERY online player (used when a server-wide stage changes). */
    private void syncAllPlayers() {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendStageSync(player, getStages(player));
            com.enviouse.progressivestages.server.enforcement.StageAttributeApplier.reconcile(player);
            com.enviouse.progressivestages.server.enforcement.AdvancementHider.resyncIfNeeded(player);
            com.enviouse.progressivestages.server.enforcement.EntityPresenceEnforcer.syncClientState(player, true);
        }
    }

    /**
     * Send unlock messages for newly granted stages
     */
    private void sendUnlockMessages(UUID teamId, List<StageId> newlyGranted) {
        if (server == null) return;

        // Server-scoped grants are stored under SERVER_TEAM — a sentinel no real player team matches —
        // so they must reach every online player rather than only members of the (synthetic) team.
        boolean serverScoped = SERVER_TEAM.equals(teamId);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean member = serverScoped || TeamProvider.getInstance()
                .getTeamMembersForOwner(teamId, player).contains(player);
            if (member) {
                for (StageId stageId : newlyGranted) {
                    Optional<StageDefinition> defOpt = StageOrder.getInstance().getStageDefinition(stageId);
                    if (defOpt.isPresent()) {
                        StageDefinition def = defOpt.get();
                        def.getUnlockMessage().ifPresent(msg -> {
                            Component message = TextUtil.parseColorCodes(msg);
                            player.sendSystemMessage(message);
                        });
                        // v2.4: optional [unlock] toast/title/subtitle/sound/particles. This is
                        // per-member PRESENTATION (everyone on the team sees it). [rewards] are NOT
                        // applied here — they'd duplicate per online member / per online player on a
                        // server-scoped grant. They're applied once to the granting player instead.
                        com.enviouse.progressivestages.server.enforcement.UnlockEffectsApplier.apply(player, def);
                    }
                }

                // Play unlock sound
                if (StageConfig.isPlayLockSound()) {
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.5f);
                }
            }
        }
    }

    /**
     * Get progress string for a player (e.g., "2/5")
     */
    @SuppressWarnings("removal")
    public String getProgressString(ServerPlayer player) {
        Set<StageId> stages = getStages(player);
        int total = StageOrder.getInstance().getStageCount();
        return stages.size() + "/" + total;
    }

    /**
     * Fire a stage change event on the NeoForge event bus.
     * This notifies all listeners (including FTB Quests compat) that a stage changed.
     */
    private void fireStageChangeEvent(ServerPlayer player, UUID teamId, StageId stageId,
                                       StageChangeType changeType, StageCause cause) {
        StageChangeEvent event = new StageChangeEvent(player, teamId, stageId, changeType, cause);
        NeoForge.EVENT_BUS.post(event);

        if (StageConfig.isDebugLogging()) {
            LOGGER.info("[ProgressiveStages] Stage {} {} for player {} (cause: {})",
                stageId, changeType, player.getName().getString(), cause);
        }
    }

    /**
     * Fire a bulk stages changed event.
     * Use this for login, team join, reload, etc. instead of firing N individual events.
     *
     * @param player The affected player
     * @param reason Why the bulk change occurred
     */
    public void fireBulkChangedEvent(ServerPlayer player, StagesBulkChangedEvent.Reason reason) {
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        Set<StageId> currentStages = Collections.unmodifiableSet(new HashSet<>(getStages(player)));

        StagesBulkChangedEvent event = new StagesBulkChangedEvent(player, teamId, currentStages, reason);
        NeoForge.EVENT_BUS.post(event);

        if (StageConfig.isDebugLogging()) {
            LOGGER.info("[ProgressiveStages] Bulk stage change for player {} (reason: {}, {} stages)",
                player.getName().getString(), reason, currentStages.size());
        }
    }

    /**
     * Sync stages to a player on login (fires bulk event instead of individual events).
     * Call this instead of multiple grantStage calls when a player logs in.
     */
    public void syncStagesOnLogin(ServerPlayer player) {
        // Grant starting stage if needed (this is a single operation, not bulk)
        grantStartingStage(player);

        // A global revoke cascade may have removed a purchased team stage while every member was
        // offline. Deliver its persisted refund to the first member who returns.
        if (player.server != null) {
            var purchaseData = com.enviouse.progressivestages.server.triggers.StagePurchaseData.get(player.server);
            UUID teamId = TeamProvider.getInstance().getTeamId(player);
            Set<StageId> pendingStages = new LinkedHashSet<>(purchaseData.getPendingRefunds(teamId));
            pendingStages.addAll(purchaseData.getPendingRefunds(TeamProvider.getInstance().getFtbTeamId(player)));
            pendingStages.addAll(purchaseData.getPendingRefunds(player.getUUID()));
            for (StageId pending : pendingStages) {
                StageDefinition def = StageOrder.getInstance().getStageDefinition(pending).orElse(null);
                UUID refundOwner = owner(player, pending).id();
                if (def != null && def.isPurchasable() && def.getCost().refundPercent() > 0
                        && purchaseData.consumePendingRefund(refundOwner, pending)) {
                    refundCost(player, def.getCost());
                }
            }
        }

        // Fire bulk event for login - FTB Quests will do one recheck
        fireBulkChangedEvent(player, StagesBulkChangedEvent.Reason.LOGIN);

        // v2.4: apply this player's team's [attribute] modifiers (transient — re-applied each login).
        com.enviouse.progressivestages.server.enforcement.StageAttributeApplier.reconcile(player);
    }

    /**
     * Get the server instance.
     */
    public MinecraftServer getServer() {
        return server;
    }
}
