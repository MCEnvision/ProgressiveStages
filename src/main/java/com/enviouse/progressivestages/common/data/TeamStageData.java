package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
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
                .optionalFieldOf("stage_sources", Map.of()).forGetter(TeamStageData::serializeSources)
        ).apply(instance, TeamStageData::new)
    );

    // Team UUID -> Set of stage IDs
    private final Map<UUID, Set<StageId>> teamStages = new HashMap<>();
    /** Player UUID to stages explicitly owned by that player. */
    private final Map<UUID, Set<StageId>> personalStages = new HashMap<>();
    /** Owner UUID to stage id to source labels. Empty source sets mean legacy ownership. */
    private record SourceOwner(OwnerKind kind, UUID id) {}
    private final Map<SourceOwner, Map<StageId, Set<String>>> stageSources = new HashMap<>();
    private int ownershipSchema = CURRENT_SCHEMA;

    public TeamStageData() {}

    private TeamStageData(int schema, Map<String, List<ResourceLocation>> serialized,
                          Map<String, List<ResourceLocation>> serializedPersonal,
                          Map<String, Map<String, List<String>>> serializedSources) {
        ownershipSchema = schema;
        readStages(serialized, teamStages);
        readStages(serializedPersonal, personalStages);
        readSources(serializedSources);
    }

    public int getOwnershipSchema() { return ownershipSchema; }

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
        ownershipSchema = CURRENT_SCHEMA;
        Set<StageId> stages = teamStages.computeIfAbsent(teamId, k -> new HashSet<>());
        boolean changed = stages.add(stageId);
        addSource(teamKind(teamId), teamId, stageId, source);
        return changed;
    }

    /**
     * Revoke a stage from a team
     * @return true if the stage was revoked, false if didn't have it
     */
    public boolean revokeStage(UUID teamId, StageId stageId) {
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
        ownershipSchema = CURRENT_SCHEMA;
        Set<StageId> stages = personalStages.computeIfAbsent(playerId, k -> new HashSet<>());
        boolean changed = stages.add(stageId);
        addSource(OwnerKind.PERSONAL, playerId, stageId, source);
        return changed;
    }

    public boolean revokePersonalStage(UUID playerId, StageId stageId) {
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

    public boolean revokeStageFromSource(UUID owner, StageId stageId, String source, boolean personal) {
        OwnerKind kind = personal ? OwnerKind.PERSONAL : teamKind(owner);
        return revokeStageFromSource(new OwnerRef(kind, owner), stageId, source);
    }

    public boolean revokeStageFromSource(OwnerRef owner, StageId stageId, String source) {
        Objects.requireNonNull(owner, "owner");
        OwnerKind kind = owner.kind();
        Map<StageId, Set<String>> stages = stageSources.get(new SourceOwner(kind, owner.id()));
        Set<String> sources = stages == null ? null : stages.get(stageId);
        if (sources == null || !sources.remove(source)) return false;
        ownershipSchema = CURRENT_SCHEMA;
        if (sources.isEmpty()) {
            if (kind == OwnerKind.PERSONAL) revokePersonalStage(owner.id(), stageId);
            else revokeStage(owner.id(), stageId);
        }
        return true;
    }

    private void addSource(OwnerKind kind, UUID owner, StageId stageId, String source) {
        if (source == null || source.isBlank()) return;
        stageSources.computeIfAbsent(new SourceOwner(kind, owner), ignored -> new HashMap<>())
            .computeIfAbsent(stageId, ignored -> new HashSet<>()).add(source.trim());
    }

    private static OwnerKind teamKind(UUID owner) {
        return SERVER_OWNER.equals(owner) ? OwnerKind.SERVER : OwnerKind.TEAM;
    }

    private void removeSources(OwnerKind kind, UUID owner, StageId stageId) {
        Map<StageId, Set<String>> stages = stageSources.get(new SourceOwner(kind, owner));
        if (stages == null) return;
        stages.remove(stageId);
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
        ownershipSchema = CURRENT_SCHEMA;
        teamStages.put(teamId, new HashSet<>(stages));
        pruneSources(teamKind(teamId), teamId, stages);
    }

    public void setPersonalStages(UUID playerId, Set<StageId> stages) {
        ownershipSchema = CURRENT_SCHEMA;
        personalStages.put(playerId, new HashSet<>(stages));
        pruneSources(OwnerKind.PERSONAL, playerId, stages);
    }

    private void pruneSources(OwnerKind kind, UUID owner, Set<StageId> retained) {
        SourceOwner sourceOwner = new SourceOwner(kind, owner);
        Map<StageId, Set<String>> sources = stageSources.get(sourceOwner);
        if (sources == null) return;
        sources.keySet().removeIf(stage -> !retained.contains(stage));
        if (sources.isEmpty()) stageSources.remove(sourceOwner);
    }

    /**
     * Remove all data for a team
     */
    public void removeTeam(UUID teamId) {
        teamStages.remove(teamId);
        stageSources.remove(new SourceOwner(teamKind(teamId), teamId));
    }

    public void removePersonal(UUID playerId) {
        personalStages.remove(playerId);
        stageSources.remove(new SourceOwner(OwnerKind.PERSONAL, playerId));
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
        Set<StageId> stages = teamStages.get(teamId);
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
        TeamStageData copy = new TeamStageData();
        copy.ownershipSchema = ownershipSchema;
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
        return copy;
    }
}
