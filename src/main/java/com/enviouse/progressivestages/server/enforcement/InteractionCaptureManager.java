package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.InteractionDecision;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.StageManager;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
    private static volatile Capture lastCapture;

    private InteractionCaptureManager() {}

    public static StartResult start(MinecraftServer server, ServerPlayer target) {
        return start(server, target, "interactions");
    }

    public static StartResult start(MinecraftServer server, ServerPlayer target, String category) {
        if (server == null || target == null
                || server.getPlayerList().getPlayer(target.getUUID()) != target) return StartResult.invalid();
        String normalizedCategory = category == null || category.isBlank() ? "interactions"
            : category.trim().toLowerCase(Locale.ROOT);
        if (!List.of("interactions", "progression", "permissions", "editor").contains(normalizedCategory))
            return StartResult.invalid();
        synchronized (LOCK) {
            if (lastCapture != null && !lastCapture.canReplace())
                return StartResult.alreadyActive(lastCapture.status());
            String id = UUID.randomUUID().toString().replace("-", "");
            Path output = server.getServerDirectory().resolve("logs").resolve("progressivestages")
                .resolve(normalizedCategory).resolve(id + ".log");
            Capture capture = new Capture(id, target.getUUID(), normalizedCategory, output, server.getTickCount(),
                CaptureIdentity.snapshot());
            active = capture;
            lastCapture = capture;
            capture.startWriter();
            return StartResult.started(capture.status());
        }
    }

    public static CaptureStatus status() {
        Capture capture = lastCapture;
        return capture == null ? CaptureStatus.inactive() : capture.status();
    }

    public static boolean stop(StopReason reason) {
        Capture capture;
        synchronized (LOCK) {
            capture = active;
            if (capture == null || !capture.isActive()) return false;
            active = null;
            capture.stop(reason == null ? StopReason.MANUAL : reason);
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
        synchronized (LOCK) {
            if (lastCapture == null || lastCapture.canReplace()) lastCapture = null;
        }
    }

    public static void record(ServerPlayer player, InteractionHand hand, ItemStack stack, Block block,
                              InteractionDecision decision, boolean canceled, TriState useBlock,
                              TriState useItem, InteractionResult result, String mutation) {
        Capture capture = active;
        if (capture == null || player == null || decision == null
                || !capture.accepts("interactions", player.getUUID())) return;
        capture.record(player, hand, stack, block, decision, canceled, useBlock, useItem, result, mutation);
    }

    public static void recordProgression(ServerPlayer player, StageId stageId,
                                         java.util.Set<StageId> before, java.util.Set<StageId> after,
                                         String cause, String reason, int recipientCount) {
        Capture capture = active;
        if (capture == null || player == null || stageId == null
                || !capture.accepts("progression", player.getUUID())) return;
        capture.recordProgression(player, stageId, before, after, cause, reason, recipientCount);
    }

    public static void recordPermission(ServerPlayer player, StageId stageId, String ruleId,
                                        boolean desired, String reason, String providerState) {
        Capture capture = active;
        if (capture == null || player == null || stageId == null
                || !capture.accepts("permissions", player.getUUID())) return;
        capture.recordPermission(player, stageId, ruleId, desired, reason, providerState);
    }

    public static void recordCommandPermission(ServerPlayer player, StageId stageId,
            com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context,
            boolean allowed, boolean nativeAllowed, String reason) {
        Capture capture = active;
        if (capture == null || player == null || stageId == null
                || !capture.accepts("permissions", player.getUUID())) return;
        String path = context.getNodes().stream().map(node -> node.getNode())
            .filter(node -> node instanceof com.mojang.brigadier.tree.LiteralCommandNode<?>)
            .map(node -> node.getName()).collect(java.util.stream.Collectors.joining(" "));
        String executionNode = context.getNodes().isEmpty() ? context.getRootNode().getName()
            : context.getNodes().get(context.getNodes().size() - 1).getNode().getName();
        capture.recordCommandPermission(player, stageId, path, executionNode, allowed, nativeAllowed, reason);
    }

    public record StartResult(boolean started, boolean alreadyActive, boolean invalidTarget,
                              CaptureStatus status) {
        static StartResult started(CaptureStatus status) { return new StartResult(true, false, false, status); }
        static StartResult alreadyActive(CaptureStatus status) { return new StartResult(false, true, false, status); }
        static StartResult invalid() { return new StartResult(false, false, true, CaptureStatus.inactive()); }
    }

    public record CaptureStatus(boolean active, String captureId, String target, Path output,
                                int records, int dropped, int bytes, String stopReason,
                                String category, int queued, String outputState, int remainingSeconds) {
        static CaptureStatus inactive() {
            return new CaptureStatus(false, "", "", null, 0, 0, 0, "off", "", 0, "idle", 0);
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

    static final class Capture {
        private final String id;
        private final UUID target;
        private final String category;
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
        private volatile boolean writerFinished;
        private long currentTick;
        private Thread writer;
        private final Map<OwnerRef, String> ownerLabels = new LinkedHashMap<>();
        private final CaptureIdentity identity;
        private int reservedHeaderBytes;

        Capture(String id, UUID target, String category, Path output, long startedTick) {
            this(id, target, category, output, startedTick, null);
        }

        Capture(String id, UUID target, String category, Path output, long startedTick, CaptureIdentity identity) {
            this.id = id;
            this.target = target;
            this.category = category;
            this.output = output;
            this.startedTick = startedTick;
            this.rateWindowStart = startedTick;
            this.currentTick = startedTick;
            this.identity = identity;
            this.reservedHeaderBytes = identity == null ? 0 : CaptureIdentity.MAX_HEADER_BYTES;
        }

        String id() { return id; }
        UUID target() { return target; }
        String category() { return category; }
        boolean isActive() { return active; }
        boolean accepts(String requestedCategory, UUID actor) {
            return active && category.equals(requestedCategory) && target.equals(actor);
        }
        boolean canReplace() { return !active && writerFinished; }

        synchronized void startWriter() {
            if (writer != null) return;
            writer = new Thread(this::write, "progressivestages-interaction-capture");
            writer.setDaemon(true);
            writer.start();
        }

        synchronized void checkTimeout(long currentTick) {
            this.currentTick = currentTick;
            if (active && currentTick - startedTick >= MAX_SECONDS * 20L) stop(StopReason.TIMEOUT);
        }

        synchronized void record(ServerPlayer player, InteractionHand hand, ItemStack stack, Block block,
                                 InteractionDecision decision, boolean canceled, TriState useBlock,
                                 TriState useItem, InteractionResult result, String mutation) {
            long tick = player.getServer().getTickCount();
            recordLine(tick, () -> {
                return line(player, hand, stack, block, decision, canceled, useBlock, useItem, result, mutation, tick);
            });
        }

        synchronized void recordProgression(ServerPlayer player, StageId stageId,
                                             java.util.Set<StageId> before, java.util.Set<StageId> after,
                                             String cause, String reason, int recipientCount) {
            long tick = player.getServer().getTickCount();
            recordLine(tick, () -> {
                StageDefinition definition = StageFileLoader.getInstance().getStage(stageId).orElse(null);
                OwnerRef owner = StageManager.getInstance().getStageOwner(player, stageId);
                return progressionLine(stageId, definition, owner, before, after, cause, reason,
                    recipientCount, tick);
            });
        }

        synchronized void recordPermission(ServerPlayer player, StageId stageId, String ruleId,
                                            boolean desired, String reason, String providerState) {
            long tick = player.getServer().getTickCount();
            recordLine(tick, () -> {
                return "{\"capture_id\":\"" + id + "\",\"sequence\":" + (records + 1)
                    + ",\"server_tick\":" + tick + ",\"side\":\"server\",\"category\":\"permissions\""
                    + ",\"definition_revision\":" + StageFileLoader.getInstance().getCompiledSnapshot().revision()
                    + ",\"stage\":\"" + esc(stageId.toString()) + "\",\"rule_id\":\""
                    + esc(ruleId) + "\",\"desired\":" + desired + ",\"provider_state\":\""
                    + esc(providerState) + "\",\"reason\":\"" + esc(reason) + "\"}\n";
            });
        }

        synchronized void recordCommandPermission(ServerPlayer player, StageId stageId, String path, String executionNode,
                                                   boolean allowed, boolean nativeAllowed, String reason) {
            long tick = player.getServer().getTickCount();
            recordLine(tick, () -> {
                return "{\"capture_id\":\"" + id + "\",\"sequence\":" + (records + 1)
                    + ",\"server_tick\":" + tick + ",\"side\":\"server\",\"category\":\"permissions\""
                    + ",\"command_path\":\"" + esc(path) + "\",\"stage\":\""
                    + esc(stageId.toString()) + "\",\"stage_allowed\":" + allowed
                    + ",\"native_allowed\":" + nativeAllowed + ",\"effective_actor_label\":\"target\""
                    + ",\"execution_node\":\"" + esc(executionNode) + "\""
                    + ",\"decision\":\"" + esc(reason) + "\"}\n";
            });
        }

        synchronized void recordLine(long tick, Supplier<String> record) {
            if (!active) return;
            checkTimeout(tick);
            if (!active) return;
            if (records >= MAX_DECISIONS) { stop(StopReason.DECISION_LIMIT); return; }
            if (tick - rateWindowStart >= 20L) { rateWindowStart = tick; rateWindowRecords = 0; }
            if (rateWindowRecords >= MAX_DECISIONS_PER_SECOND) { stop(StopReason.RATE_LIMIT); return; }
            String line = record.get();
            int lineBytes = line.getBytes(StandardCharsets.UTF_8).length;
            if (lineBytes > MAX_OUTPUT_BYTES - reservedHeaderBytes - bytes) { stop(StopReason.OUTPUT_LIMIT); return; }
            if (!queue.offer(line)) { dropped++; stop(StopReason.QUEUE_LIMIT); return; }
            records++;
            rateWindowRecords++;
            bytes += lineBytes;
            if (bytes == MAX_OUTPUT_BYTES) stop(StopReason.OUTPUT_LIMIT);
            else if (records == MAX_DECISIONS) stop(StopReason.DECISION_LIMIT);
            else if (rateWindowRecords == MAX_DECISIONS_PER_SECOND) stop(StopReason.RATE_LIMIT);
        }

        synchronized void stop(StopReason reason) {
            if (!active) return;
            active = false;
            stopReason = reason;
        }

        synchronized CaptureStatus status() {
            StopReason reason = stopReason;
            return new CaptureStatus(active, id, "selected", output, records, dropped, bytes,
                active ? "active" : (reason == null ? "off" : reason.value()), category, queue.size(),
                writerFinished ? (reason == StopReason.OUTPUT_ERROR ? "failed" : "drained")
                    : (active ? "writing" : "draining"),
                active ? (int) Math.max(0, MAX_SECONDS - (currentTick - startedTick) / 20L) : 0);
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
                + ",\"block_id\":\"" + blockId + "\"," + stageFields("matched_stages", decision.matchedStages())
                + "," + stageFields("missing_stages", decision.missingStages())
                + ",\"reason\":\"" + decision.reason().name().toLowerCase(Locale.ROOT)
                + "\",\"allowed\":" + decision.allowed() + ",\"canceled\":" + canceled
                + ",\"use_block\":\"" + (useBlock == null ? "" : useBlock.name().toLowerCase(Locale.ROOT))
                + "\",\"use_item\":\"" + (useItem == null ? "" : useItem.name().toLowerCase(Locale.ROOT))
                + "\",\"result\":\"" + (result == null ? "" : result.name().toLowerCase(Locale.ROOT))
                + "\",\"mutation\":\"" + esc(mutation) + "\"}\n";
        }

        private String progressionLine(StageId stageId, StageDefinition definition, OwnerRef owner,
                                       java.util.Set<StageId> before, java.util.Set<StageId> after,
                                       String cause, String reason, int recipientCount, long tick) {
            String ownerKind = owner == null ? "unknown" : owner.kind().name().toLowerCase(Locale.ROOT);
            String ownerLabel = ownerLabel(owner);
            boolean scopePresent = definition != null && definition.isScopePresent();
            boolean teamStagePresent = definition != null && definition.getTeamStage().isPresent();
            String teamStageValue = teamStagePresent ? String.valueOf(definition.getTeamStage().orElse(false)) : "";
            return "{\"capture_id\":\"" + id + "\",\"sequence\":" + (records + 1)
                + ",\"server_tick\":" + tick + ",\"side\":\"server\",\"category\":\"progression\""
                + ",\"definition_revision\":" + StageFileLoader.getInstance().getCompiledSnapshot().revision()
                + ",\"stage\":\"" + esc(stageId.toString()) + "\",\"scope_present\":" + scopePresent
                + ",\"team_stage_present\":" + teamStagePresent + ",\"team_stage_value\":\""
                + esc(teamStageValue) + "\",\"owner_kind\":\"" + ownerKind + "\",\"owner_label\":\""
                + esc(ownerLabel) + "\",\"cause\":\"" + esc(cause) + "\",\"reason\":\""
                + esc(reason) + "\"," + stageFields("before_effective", before == null ? List.of() : before.stream().sorted(java.util.Comparator.comparing(StageId::toString)).toList())
                + "," + stageFields("after_effective", after == null ? List.of() : after.stream().sorted(java.util.Comparator.comparing(StageId::toString)).toList())
                + ",\"recipient_count\":" + Math.max(0, recipientCount)
                + ",\"mutation_revision\":" + StageManager.getInstance().getMutationRevision() + "}\n";
        }

        String ownerLabel(OwnerRef owner) {
            if (owner == null) return "unknown";
            String existing = ownerLabels.get(owner);
            if (existing != null) return existing;
            if (ownerLabels.size() >= MAX_COLLECTION_LENGTH) return "owner_truncated";
            String label = "owner" + (ownerLabels.size() + 1);
            ownerLabels.put(owner, label);
            return label;
        }

        String stageFields(String name, List<StageId> values) {
            StringBuilder output = new StringBuilder("\"").append(name).append("\":[");
            int total = values == null ? 0 : values.size();
            int count = Math.min(total, MAX_COLLECTION_LENGTH);
            for (int index = 0; index < count; index++) {
                if (index > 0) output.append(',');
                output.append('"').append(esc(values.get(index).toString())).append('"');
            }
            return output.append("],\"").append(name).append("_total\":").append(total)
                .append(",\"").append(name).append("_truncated\":").append(total > count).toString();
        }

        private void write() {
            try {
                Files.createDirectories(output.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    if (identity != null) {
                        String header = identity.header(id, category, startedTick);
                        synchronized (this) {
                            int headerBytes = header.getBytes(StandardCharsets.UTF_8).length;
                            if (headerBytes > reservedHeaderBytes) throw new IOException("Capture header exceeds its reserved limit");
                            bytes += headerBytes;
                            reservedHeaderBytes = 0;
                        }
                        writer.write(header);
                    }
                    while (active || !queue.isEmpty()) {
                        String line = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (line != null && !line.isEmpty()) writer.write(line);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                failOutput();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                failOutput();
            } finally {
                writerFinished = true;
            }
        }

        private synchronized void failOutput() {
            active = false;
            stopReason = StopReason.OUTPUT_ERROR;
            dropped += queue.size();
            queue.clear();
            LOGGER.error("ProgressiveStages diagnostic capture {} could not finish writing.", id);
        }
    }

    private static String id(net.minecraft.resources.ResourceLocation value) {
        return value == null ? "" : esc(value.toString());
    }

    static String esc(String value) {
        String encoded = new com.google.gson.JsonPrimitive(bounded(value)).toString();
        return encoded.substring(1, encoded.length() - 1);
    }

    static String bounded(String value) {
        if (value == null) return "";
        if (value.length() > MAX_STRING_LENGTH) {
            int end = MAX_STRING_LENGTH - 3;
            if (Character.isHighSurrogate(value.charAt(end - 1))) end--;
            value = value.substring(0, end) + "...";
        }
        return value;
    }
}
