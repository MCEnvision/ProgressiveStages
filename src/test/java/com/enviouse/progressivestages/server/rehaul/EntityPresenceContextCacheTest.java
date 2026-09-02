package com.enviouse.progressivestages.server.rehaul;

import com.enviouse.progressivestages.common.rehaul.condition.ConditionContext;
import com.enviouse.progressivestages.common.rehaul.condition.SubjectScope;
import org.junit.jupiter.api.Test;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class EntityPresenceContextCacheTest {

    @Test
    void reusesOneContextForEveryDecisionInAnUnchangedTuple() {
        EntityPresenceContextCache cache = new EntityPresenceContextCache();
        UUID player = UUID.randomUUID();
        EntityPresenceContextCache.Key key = key(player, 100, 4, 2, 9, 3);
        AtomicInteger created = new AtomicInteger();

        ConditionContext first = cache.getOrCreate(key, () -> context(created.incrementAndGet()));
        ConditionContext second = null;
        for (int decision = 0; decision < 1_000; decision++) {
            second = cache.getOrCreate(key, () -> context(created.incrementAndGet()));
            assertSame(first, second);
        }

        assertSame(first, second);
        assertEquals(1, created.get());
        assertEquals(new EntityPresenceContextCache.Stats(1, 1_000), cache.stats());
    }

    @Test
    void rebuildsWhenAnyAuthoritativeTupleFactChanges() {
        EntityPresenceContextCache cache = new EntityPresenceContextCache();
        UUID player = UUID.randomUUID();
        AtomicInteger created = new AtomicInteger();

        ConditionContext first = cache.getOrCreate(key(player, 100, 4, 2, 9, 3),
            () -> context(created.incrementAndGet()));
        ConditionContext afterRuleReload = cache.getOrCreate(key(player, 100, 5, 2, 9, 3),
            () -> context(created.incrementAndGet()));
        ConditionContext afterMetricChange = cache.getOrCreate(key(player, 100, 5, 3, 9, 3),
            () -> context(created.incrementAndGet()));
        ConditionContext afterTeamChange = cache.getOrCreate(key(player, 100, 5, 3, 9, 4),
            () -> context(created.incrementAndGet()));
        ConditionContext nextTick = cache.getOrCreate(key(player, 101, 5, 3, 9, 4),
            () -> context(created.incrementAndGet()));

        assertNotSame(first, afterRuleReload);
        assertNotSame(afterRuleReload, afterMetricChange);
        assertNotSame(afterMetricChange, afterTeamChange);
        assertNotSame(afterTeamChange, nextTick);
        assertEquals(5, created.get());
    }

    @Test
    void explicitInvalidationPreventsSameTickReuse() {
        EntityPresenceContextCache cache = new EntityPresenceContextCache();
        UUID player = UUID.randomUUID();
        AtomicInteger created = new AtomicInteger();
        EntityPresenceContextCache.Key key = key(player, 100, 4, 0, 9, 3);

        ConditionContext first = cache.getOrCreate(key, () -> context(created.incrementAndGet()));
        cache.invalidate(player);
        EntityPresenceContextCache.Key revisedKey = key(player, 100, 4, 1, 9, 3);
        ConditionContext replacement = cache.getOrCreate(revisedKey, () -> context(created.incrementAndGet()));
        ConditionContext repeatedReplacement = cache.getOrCreate(revisedKey,
            () -> context(created.incrementAndGet()));

        assertNotSame(first, replacement);
        assertSame(replacement, repeatedReplacement);
        assertEquals(2, created.get());
    }

    @Test
    void teamInvalidationOnlyEvictsAffectedCachedPlayers() {
        EntityPresenceContextCache cache = new EntityPresenceContextCache();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        UUID firstTeam = new UUID(0L, 1L);
        UUID secondTeam = new UUID(0L, 2L);
        AtomicInteger created = new AtomicInteger();

        EntityPresenceContextCache.Key firstKey = key(firstPlayer, 100, 4, 0, firstTeam, 3);
        EntityPresenceContextCache.Key secondKey = key(secondPlayer, 100, 4, 0, secondTeam, 3);
        ConditionContext first = cache.getOrCreate(firstKey, () -> context(created.incrementAndGet()));
        ConditionContext second = cache.getOrCreate(secondKey, () -> context(created.incrementAndGet()));

        cache.invalidateTeam(firstTeam);

        EntityPresenceContextCache.Key revisedFirstKey = key(firstPlayer, 100, 4, 1, firstTeam, 3);
        ConditionContext rebuiltFirst = cache.getOrCreate(revisedFirstKey,
            () -> context(created.incrementAndGet()));
        ConditionContext reusedSecond = cache.getOrCreate(secondKey, () -> context(created.incrementAndGet()));
        assertNotSame(first, rebuiltFirst);
        assertSame(second, reusedSecond);
        assertEquals(3, created.get());
    }

    @Test
    void globalInvalidationRebuildsEveryCachedPlayer() {
        EntityPresenceContextCache cache = new EntityPresenceContextCache();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        AtomicInteger created = new AtomicInteger();

        ConditionContext first = cache.getOrCreate(key(firstPlayer, 100, 4, 0, 9, 3),
            () -> context(created.incrementAndGet()));
        ConditionContext second = cache.getOrCreate(key(secondPlayer, 100, 4, 0, 10, 3),
            () -> context(created.incrementAndGet()));

        cache.invalidateAll();

        assertNotSame(first, cache.getOrCreate(key(firstPlayer, 100, 4, 1, 9, 3),
            () -> context(created.incrementAndGet())));
        assertNotSame(second, cache.getOrCreate(key(secondPlayer, 100, 4, 1, 10, 3),
            () -> context(created.incrementAndGet())));
        assertEquals(4, created.get());
    }

    private static EntityPresenceContextCache.Key key(UUID player, long tick, long ruleRevision,
                                                       long playerRevision, int teamSeed,
                                                       int teamSize) {
        return key(player, tick, ruleRevision, playerRevision, new UUID(0L, teamSeed), teamSize);
    }

    private static EntityPresenceContextCache.Key key(UUID player, long tick, long ruleRevision,
                                                       long playerRevision, UUID teamId,
                                                       int teamSize) {
        return new EntityPresenceContextCache.Key(player, tick, ruleRevision, playerRevision,
            teamId, teamSize,
            new EntityPresenceContextCache.PlayerFacts(ResourceLocation.withDefaultNamespace("overworld"),
                new BlockPos(0, 64, 0), 0, 20, 0L, 0, 0));
    }

    private static ConditionContext context(int sequence) {
        return new ConditionContext("player." + sequence, SubjectScope.PLAYER, sequence,
            Map.of("sequence", sequence), Set.of(), Map.of());
    }
}
