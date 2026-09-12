package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.InteractionDecision;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.TriState;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Server owned bounded interaction diagnostics.
 */
public final class InteractionCaptureManager {

    public static final int MAX_SECONDS = 60;
    public static final int MAX_DECISIONS = 200;
    public static final int MAX_DECISIONS_PER_SECOND = 20;
    public static final int MAX_OUTPUT_BYTES = 128 * 1024;
    public static final int MAX_QUEUE = 256;
    public static final int MAX_STRING_LENGTH = 256;
    public static final int MAX_COLLECTION_LENGTH = 32;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object LOCK = new Object();
    private static volatile Capture active;
    private static volatile CaptureStatus lastStatus = CaptureStatus.inactive();

    private InteractionCaptureManager() {}

    public static StartResult start(MinecraftServer server, ServerPlayer target) {
        if (server == null || target == null) return StartResult.invalid();
        synchronized (LOCK) {
            if (active != null) return StartResult.alreadyActive(active.status());
            String id = UUID.randomUUID().toString().replace("-", "");
            Path output = server.getServerDirectory().resolve("debug")
                .resolve("progressivestages-interactions-" + id + ".jsonl");
            Capture capture = new Capture(id, target.getUUID(), output, server.getTickCount());
            active = capture;
            lastStatus = capture.status();
            capture.startWriter();
            return StartResult.started(capture.status());
        }
    }

    public static CaptureStatus status() {
        Capture capture = active;
        return capture == null ? lastStatus : capture.status();
    }

    public static boolean stop(StopReason reason) {
        Capture capture;
        synchronized (LOCK) {
            capture = active;
            if (capture == null) return false;
            active = null;
            capture.stop(reason == null ? StopReason.MANUAL : reason);
            lastStatus = capture.status();
        }
        return true;
    }

    public static void stopForTarget(UUID target, StopReason reason) {
        Capture capture = active;
        if (capture != null && target != null && target.equals(capture.target())) stop(reason);
    }

    public static void tick(MinecraftServer server) {
        Capture capture = active;
        if (capture == null || server == null) return;
        if (server.getPlayerList().getPlayer(capture.target()) == null) {
            stop(StopReason.TARGET_REMOVED);
            return;
        }
        capture.checkTimeout(server.getTickCount());
    }

    public static void stopForReload() {
        stop(StopReason.RELOAD);
    }

    public static void stopForShutdown() {
        stop(StopReason.SHUTDOWN);
    }

    public static void resetRuntimeState() {
        stop(StopReason.RESTART);
        lastStatus = CaptureStatus.inactive();
    }

    public static void record(ServerPlayer player, InteractionHand hand, ItemStack stack, Block block,
                              InteractionDecision decision, boolean canceled, TriState useBlock,
                              TriState useItem, InteractionResult result, String mutation) {
        Capture capture = active;
        if (capture == null || player == null || decision == null || !capture.target().equals(player.getUUID())) return;
        capture.record(player, hand, stack, block, decision, canceled, useBlock, useItem, result, mutation);
        lastStatus = capture.status();
    }

    public record StartResult(boolean started, boolean alreadyActive, boolean invalidTarget,
                              CaptureStatus status) {
        static StartResult started(CaptureStatus status) { return new StartResult(true, false, false, status); }
        static StartResult alreadyActive(CaptureStatus status) { return new StartResult(false, true, false, status); }
        static StartResult invalid() { return new StartResult(false, false, true, CaptureStatus.inactive()); }
    }

    public record CaptureStatus(boolean active, String captureId, String target, Path output,
                                int records, int dropped, int bytes, String stopReason) {
        static CaptureStatus inactive() {
            return new CaptureStatus(false, "", "", null, 0, 0, 0, "off");
        }
    }

    public enum StopReason {
        MANUAL("manual"), TIMEOUT("timeout"), DECISION_LIMIT("decision_limit"),
        RATE_LIMIT("rate_limit"), OUTPUT_LIMIT("output_limit"), QUEUE_LIMIT("queue_limit"),
        OUTPUT_ERROR("output_error"), TARGET_REMOVED("target_removed"), RELOAD("reload"),
        SHUTDOWN("shutdown"), RESTART("restart");

        private final String value;
        StopReason(String value) { this.value = value; }
        String value() { return value; }
    }

    private static final class Capture {
        private final String id;
        private final UUID target;
        private final Path output;
        private final long startedTick;
        private final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(MAX_QUEUE);
        private volatile boolean active = true;
        private volatile StopReason stopReason;
        private volatile int records;
        private volatile int dropped;
        private volatile int bytes;
        private long rateWindowStart;
        private int rateWindowRecords;
        private Thread writer;

        private Capture(String id, UUID target, Path output, long startedTick) {
            this.id = id;
            this.target = target;
            this.output = output;
            this.startedTick = startedTick;
            this.rateWindowStart = startedTick;
        }

        String id() { return id; }
        UUID target() { return target; }

        void startWriter() {
            writer = new Thread(this::write, "progressivestages-interaction-capture");
            writer.setDaemon(true);
            writer.start();
        }

