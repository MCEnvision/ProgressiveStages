package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.util.Constants;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class EntityPresenceFixtureProfiler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = Boolean.getBoolean("progressivestages.entityPresenceFixture");
    private static final int MAX_SAMPLES = Math.max(1, Integer.getInteger(
        "progressivestages.entityPresenceFixtureSamples", 20 * 60 * 5));
    private static final String REPORT_FILE = fixtureFileName(
        System.getProperty("progressivestages.entityPresenceFixtureLabel"));
    private static final long[] TICK_NANOS = new long[MAX_SAMPLES];
    private static final long[] PRESENCE_NANOS = new long[MAX_SAMPLES];

    private static Thread serverThread;
    private static long tickStartedAt;
    private static long presenceNanos;
    private static int sampleCount;

    private EntityPresenceFixtureProfiler() {}

    static long beginDecision() {
        return ENABLED && Thread.currentThread() == serverThread ? System.nanoTime() : 0L;
    }

    static void endDecision(long startedAt) {
        if (startedAt != 0L) presenceNanos += System.nanoTime() - startedAt;
    }

    @SubscribeEvent
    public static void onServerTickPre(ServerTickEvent.Pre event) {
        if (!ENABLED || sampleCount >= MAX_SAMPLES) return;
        serverThread = Thread.currentThread();
        presenceNanos = 0L;
        tickStartedAt = System.nanoTime();
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        if (!ENABLED || tickStartedAt == 0L || sampleCount >= MAX_SAMPLES) return;
        TICK_NANOS[sampleCount] = System.nanoTime() - tickStartedAt;
        PRESENCE_NANOS[sampleCount] = presenceNanos;
        sampleCount++;
        tickStartedAt = 0L;
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (!ENABLED) return;
        FixtureSummary summary = summarize(TICK_NANOS, PRESENCE_NANOS, sampleCount);
        Path output = event.getServer().getServerDirectory().resolve("debug")
            .resolve(REPORT_FILE);
        try {
            writeReport(output, summary, TICK_NANOS, PRESENCE_NANOS, sampleCount);
            LOGGER.info("ProgressiveStages entity presence fixture wrote {} samples to {}. p95 {} ms. share {} percent.",
                summary.sampleCount(), output, nanosToMillis(summary.p95TickNanos()), summary.presenceSharePercent());
        } catch (IOException exception) {
            LOGGER.error("ProgressiveStages could not write the entity presence fixture report to {}.", output, exception);
        }
    }

    static FixtureSummary summarize(long[] tickNanos, long[] presenceNanos, int sampleCount) {
        int count = Math.max(0, Math.min(sampleCount, Math.min(tickNanos.length, presenceNanos.length)));
        if (count == 0) return new FixtureSummary(0, 0L, 0L, 0L, 0.0D);
        long[] orderedTicks = Arrays.copyOf(tickNanos, count);
        Arrays.sort(orderedTicks);
        long totalTickNanos = 0L;
        long totalPresenceNanos = 0L;
        for (int index = 0; index < count; index++) {
            totalTickNanos += tickNanos[index];
            totalPresenceNanos += presenceNanos[index];
        }
        int percentileIndex = Math.max(0, (int) Math.ceil(count * 0.95D) - 1);
        double presenceShare = totalTickNanos == 0L ? 0.0D
            : (double) totalPresenceNanos / (double) totalTickNanos * 100.0D;
        return new FixtureSummary(count, orderedTicks[percentileIndex], totalTickNanos, totalPresenceNanos,
            presenceShare);
    }

    private static void writeReport(Path output, FixtureSummary summary, long[] tickNanos,
                                    long[] presenceNanos, int sampleCount) throws IOException {
        Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("sample,tick_nanos,entity_presence_nanos");
            writer.newLine();
            for (int index = 0; index < sampleCount; index++) {
                writer.write(index + "," + tickNanos[index] + "," + presenceNanos[index]);
                writer.newLine();
            }
            writer.write("summary,p95_tick_nanos," + summary.p95TickNanos());
            writer.newLine();
            writer.write("summary,total_tick_nanos," + summary.totalTickNanos());
            writer.newLine();
            writer.write("summary,total_entity_presence_nanos," + summary.totalPresenceNanos());
            writer.newLine();
            writer.write("summary,entity_presence_share_percent," + summary.presenceSharePercent());
            writer.newLine();
        }
    }

    private static double nanosToMillis(long nanos) {
        return nanos / 1_000_000.0D;
    }

    static String fixtureFileName(String label) {
        String normalized = label == null ? "capture" : label.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]{0,63}")) normalized = "capture";
        return "progressivestages-entity-presence-fixture-" + normalized + ".csv";
    }

    record FixtureSummary(int sampleCount, long p95TickNanos, long totalTickNanos, long totalPresenceNanos,
                          double presenceSharePercent) {}
}
