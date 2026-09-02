package com.enviouse.progressivestages.server.enforcement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityPresenceFixtureProfilerTest {

    @Test
    void summarizesRawFixtureSamplesAtTheNearestRankP95() {
        long[] ticks = new long[20];
        long[] presence = new long[20];
        for (int index = 0; index < ticks.length; index++) {
            ticks[index] = index + 1L;
            presence[index] = 1L;
        }

        var summary = EntityPresenceFixtureProfiler.summarize(ticks, presence, ticks.length);

        assertEquals(20, summary.sampleCount());
        assertEquals(19L, summary.p95TickNanos());
        assertEquals(210L, summary.totalTickNanos());
        assertEquals(20L, summary.totalPresenceNanos());
        assertEquals(20.0D / 210.0D * 100.0D, summary.presenceSharePercent(), 0.0000001D);
    }

    @Test
    void emptyFixtureHasNoPercentileOrShare() {
        var summary = EntityPresenceFixtureProfiler.summarize(new long[0], new long[0], 0);

        assertEquals(0, summary.sampleCount());
        assertEquals(0L, summary.p95TickNanos());
        assertEquals(0.0D, summary.presenceSharePercent());
    }
}