        synchronized void checkTimeout(long currentTick) {
            if (active && currentTick - startedTick >= MAX_SECONDS * 20L) stop(StopReason.TIMEOUT);
        }

        synchronized void record(ServerPlayer player, InteractionHand hand, ItemStack stack, Block block,
                                 InteractionDecision decision, boolean canceled, TriState useBlock,
                                 TriState useItem, InteractionResult result, String mutation) {
            if (!active) return;
            long tick = player.level().getGameTime();
            if (tick - startedTick >= MAX_SECONDS * 20L) {
                stop(StopReason.TIMEOUT);
                return;
            }
            if (records >= MAX_DECISIONS) {
                stop(StopReason.DECISION_LIMIT);
                return;
            }
            if (tick - rateWindowStart >= 20L) {
                rateWindowStart = tick;
                rateWindowRecords = 0;
            }
            if (rateWindowRecords >= MAX_DECISIONS_PER_SECOND) {
                stop(StopReason.RATE_LIMIT);
                return;
            }
            String line = line(player, hand, stack, block, decision, canceled, useBlock, useItem, result, mutation, tick);
            int lineBytes = line.getBytes(StandardCharsets.UTF_8).length;
            if (bytes + lineBytes > MAX_OUTPUT_BYTES) {
                stop(StopReason.OUTPUT_LIMIT);
                return;
            }
            if (!queue.offer(line)) {
                dropped++;
                stop(StopReason.QUEUE_LIMIT);
                return;
            }
            records++;
            rateWindowRecords++;
            bytes += lineBytes;
        }

        synchronized void stop(StopReason reason) {
            if (!active) return;
            active = false;
            stopReason = reason;
            queue.offer("");
        }

        CaptureStatus status() {
            StopReason reason = stopReason;
            return new CaptureStatus(active, id, "selected", output, records, dropped, bytes,
                active ? "active" : (reason == null ? "off" : reason.value()));
        }

        private String line(ServerPlayer player, InteractionHand hand, ItemStack stack, Block block,
                            InteractionDecision decision, boolean canceled, TriState useBlock,
                            TriState useItem, InteractionResult result, String mutation, long tick) {
            String itemId = stack == null || stack.isEmpty() ? "" : InteractionCaptureManager.id(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            int itemCount = stack == null ? 0 : stack.getCount();
            String blockId = InteractionCaptureManager.id(block == null ? null : BuiltInRegistries.BLOCK.getKey(block));
            return "{\"capture_id\":\"" + id + "\",\"sequence\":" + (records + 1)
                + ",\"server_tick\":" + tick + ",\"side\":\"server\",\"category\":\"interactions\""
                + ",\"definition_revision\":" + StageFileLoader.getInstance().getCompiledSnapshot().revision()
                + ",\"target\":\"selected\",\"hand\":\"" + (hand == null ? "" : hand.name().toLowerCase(Locale.ROOT))
                + "\",\"item_id\":\"" + itemId + "\",\"item_count\":" + itemCount
                + ",\"block_id\":\"" + blockId + "\",\"matched_stages\":" + stages(decision.matchedStages())
                + ",\"missing_stages\":" + stages(decision.missingStages())
                + ",\"reason\":\"" + decision.reason().name().toLowerCase(Locale.ROOT)
                + "\",\"allowed\":" + decision.allowed() + ",\"canceled\":" + canceled
                + ",\"use_block\":\"" + (useBlock == null ? "" : useBlock.name().toLowerCase(Locale.ROOT))
                + "\",\"use_item\":\"" + (useItem == null ? "" : useItem.name().toLowerCase(Locale.ROOT))
                + "\",\"result\":\"" + (result == null ? "" : result.name().toLowerCase(Locale.ROOT))
                + "\",\"mutation\":\"" + esc(mutation) + "\"}\n";
        }

        private String stages(List<StageId> values) {
            StringBuilder output = new StringBuilder("[");
            int total = values == null ? 0 : values.size();
            int count = Math.min(total, MAX_COLLECTION_LENGTH);
            for (int index = 0; index < count; index++) {
                if (index > 0) output.append(',');
                output.append('\"').append(esc(values.get(index).toString())).append('\"');
            }
            if (total > count) {
                if (count > 0) output.append(',');
                output.append("\"...\"");
            }
            return output.append(']').toString();
        }

        private void write() {
            try {
                Files.createDirectories(output.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                    while (active || !queue.isEmpty()) {
                        String line = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (line != null && !line.isEmpty()) writer.write(line);
                    }
                }
            } catch (IOException exception) {
                LOGGER.error("ProgressiveStages interaction capture could not write {}.", output, exception);
                synchronized (this) {
                    if (active) stop(StopReason.OUTPUT_ERROR);
                    else if (stopReason == null) stopReason = StopReason.OUTPUT_ERROR;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String id(net.minecraft.resources.ResourceLocation value) {
        return value == null ? "" : esc(value.toString());
    }

    private static String esc(String value) {
        if (value == null) return "";
        String bounded = value.length() > MAX_STRING_LENGTH ? value.substring(0, MAX_STRING_LENGTH) + "..." : value;
        return bounded.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\n", "\\n").replace("\r", "\\r");
    }
}
