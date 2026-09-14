package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.InteractionDecision;
import com.enviouse.progressivestages.common.api.StageId;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class DiagnosticPerformanceGameTests {
    private DiagnosticPerformanceGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 6000, batch = "progressivestages_diagnostic_performance")
    public static void captureOverheadStaysBoundedAcrossCategories(GameTestHelper helper) throws Exception {
        new Measurement(helper).start();
    }

    private static final class Measurement {
        private static final int ATTEMPTS = 1000;
        private static final int WINDOW = 20;
        private static final List<String> CATEGORIES = List.of("interactions", "progression", "permissions", "commands", "editor");
        private final GameTestHelper helper;
        private final ServerPlayer player;
        private final Map<UUID, ServerPlayer> players;
        private final com.sun.management.ThreadMXBean threads;
        private final long threadId = Thread.currentThread().threadId();
        private final long startedTick;
        private final Runnable[] operations;
        private int category;
        private int round;
        private int accepted;
        private long enabledCpu;
        private long enabledWall;
        private long disabledCpu;
        private long disabledWall;
        private long disabledAllocation;
        private Path pendingOutput;
        private Throwable failure;
        private volatile Object allocationControl;

        @SuppressWarnings("unchecked")
        Measurement(GameTestHelper helper) throws Exception {
            this.helper = helper;
            var server = helper.getLevel().getServer();
            helper.assertTrue(server.isSameThread(), "Performance measurement must run on the server thread.");
            helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The benchmark requires an isolated server.");
            var status = InteractionCaptureManager.status();
            helper.assertTrue(!status.active() && (status.outputState().equals("idle") || status.outputState().equals("drained")),
                "The benchmark must not replace an existing capture or writer.");
            threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
            helper.assertTrue(threads.isCurrentThreadCpuTimeSupported() && threads.isThreadCpuTimeEnabled()
                    && threads.isThreadAllocatedMemorySupported() && threads.isThreadAllocatedMemoryEnabled(),
                "The benchmark requires enabled server thread CPU and allocation counters.");
            var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "capture-benchmark"), false);
            player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
            var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            players = (Map<UUID, ServerPlayer>) field.get(server.getPlayerList());
            helper.assertTrue(!players.containsKey(player.getUUID()), "The fixture identity must be unused.");
            startedTick = server.getTickCount();
            var stages = IntStream.range(0, 32).mapToObj(i -> StageId.parse("test:capture_" + i)).toList();
            var stage = stages.getLast();
            var decision = new InteractionDecision(List.of(stage), List.of(stage),
                InteractionDecision.Reason.STAGE_MISSING, false);
            var stack = new ItemStack(Items.BREAD, 64);
            var before = Set.of(stage);
            var after = Set.of(stage);
            var context = server.getCommands().getDispatcher().parse("time query gametime", server.createCommandSourceStack())
                .getContext().build("time query gametime");
            var source = Map.of("stage.toml", "[stage]\nid = \"capture\"\nteam_stage = false\n");
            operations = new Runnable[] {
                () -> InteractionCaptureManager.record(player, InteractionHand.MAIN_HAND, stack, Blocks.CHEST,
                    decision, true, TriState.FALSE, TriState.FALSE, InteractionResult.FAIL, "denied"),
                () -> InteractionCaptureManager.recordProgression(player, stage, before, after, "command", "granted", 1),
                () -> InteractionCaptureManager.recordPermission(player, stage, "capture", true, "reconciled", "absent"),
                () -> InteractionCaptureManager.recordCommandPermission(player, stage, context, false, true, "stage_missing"),
                () -> {
                    var operation = InteractionCaptureManager.beginEditor(player);
                    if (operation != null) operation.complete(new EditorCaptureRecord("validate", 1, 1, 1, 0, 0,
                        server.getTickCount(), "accepted", "valid", "absent", source, source));
                }
            };
        }

        void start() {
            players.put(player.getUUID(), player);
            InteractionCaptureManager.resetRuntimeState();
            try {
                Runnable empty = () -> {};
                Runnable allocating = () -> allocationControl = new byte[64];
                for (int warmup = 0; warmup < 20; warmup++) {
                    for (Runnable operation : operations) measureAllocation(operation, ATTEMPTS);
                    measureAllocation(empty, ATTEMPTS);
                    measureAllocation(allocating, ATTEMPTS);
                }
                helper.assertTrue(measureAllocation(empty, ATTEMPTS) == 0,
                    "The allocation probe must report zero for an empty operation.");
                helper.assertTrue(measureAllocation(allocating, ATTEMPTS) >= 64L * ATTEMPTS,
                    "The allocation probe must detect an escaping allocation on every attempt.");
                allocationControl = null;
                step();
            } catch (Throwable error) {
                finish();
                throw error;
            }
        }

        private void step() {
            try {
                helper.assertTrue(Thread.currentThread().threadId() == threadId, "Measurement must stay on its server thread.");
                if (pendingOutput != null) {
                    var status = InteractionCaptureManager.status();
                    if (status.outputState().equals("writing") || status.outputState().equals("draining")) {
                        helper.assertTrue(helper.getLevel().getServer().getTickCount() - startedTick < 5800,
                            "The capture writer exceeded the bounded measurement deadline.");
                        Thread.yield();
                        try { Thread.sleep(1L); } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            throw interrupted;
                        }
                        helper.runAfterDelay(1, this::step);
                        return;
                    }
                    helper.assertTrue(status.outputState().equals("drained") && status.queued() == 0 && status.dropped() == 0,
                        "Every measured capture must drain without a dropped record.");
                    var lines = Files.readAllLines(pendingOutput);
                    helper.assertTrue(lines.size() == WINDOW + 1, "Every measured attempt must reach the output file.");
                    for (int i = 0; i < lines.size(); i++) {
                        var line = JsonParser.parseString(lines.get(i)).getAsJsonObject();
                        helper.assertTrue(line.get("sequence").getAsInt() == i
                                && line.get("capture_id").getAsString().equals(status.captureId())
                                && line.get("category").getAsString().equals(captureCategory()),
                            "Measured output must retain its capture identity and exact ordered category records.");
                    }
                    Files.delete(pendingOutput);
                    pendingOutput = null;
                    InteractionCaptureManager.resetRuntimeState();
                    helper.assertTrue(InteractionCaptureManager.status().stopReason().equals("off"),
                        "A drained capture must release the inactive runtime reference.");
                }
                if (accepted == ATTEMPTS) {
                    if (round > 0) {
                        long cpuAdded = Math.max(0, enabledCpu - disabledCpu) / ATTEMPTS;
                        long wallAdded = Math.max(0, enabledWall - disabledWall) / ATTEMPTS;
                        LogUtils.getLogger().info("Capture measurement category={} round={} attempts={} cpu_added_ns={} wall_added_ns={} disabled_allocated_bytes={}",
                            CATEGORIES.get(category), round, accepted, cpuAdded, wallAdded, disabledAllocation);
                        helper.assertTrue(cpuAdded <= 100000 && wallAdded <= 100000,
                            "Capture must add at most 0.1 milliseconds per attempt in each measured round.");
                        helper.assertTrue(disabledAllocation == 0, "Disabled capture must allocate no diagnostic objects.");
                    }
                    if (++round == 4) { round = 0; category++; }
                    accepted = 0;
                    enabledCpu = enabledWall = disabledCpu = disabledWall = disabledAllocation = 0;
                }
                if (category == CATEGORIES.size()) {
                    finish();
                    helper.succeed();
                    return;
                }
                var disabled = measure(operations[category], ATTEMPTS);
                disabledCpu += disabled[0];
                disabledWall += disabled[1];
                disabledAllocation += measureAllocation(operations[category], ATTEMPTS);
                var started = InteractionCaptureManager.start(helper.getLevel().getServer(), player, captureCategory());
                helper.assertTrue(started.started(), "Every window must start a real active capture.");
                pendingOutput = started.status().output();
                var enabled = measure(operations[category], ATTEMPTS);
                enabledCpu += enabled[0];
                enabledWall += enabled[1];
                var status = InteractionCaptureManager.status();
                helper.assertTrue(status.records() == WINDOW && !status.active() && status.stopReason().equals("rate_limit"),
                    "The first twenty attempts must be accepted before the capture rate limit stops the window.");
                accepted += ATTEMPTS;
                helper.runAfterDelay(1, this::step);
            } catch (Throwable error) {
                failure = error;
                InteractionCaptureManager.stop(InteractionCaptureManager.StopReason.MANUAL);
                helper.runAfterDelay(1, this::finishFailure);
            }
        }

        private long[] measure(Runnable operation, int attempts) {
            long cpu = threads.getCurrentThreadCpuTime();
            long wall = System.nanoTime();
            for (int i = 0; i < attempts; i++) operation.run();
            wall = System.nanoTime() - wall;
            cpu = threads.getCurrentThreadCpuTime() - cpu;
            return new long[] {cpu, wall};
        }

        private long measureAllocation(Runnable operation, int attempts) {
            long before = threads.getThreadAllocatedBytes(threadId);
            for (int index = 0; index < attempts; index++) operation.run();
            return threads.getThreadAllocatedBytes(threadId) - before;
        }

        private String captureCategory() {
            return CATEGORIES.get(category).equals("commands") ? "permissions" : CATEGORIES.get(category);
        }

        private void finishFailure() {
            var status = InteractionCaptureManager.status();
            if (status.outputState().equals("writing") || status.outputState().equals("draining")) {
                helper.runAfterDelay(1, this::finishFailure);
                return;
            }
            try {
                if (pendingOutput != null) Files.deleteIfExists(pendingOutput);
            } catch (java.io.IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            } finally {
                finish();
            }
            helper.fail("Capture measurement failed. " + failure);
        }

        private void finish() {
            players.remove(player.getUUID(), player);
            player.discard();
            InteractionCaptureManager.resetRuntimeState();
        }
    }
}
