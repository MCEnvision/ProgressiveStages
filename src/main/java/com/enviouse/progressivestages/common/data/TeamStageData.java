package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;
import com.enviouse.progressivestages.common.stage.PermissionEpisode;
import com.enviouse.progressivestages.common.stage.PendingStageReward;
import com.enviouse.progressivestages.common.stage.StageSourceKind;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Data class representing a team's stages.
 * This is stored as a data attachment on the server level.
 */
public class TeamStageData {

    public static final int CURRENT_SCHEMA = 1;
    private static final UUID SERVER_OWNER = new UUID(0L, 0L);

    public static final Codec<TeamStageData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.intRange(0, CURRENT_SCHEMA).optionalFieldOf("ownership_schema", 0)
                .forGetter(TeamStageData::getOwnershipSchema),
            Codec.unboundedMap(
                Codec.STRING,
                Codec.list(ResourceLocation.CODEC)
            ).fieldOf("team_stages").forGetter(TeamStageData::serializeTeamStages),
            Codec.unboundedMap(
                Codec.STRING,
                Codec.list(ResourceLocation.CODEC)
            ).optionalFieldOf("personal_stages", Map.of()).forGetter(TeamStageData::serializePersonalStages)
            , Codec.unboundedMap(Codec.STRING,
                Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING)))
                .optionalFieldOf("stage_sources", Map.of()).forGetter(TeamStageData::serializeSources),
            Codec.intRange(0, 1).optionalFieldOf("permission_episode_schema", 0).forGetter(value -> 1),
            PermissionEpisode.CODEC.listOf().optionalFieldOf("permission_episodes", List.of())
                .forGetter(value -> List.copyOf(value.permissionEpisodes.values())),
            Codec.intRange(0, 1).optionalFieldOf("ftb_helper_import_schema", 0)
                .forGetter(value -> value.ftbHelperImportSchema),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC.listOf())
                .optionalFieldOf("ftb_helper_imports", Map.of())
                .forGetter(value -> serialize(value.ftbHelperImports)),
            Codec.intRange(0, 1).optionalFieldOf("pending_reward_schema", 0).forGetter(value -> 1),
            PendingStageReward.CODEC.listOf().optionalFieldOf("pending_rewards", List.of())
                .forGetter(value -> List.copyOf(value.pendingRewards.values()))
        ).apply(instance, TeamStageData::new)
    );

    // Team UUID -> Set of stage IDs
    private final Map<UUID, Set<StageId>> teamStages = new HashMap<>();
    /** Player UUID to stages explicitly owned by that player. */
    private final Map<UUID, Set<StageId>> personalStages = new HashMap<>();
    private final Map<UUID, Set<StageId>> ftbHelperImports = new HashMap<>();
    private int ftbHelperImportSchema;
    /** Owner UUID to stage id to source labels. Empty source sets mean legacy ownership. */
    private record SourceOwner(OwnerKind kind, UUID id) {}
    private final Map<SourceOwner, Map<StageId, Set<String>>> stageSources = new HashMap<>();
    public record PermissionContribution(OwnerRef owner, StageId stage, PermissionStageSource source) {}
    private final java.util.NavigableMap<UUID, Set<PermissionContribution>> permissionContributions = new java.util.TreeMap<>();
    private record ActiveSource(SourceOwner owner, StageId stage, String label) {}
    private final Set<ActiveSource> activeSynchronizedSources = new HashSet<>();
    private record EpisodeKey(OwnerRef owner, StageId stage, UUID subject, String row) {
        private static EpisodeKey of(PermissionEpisode episode) {
            return new EpisodeKey(episode.owner(), episode.stage(), episode.subject(), episode.row());
        }
    }
    private record OwnerStage(OwnerRef owner, StageId stage) {}
    private final Map<EpisodeKey, PermissionEpisode> permissionEpisodes = new HashMap<>();
    private final Map<OwnerStage, Set<EpisodeKey>> ownerEpisodes = new HashMap<>();
    private final NavigableMap<UUID, Set<EpisodeKey>> subjectEpisodes = new TreeMap<>();
    private final Map<UUID, PendingStageReward> pendingRewards = new LinkedHashMap<>();
    private int ownershipSchema = CURRENT_SCHEMA;
    private net.minecraft.nbt.Tag unreadable;

    static TeamStageData preserveUnreadable(net.minecraft.nbt.Tag tag) {
        TeamStageData data = new TeamStageData();
        data.unreadable = tag.copy();
        return data;
    }

    net.minecraft.nbt.Tag unreadableTag() { return unreadable == null ? null : unreadable.copy(); }

    private void requireReadable() {
        if (unreadable != null) throw new IllegalStateException("Stage ownership data is unreadable. Restore a compatible backup before changing progression.");
    }

    public TeamStageData() {}

    private TeamStageData(int schema, Map<String, List<ResourceLocation>> serialized,
                          Map<String, List<ResourceLocation>> serializedPersonal,
                          Map<String, Map<String, List<String>>> serializedSources, int episodeSchema,
                          List<PermissionEpisode> episodes, int helperSchema,
                          Map<String, List<ResourceLocation>> helperImports, int rewardSchema, List<PendingStageReward> rewards) {
        if (episodeSchema == 0 && !episodes.isEmpty()) {
            throw new IllegalArgumentException("Permission episodes require a schema version");
        }
        if (rewardSchema == 0 && !rewards.isEmpty()) {
            throw new IllegalArgumentException("Pending rewards require a schema version");
        }
        for (PendingStageReward reward : rewards) queueReward(reward);
        ownershipSchema = schema;
        readStages(serialized, teamStages);
        readStages(serializedPersonal, personalStages);
        readSources(serializedSources);
        if (helperSchema == 0 && !helperImports.isEmpty()) {
            throw new IllegalArgumentException("FTB helper imports require a schema version");
        }
        readStages(helperImports, ftbHelperImports);
        if (ftbHelperImports.containsKey(SERVER_OWNER)) {
            throw new IllegalArgumentException("FTB helper imports require a team owner");
        }
        ftbHelperImportSchema = helperSchema;
        indexPermissionSources();
        for (PermissionEpisode episode : episodes) {
            if (permissionEpisodes.containsKey(EpisodeKey.of(episode))) {
                throw new IllegalArgumentException("Duplicate permission episode");
            }
            putPermissionEpisode(episode);
        }
    }

    public void queueReward(PendingStageReward reward) {
        requireReadable();
        Objects.requireNonNull(reward, "reward");
        if (pendingRewards.putIfAbsent(reward.receipt(), reward) != null) {
            throw new IllegalArgumentException("Duplicate pending reward receipt");
        }
    }

    public List<PendingStageReward> getPendingRewards(UUID actor) {
        requireReadable();
        return pendingRewards.values().stream().filter(reward -> reward.actor().equals(actor)).toList();
    }

    public boolean consumeReward(UUID receipt, UUID actor) {
        requireReadable();
        PendingStageReward reward = pendingRewards.get(receipt);
        return reward != null && reward.actor().equals(actor) && pendingRewards.remove(receipt, reward);
    }

    public int getOwnershipSchema() { requireReadable(); return ownershipSchema; }

    public boolean hasImportedFtbHelperStages() {
        requireReadable();
        return ftbHelperImportSchema == 1;
    }

    public boolean importFtbHelperStages(Map<UUID, Set<StageId>> imports) {
        requireReadable();
        if (hasImportedFtbHelperStages()) return false;
        Map<UUID, Set<StageId>> snapshot = new HashMap<>();
        imports.forEach((owner, stages) -> {
            if (owner == null || SERVER_OWNER.equals(owner)) {
                throw new IllegalArgumentException("FTB helper imports require a team owner");
            }
            snapshot.put(owner, Set.copyOf(stages));
        });
        snapshot.forEach((owner, stages) -> {
            stages.forEach(stage -> grantStage(owner, stage));
            if (!stages.isEmpty()) ftbHelperImports.put(owner, new HashSet<>(stages));
        });
        ftbHelperImportSchema = 1;
        return true;
    }

    private static void readStages(Map<String, List<ResourceLocation>> serialized,
                                   Map<UUID, Set<StageId>> destination) {
        if (serialized == null) return;
        for (Map.Entry<String, List<ResourceLocation>> entry : serialized.entrySet()) {
            try {
                UUID teamId = UUID.fromString(entry.getKey());
                Set<StageId> stages = new HashSet<>();
                for (ResourceLocation rl : entry.getValue() == null ? List.<ResourceLocation>of() : entry.getValue()) {
                    stages.add(StageId.fromResourceLocation(rl));
                }
                destination.put(teamId, stages);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid stage owner record. " + entry.getKey(), e);
            }
        }
    }

    private void readSources(Map<String, Map<String, List<String>>> serialized) {
        if (serialized == null) return;
        for (Map.Entry<String, Map<String, List<String>>> ownerEntry : serialized.entrySet()) {
            try {
                String encodedOwner = ownerEntry.getKey();
                OwnerKind kind = OwnerKind.TEAM;
                if (encodedOwner.startsWith("personal:")) {
                    kind = OwnerKind.PERSONAL;
                    encodedOwner = encodedOwner.substring("personal:".length());
                } else if (encodedOwner.startsWith("server:")) {
                    kind = OwnerKind.SERVER;
                    encodedOwner = encodedOwner.substring("server:".length());
                } else if (encodedOwner.startsWith("team:")) {
                    encodedOwner = encodedOwner.substring("team:".length());
                }
                UUID owner = UUID.fromString(encodedOwner);
                Map<StageId, Set<String>> stages = new HashMap<>();
                if (ownerEntry.getValue() != null) {
                    for (Map.Entry<String, List<String>> stageEntry : ownerEntry.getValue().entrySet()) {
                        StageId stage = StageId.tryParse(stageEntry.getKey());
                        if (stage == null) {
                            throw new IllegalArgumentException("Invalid stage source key. " + stageEntry.getKey());
                        }
                        stages.put(stage, new HashSet<>(stageEntry.getValue() == null
                            ? List.of() : stageEntry.getValue()));
                    }
                }
                if (!ownerEntry.getKey().contains(":")) kind = teamKind(owner);
                stageSources.put(new SourceOwner(kind, owner), stages);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid stage source owner record. " + ownerEntry.getKey(), e);
            }
        }
    }

    private Map<String, List<ResourceLocation>> serializeTeamStages() {
        return serialize(teamStages);
    }

    private Map<String, List<ResourceLocation>> serializePersonalStages() {
        return serialize(personalStages);
    }

    private Map<String, Map<String, List<String>>> serializeSources() {
        Map<String, Map<String, List<String>>> result = new HashMap<>();
        for (Map.Entry<SourceOwner, Map<StageId, Set<String>>> owner : stageSources.entrySet()) {
            Map<String, List<String>> stages = new HashMap<>();
            for (Map.Entry<StageId, Set<String>> stage : owner.getValue().entrySet()) {
                stages.put(stage.getKey().toString(), List.copyOf(stage.getValue()));
            }
            String prefix = owner.getKey().kind() == OwnerKind.PERSONAL ? "personal:"
                : owner.getKey().kind() == OwnerKind.SERVER ? "server:" : "team:";
            result.put(prefix + owner.getKey().id(), stages);
        }
        return result;
    }

    private static Map<String, List<ResourceLocation>> serialize(Map<UUID, Set<StageId>> source) {
        Map<String, List<ResourceLocation>> result = new HashMap<>();
        for (Map.Entry<UUID, Set<StageId>> entry : source.entrySet()) {
            List<ResourceLocation> stages = new ArrayList<>();
            for (StageId stageId : entry.getValue()) {
                stages.add(stageId.getResourceLocation());
            }
            result.put(entry.getKey().toString(), stages);
        }
        return result;
    }

    /**
     * Check if a team has a specific stage
     */
    public boolean hasStage(UUID teamId, StageId stageId) {
        Set<StageId> stages = teamStages.get(teamId);
        return stages != null && stages.contains(stageId);
    }

    public boolean hasPersonalStage(UUID playerId, StageId stageId) {
        Set<StageId> stages = personalStages.get(playerId);
        return stages != null && stages.contains(stageId);
    }

    /**
     * Grant a stage to a team
     * @return true if the stage was newly granted, false if already had it
     */
    public boolean grantStage(UUID teamId, StageId stageId) {
        return grantStageFromSource(teamId, stageId, "independent");
    }

    public boolean grantStageFromSource(UUID teamId, StageId stageId, String source) {
        requireReadable();
        if (source == null || source.isBlank()
            || permissionSourceBlocked(new OwnerRef(teamKind(teamId), teamId), stageId, source)) return false;
        ownershipSchema = CURRENT_SCHEMA;
        Set<StageId> stages = teamStages.computeIfAbsent(teamId, k -> new HashSet<>());
        if (stages.contains(stageId) && getSources(teamId, stageId).isEmpty()) {
            addSource(teamKind(teamId), teamId, stageId, "independent");
        }
        boolean changed = stages.add(stageId);
        addSource(teamKind(teamId), teamId, stageId, source);
        return changed;
    }

    /**
     * Revoke a stage from a team
     * @return true if the stage was revoked, false if didn't have it
     */
    public boolean revokeStage(UUID teamId, StageId stageId) {
        requireReadable();
        suppressPermissionEpisodes(new OwnerRef(teamKind(teamId), teamId), stageId);
        Set<StageId> stages = teamStages.get(teamId);
        if (stages != null) {
            boolean removed = stages.remove(stageId);
            if (removed) {
                ownershipSchema = CURRENT_SCHEMA;
                removeSources(teamKind(teamId), teamId, stageId);
            }
            return removed;
        }
        return false;
    }

    public boolean grantPersonalStage(UUID playerId, StageId stageId) {
        return grantPersonalStageFromSource(playerId, stageId, "independent");
    }

    public boolean grantPersonalStageFromSource(UUID playerId, StageId stageId, String source) {
        requireReadable();
        if (source == null || source.isBlank()
            || permissionSourceBlocked(new OwnerRef(OwnerKind.PERSONAL, playerId), stageId, source)) return false;
        ownershipSchema = CURRENT_SCHEMA;
        Set<StageId> stages = personalStages.computeIfAbsent(playerId, k -> new HashSet<>());
        if (stages.contains(stageId)
                && getSources(new OwnerRef(OwnerKind.PERSONAL, playerId), stageId).isEmpty()) {
            addSource(OwnerKind.PERSONAL, playerId, stageId, "independent");
        }
        boolean changed = stages.add(stageId);
        addSource(OwnerKind.PERSONAL, playerId, stageId, source);
        return changed;
    }

    public boolean revokePersonalStage(UUID playerId, StageId stageId) {
        requireReadable();
        suppressPermissionEpisodes(new OwnerRef(OwnerKind.PERSONAL, playerId), stageId);
        Set<StageId> stages = personalStages.get(playerId);
        if (stages == null) return false;
        boolean removed = stages.remove(stageId);
        if (removed) {
            ownershipSchema = CURRENT_SCHEMA;
            removeSources(OwnerKind.PERSONAL, playerId, stageId);
        }
        return removed;
    }

    public Set<String> getSources(UUID owner, StageId stageId) {
        return getSources(new OwnerRef(teamKind(owner), owner), stageId);
    }

    public Set<String> getSources(OwnerRef owner, StageId stageId) {
        Map<StageId, Set<String>> stages = stageSources.get(new SourceOwner(owner.kind(), owner.id()));
        if (stages == null) return Set.of();
        Set<String> sources = stages.get(stageId);
        return sources == null ? Set.of() : Set.copyOf(sources);
    }

    public boolean hasEffectiveStage(OwnerRef owner, StageId stage) {
        Set<StageId> stored = owner.kind() == OwnerKind.PERSONAL
            ? personalStages.get(owner.id()) : teamStages.get(owner.id());
        if (stored == null || !stored.contains(stage)) return false;
        Map<StageId, Set<String>> records = stageSources.get(new SourceOwner(owner.kind(), owner.id()));
        Set<String> sources = records == null ? null : records.get(stage);
        return sources == null || sources.isEmpty() || sources.stream().anyMatch(source -> sourceIsActive(owner, stage, source));
    }

    public Set<StageId> getEffectiveStages(OwnerRef owner) {
        Set<StageId> stored = owner.kind() == OwnerKind.PERSONAL
            ? personalStages.get(owner.id()) : teamStages.get(owner.id());
        if (stored == null) return Set.of();
        Set<StageId> result = new HashSet<>();
        for (StageId stage : stored) if (hasEffectiveStage(owner, stage)) result.add(stage);
        return Set.copyOf(result);
    }

    public Set<String> getEffectiveSources(OwnerRef owner, StageId stage) {
        Set<String> result = new HashSet<>();
        for (String source : getSources(owner, stage)) if (sourceIsActive(owner, stage, source)) result.add(source);
        return Set.copyOf(result);
    }

    private boolean sourceIsActive(OwnerRef owner, StageId stage, String source) {
        return !permissionSourceBlocked(owner, stage, source)
            && (StageSourceKind.fromLabel(source) != StageSourceKind.LUCKPERMS_SYNCHRONIZED
                || activeSynchronizedSources.contains(new ActiveSource(new SourceOwner(owner.kind(), owner.id()), stage, source)));
    }

    public boolean revokeStageFromSource(UUID owner, StageId stageId, String source, boolean personal) {
        OwnerKind kind = personal ? OwnerKind.PERSONAL : teamKind(owner);
        return revokeStageFromSource(new OwnerRef(kind, owner), stageId, source);
    }

    public boolean revokeStageFromSource(OwnerRef owner, StageId stageId, String source) {
        requireReadable();
        Objects.requireNonNull(owner, "owner");
        OwnerKind kind = owner.kind();
        Map<StageId, Set<String>> stages = stageSources.get(new SourceOwner(kind, owner.id()));
        Set<String> sources = stages == null ? null : stages.get(stageId);
        if (sources == null || !sources.remove(source)) return false;
        ownershipSchema = CURRENT_SCHEMA;
        updatePermissionIndex(kind, owner.id(), stageId, source, false);
        activeSynchronizedSources.remove(new ActiveSource(new SourceOwner(kind, owner.id()), stageId, source));
        if (sources.isEmpty()) {
            Set<StageId> owned = (kind == OwnerKind.PERSONAL ? personalStages : teamStages).get(owner.id());
            if (owned != null) owned.remove(stageId);
            removeSources(kind, owner.id(), stageId);
        }
        return true;
    }

    private void addSource(OwnerKind kind, UUID owner, StageId stageId, String source) {
        if (source == null || source.isBlank()) return;
        String label = source.trim();
        if (StageSourceKind.fromLabel(label) == StageSourceKind.LUCKPERMS_SYNCHRONIZED) {
            activeSynchronizedSources.add(new ActiveSource(new SourceOwner(kind, owner), stageId, label));
        }
        if (stageSources.computeIfAbsent(new SourceOwner(kind, owner), ignored -> new HashMap<>())
                .computeIfAbsent(stageId, ignored -> new HashSet<>()).add(label)) {
            updatePermissionIndex(kind, owner, stageId, label, true);
        }
    }

    public Set<PermissionContribution> getPermissionContributions(UUID subject) {
        Set<PermissionContribution> sources = permissionContributions.get(subject);
        return sources == null ? Set.of() : Set.copyOf(sources);
    }

    public TeamStageData copyPermissionView(UUID subject, Map<StageId, OwnerRef> owners) {
        requireReadable();
        TeamStageData draft = new TeamStageData();
        draft.ownershipSchema = ownershipSchema;
        Set<OwnerStage> selected = new HashSet<>();
        owners.forEach((stage, owner) -> selected.add(new OwnerStage(owner, stage)));
        getPermissionContributions(subject).forEach(source -> selected.add(new OwnerStage(source.owner(), source.stage())));
        getPermissionEpisodes(subject).forEach(episode -> selected.add(new OwnerStage(episode.owner(), episode.stage())));
        for (OwnerStage selectedStage : selected) {
            OwnerRef owner = selectedStage.owner();
            StageId stage = selectedStage.stage();
            Map<UUID, Set<StageId>> stored = owner.kind() == OwnerKind.PERSONAL ? personalStages : teamStages;
            if (stored.getOrDefault(owner.id(), Set.of()).contains(stage)) {
                var target = owner.kind() == OwnerKind.PERSONAL ? draft.personalStages : draft.teamStages;
                target.computeIfAbsent(owner.id(), ignored -> new HashSet<>()).add(stage);
            }
            SourceOwner sourceOwner = new SourceOwner(owner.kind(), owner.id());
            for (String label : getSources(owner, stage)) {
                draft.stageSources.computeIfAbsent(sourceOwner, ignored -> new HashMap<>())
                    .computeIfAbsent(stage, ignored -> new HashSet<>()).add(label);
                draft.updatePermissionIndex(owner.kind(), owner.id(), stage, label, true);
                ActiveSource active = new ActiveSource(sourceOwner, stage, label);
                if (activeSynchronizedSources.contains(active)) draft.activeSynchronizedSources.add(active);
            }
            for (EpisodeKey key : ownerEpisodes.getOrDefault(selectedStage, Set.of())) {
                draft.putPermissionEpisode(permissionEpisodes.get(key));
            }
        }
        return draft;
    }

    public Set<OwnerRef> replacePermissionSubject(UUID subject, TeamStageData draft) {
        requireReadable();
        draft.requireReadable();
        Set<PermissionContribution> previous = getPermissionContributions(subject);
        Set<PermissionContribution> next = draft.getPermissionContributions(subject);
        Set<OwnerRef> changed = new HashSet<>();
        var previousEpisodes = new HashSet<>(getPermissionEpisodes(subject));
        var nextEpisodes = new HashSet<>(draft.getPermissionEpisodes(subject));
        for (PermissionEpisode episode : previousEpisodes) {
            if (!nextEpisodes.contains(episode)) changed.add(episode.owner());
        }
        for (PermissionEpisode episode : nextEpisodes) {
            if (!previousEpisodes.contains(episode)) changed.add(episode.owner());
        }
        if (!previousEpisodes.equals(nextEpisodes)) restorePermissionEpisodes(subject, List.copyOf(nextEpisodes));
        for (PermissionContribution source : previous) {
            if (!next.contains(source) && revokeStageFromSource(source.owner(), source.stage(), source.source().label())) {
                changed.add(source.owner());
            }
        }
        for (PermissionContribution source : next) {
            OwnerRef owner = source.owner();
            String label = source.source().label();
            ActiveSource active = new ActiveSource(new SourceOwner(owner.kind(), owner.id()), source.stage(), label);
            boolean wasActive = activeSynchronizedSources.contains(active);
            if (!previous.contains(source)) {
                var stored = owner.kind() == OwnerKind.PERSONAL ? personalStages : teamStages;
                Set<StageId> stages = stored.computeIfAbsent(owner.id(), ignored -> new HashSet<>());
                if (stages.contains(source.stage()) && getSources(owner, source.stage()).isEmpty()) {
                    addSource(owner.kind(), owner.id(), source.stage(), "independent");
                }
                stages.add(source.stage());
                addSource(owner.kind(), owner.id(), source.stage(), label);
                ownershipSchema = CURRENT_SCHEMA;
                changed.add(owner);
            }
            boolean nowActive = draft.activeSynchronizedSources.contains(active);
            if (nowActive) activeSynchronizedSources.add(active); else activeSynchronizedSources.remove(active);
            if (wasActive != nowActive) changed.add(owner);
        }
        return Set.copyOf(changed);
    }

    public UUID nextPermissionSubject(UUID previous) {
        UUID contribution = permissionContributions.isEmpty() ? null
            : previous == null ? permissionContributions.firstKey() : permissionContributions.higherKey(previous);
        UUID episode = subjectEpisodes.isEmpty() ? null
            : previous == null ? subjectEpisodes.firstKey() : subjectEpisodes.higherKey(previous);
        return contribution == null ? episode : episode == null ? contribution
            : contribution.compareTo(episode) < 0 ? contribution : episode;
    }

    public PermissionEpisode getPermissionEpisode(OwnerRef owner, StageId stage, PermissionStageSource source) {
        return permissionEpisodes.get(new EpisodeKey(owner, stage, source.subject(), source.row()));
    }

    public boolean putPermissionEpisode(PermissionEpisode episode) {
        requireReadable();
        EpisodeKey key = EpisodeKey.of(episode);
        PermissionEpisode previous = permissionEpisodes.put(key, episode);
        ownerEpisodes.computeIfAbsent(new OwnerStage(key.owner(), key.stage()), ignored -> new HashSet<>()).add(key);
        subjectEpisodes.computeIfAbsent(key.subject(), ignored -> new HashSet<>()).add(key);
        return !episode.equals(previous);
    }

    public List<PermissionEpisode> getPermissionEpisodes(UUID subject) {
        return subjectEpisodes.getOrDefault(subject, Set.of()).stream().map(permissionEpisodes::get).toList();
    }

    public void restorePermissionEpisodes(UUID subject, List<PermissionEpisode> episodes) {
        requireReadable();
        if (episodes.stream().anyMatch(episode -> !episode.subject().equals(subject))) {
            throw new IllegalArgumentException("Permission episode rollback contains another subject");
        }
        for (EpisodeKey key : Set.copyOf(subjectEpisodes.getOrDefault(subject, Set.of()))) {
            permissionEpisodes.remove(key);
            OwnerStage owner = new OwnerStage(key.owner(), key.stage());
            Set<EpisodeKey> entries = ownerEpisodes.get(owner);
            entries.remove(key);
            if (entries.isEmpty()) ownerEpisodes.remove(owner);
        }
        subjectEpisodes.remove(subject);
        episodes.forEach(this::putPermissionEpisode);
    }

    public boolean suppressPermissionEpisodes(OwnerRef owner, StageId stage) {
        requireReadable();
        boolean changed = false;
        for (String label : getSources(owner, stage)) {
            var source = PermissionStageSource.parse(label);
            if (source.isPresent() && getPermissionEpisode(owner, stage, source.get()) == null) {
                changed |= putPermissionEpisode(new PermissionEpisode(owner, stage, source.get().subject(),
                    source.get().row(), "", true, true, 0, 0));
            }
        }
        for (EpisodeKey key : ownerEpisodes.getOrDefault(new OwnerStage(owner, stage), Set.of())) {
            changed |= putPermissionEpisode(permissionEpisodes.get(key).suppress());
        }
        return changed;
    }

    public boolean allowPermissionEpisodes(OwnerRef owner, StageId stage) {
        requireReadable();
        boolean changed = false;
        for (EpisodeKey key : ownerEpisodes.getOrDefault(new OwnerStage(owner, stage), Set.of())) {
            changed |= putPermissionEpisode(permissionEpisodes.get(key).administrativeGrant());
        }
        return changed;
    }

    private boolean permissionSourceBlocked(OwnerRef owner, StageId stage, String label) {
        var source = PermissionStageSource.parse(label);
        PermissionEpisode episode = source.isEmpty() ? null : getPermissionEpisode(owner, stage, source.get());
        return episode != null && (episode.suppressed() || episode.expired(System.currentTimeMillis()));
    }

    public Set<OwnerRef> deactivatePermissionSources(UUID subject) {
        Set<OwnerRef> changed = new HashSet<>();
        for (PermissionContribution contribution : getPermissionContributions(subject)) {
            if (!contribution.source().permanent() && activeSynchronizedSources.remove(new ActiveSource(
                    new SourceOwner(contribution.owner().kind(), contribution.owner().id()),
                    contribution.stage(), contribution.source().label()))) {
                changed.add(contribution.owner());
            }
        }
        return Set.copyOf(changed);
    }

    private void indexPermissionSources() {
        permissionContributions.clear();
        stageSources.forEach((owner, stages) -> stages.forEach((stage, sources) ->
            sources.forEach(source -> updatePermissionIndex(owner.kind(), owner.id(), stage, source, true))));
    }

    private void updatePermissionIndex(OwnerKind kind, UUID owner, StageId stage, String label, boolean adding) {
        PermissionStageSource.parse(label).ifPresent(source -> {
            var contribution = new PermissionContribution(new OwnerRef(kind, owner), stage, source);
            if (adding) {
                permissionContributions.computeIfAbsent(source.subject(), ignored -> new HashSet<>()).add(contribution);
            } else {
                Set<PermissionContribution> entries = permissionContributions.get(source.subject());
                if (entries != null) {
                    entries.remove(contribution);
                    if (entries.isEmpty()) permissionContributions.remove(source.subject());
                }
            }
        });
    }

    private static OwnerKind teamKind(UUID owner) {
        return SERVER_OWNER.equals(owner) ? OwnerKind.SERVER : OwnerKind.TEAM;
    }

    private void removeSources(OwnerKind kind, UUID owner, StageId stageId) {
        Map<StageId, Set<String>> stages = stageSources.get(new SourceOwner(kind, owner));
        if (stages == null) return;
        Set<String> removed = stages.remove(stageId);
        if (removed != null) removed.forEach(source -> {
            updatePermissionIndex(kind, owner, stageId, source, false);
            activeSynchronizedSources.remove(new ActiveSource(new SourceOwner(kind, owner), stageId, source));
        });
        if (stages.isEmpty()) stageSources.remove(new SourceOwner(kind, owner));
    }

    /**
     * Get all stages for a team
     */
    public Set<StageId> getStages(UUID teamId) {
        Set<StageId> stages = teamStages.get(teamId);
        return stages != null ? Collections.unmodifiableSet(stages) : Collections.emptySet();
    }

    public Set<StageId> getPersonalStages(UUID playerId) {
        Set<StageId> stages = personalStages.get(playerId);
        return stages != null ? Collections.unmodifiableSet(stages) : Collections.emptySet();
    }

    /**
     * Set stages for a team (replaces existing)
     */
    public void setStages(UUID teamId, Set<StageId> stages) {
        requireReadable();
        ownershipSchema = CURRENT_SCHEMA;
        teamStages.put(teamId, new HashSet<>(stages));
        pruneSources(teamKind(teamId), teamId, stages);
    }

    public void setPersonalStages(UUID playerId, Set<StageId> stages) {
        requireReadable();
        ownershipSchema = CURRENT_SCHEMA;
        personalStages.put(playerId, new HashSet<>(stages));
        pruneSources(OwnerKind.PERSONAL, playerId, stages);
    }

    private void pruneSources(OwnerKind kind, UUID owner, Set<StageId> retained) {
        SourceOwner sourceOwner = new SourceOwner(kind, owner);
        for (OwnerStage key : Set.copyOf(ownerEpisodes.keySet())) {
            if (key.owner().kind() == kind && key.owner().id().equals(owner) && !retained.contains(key.stage())) {
                suppressPermissionEpisodes(key.owner(), key.stage());
            }
        }
        Map<StageId, Set<String>> sources = stageSources.get(sourceOwner);
        if (sources == null) return;
        for (StageId stage : Set.copyOf(sources.keySet())) {
            if (!retained.contains(stage)) {
                suppressPermissionEpisodes(new OwnerRef(kind, owner), stage);
                removeSources(kind, owner, stage);
            }
        }
    }

    /**
     * Remove all data for a team
     */
    public void removeTeam(UUID teamId) {
        requireReadable();
        teamStages.remove(teamId);
        pruneSources(teamKind(teamId), teamId, Set.of());
    }

    public void removePersonal(UUID playerId) {
        requireReadable();
        personalStages.remove(playerId);
        pruneSources(OwnerKind.PERSONAL, playerId, Set.of());
    }

    /**
     * Get all team IDs with stage data
     */
    public Set<UUID> getAllTeamIds() {
        return Collections.unmodifiableSet(teamStages.keySet());
    }

    /**
     * Get the "most advanced" stage a team has reached.
     * v1.3: Uses dependency depth instead of order number.
     * A stage with more dependencies is considered more advanced.
     */
    public Optional<StageId> getHighestStage(UUID teamId) {
        Set<StageId> stages = getEffectiveStages(new OwnerRef(teamKind(teamId), teamId));
        if (stages == null || stages.isEmpty()) {
            return Optional.empty();
        }

        StageId highest = null;
        int highestDepth = -1;

        for (StageId stageId : stages) {
            // v1.3: Use dependency depth instead of order
            int depth = com.enviouse.progressivestages.common.stage.StageOrder.getInstance()
                .getAllDependencies(stageId).size();
            if (depth > highestDepth) {
                highestDepth = depth;
                highest = stageId;
            }
        }

        return Optional.ofNullable(highest);
    }

    /**
     * Create a copy of this data
     */
    public TeamStageData copy() {
        if (unreadable != null) return preserveUnreadable(unreadable);
        TeamStageData copy = new TeamStageData();
        copy.ownershipSchema = ownershipSchema;
        copy.ftbHelperImportSchema = ftbHelperImportSchema;
        copy.pendingRewards.putAll(pendingRewards);
        ftbHelperImports.forEach((owner, stages) -> copy.ftbHelperImports.put(owner, new HashSet<>(stages)));
        for (Map.Entry<UUID, Set<StageId>> entry : teamStages.entrySet()) {
            copy.teamStages.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Map.Entry<UUID, Set<StageId>> entry : personalStages.entrySet()) {
            copy.personalStages.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Map.Entry<SourceOwner, Map<StageId, Set<String>>> entry : stageSources.entrySet()) {
            Map<StageId, Set<String>> stages = new HashMap<>();
            entry.getValue().forEach((stage, sources) -> stages.put(stage, new HashSet<>(sources)));
            copy.stageSources.put(entry.getKey(), stages);
        }
        permissionEpisodes.values().forEach(copy::putPermissionEpisode);
        copy.indexPermissionSources();
        copy.activeSynchronizedSources.addAll(activeSynchronizedSources);
        return copy;
    }
}
