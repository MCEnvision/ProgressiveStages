package com.enviouse.progressivestages.server.rehaul;

import com.enviouse.progressivestages.common.rehaul.condition.ConditionContext;
import com.enviouse.progressivestages.common.team.TeamProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server thread cache for entity presence condition contexts.
 *
 * <p>The cache intentionally owns no persistent state. A context is reusable only while the
 * player, game tick, compiled rule revision, explicit player revision, and the mutable facts
 * that are sampled before construction remain equal. This keeps entity tracking from rebuilding
 * the full condition map for every entity while preventing stale same tick decisions.</p>
 */
final class EntityPresenceContextCache {

    record Key(UUID playerId, long gameTime, long ruleRevision, long playerRevision,
               UUID teamId, int onlineTeamSize, PlayerFacts playerFacts) {}

    record PlayerFacts(ResourceLocation dimension, BlockPos position, int healthBits, int food,
                       long yBits, int experienceLevel, int totalExperience) {}

    record Stats(long constructions, long reuses) {}

    private record Entry(Key key, ConditionContext context) {}

    private final Map<UUID, Entry> entries = new HashMap<>();
    private final Map<UUID, Long> playerRevisions = new HashMap<>();
    private long constructions;
    private long reuses;

    ConditionContext contextFor(ServerPlayer player, RehaulRuntime runtime, long ruleRevision) {
        UUID playerId = player.getUUID();
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int onlineTeamSize = TeamProvider.getInstance().getTeamMembers(teamId, player).size();
        Key key = new Key(playerId, player.level().getGameTime(), ruleRevision,
            playerRevisions.getOrDefault(playerId, 0L), teamId, onlineTeamSize,
            playerFacts(player));
        return getOrCreate(key, () -> MinecraftConditionContextFactory.create(player, runtime, java.util.Set.of()));
    }

    ConditionContext getOrCreate(Key key, Supplier<ConditionContext> factory) {
        Entry cached = entries.get(key.playerId());
        if (cached != null && cached.key().equals(key)) {
            reuses++;
            return cached.context();
        }
        ConditionContext created = factory.get();
        entries.put(key.playerId(), new Entry(key, created));
        constructions++;
        return created;
    }

    void invalidate(UUID playerId) {
        if (playerId == null) return;
        playerRevisions.merge(playerId, 1L, Long::sum);
        entries.remove(playerId);
    }

    void invalidateTeam(UUID teamId) {
        if (teamId == null) {
            invalidateAll();
            return;
        }
        entries.entrySet().removeIf(entry -> {
            if (!teamId.equals(entry.getValue().key().teamId())) return false;
            playerRevisions.merge(entry.getKey(), 1L, Long::sum);
            return true;
        });
    }

    void invalidateAll() {
        entries.keySet().forEach(playerId -> playerRevisions.merge(playerId, 1L, Long::sum));
        entries.clear();
    }

    void clear(UUID playerId) {
        if (playerId == null) return;
        entries.remove(playerId);
        playerRevisions.remove(playerId);
    }

    void reset() {
        entries.clear();
        playerRevisions.clear();
        constructions = 0;
        reuses = 0;
    }

    Stats stats() {
        return new Stats(constructions, reuses);
    }

    private static PlayerFacts playerFacts(ServerPlayer player) {
        return new PlayerFacts(player.level().dimension().location(), player.blockPosition(),
            Float.floatToIntBits(player.getHealth()), player.getFoodData().getFoodLevel(),
            Double.doubleToLongBits(player.getY()), player.experienceLevel, player.totalExperience);
    }
}
