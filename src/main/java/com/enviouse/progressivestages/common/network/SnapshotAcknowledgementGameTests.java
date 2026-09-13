package com.enviouse.progressivestages.common.network;

import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.rehaul.CompiledSnapshot;
import com.enviouse.progressivestages.common.rehaul.client.ClientSnapshotCodec;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class SnapshotAcknowledgementGameTests {
    private SnapshotAcknowledgementGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void snapshotAcknowledgementsRequireAnOfferedRevision(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var other = player(helper);
        var acknowledgements = state("acknowledgedClientSnapshots");
        var history = state("clientSnapshotHistory");
        var originalHistory = Map.copyOf(history);
        var loader = StageFileLoader.getInstance();
        var snapshot = loader.getCompiledSnapshot();
        var snapshotField = StageFileLoader.class.getDeclaredField("compiledSnapshot");
        snapshotField.setAccessible(true);
        var policyField = StageConfig.class.getDeclaredField("blockInteractions");
        policyField.setAccessible(true);
        boolean policy = StageConfig.isBlockInteractions();
        var handler = handler();
        var context = context(player);
        var payload = acknowledgement(snapshot);
        try {
            handler.invoke(null, payload, context);
            helper.assertTrue(!acknowledgements.containsKey(player.getUUID()),
                "An unsolicited acknowledgement must not establish a snapshot base.");
            NetworkHandler.sendCompiledSnapshot(player);
            handler.invoke(null, new NetworkHandler.ClientSnapshotAckPayload(snapshot.revision(), "forged"), context);
            helper.assertTrue(!acknowledgements.containsKey(player.getUUID()),
                "A forged checksum must not acknowledge an offered snapshot.");
            handler.invoke(null, payload, context(other));
            helper.assertTrue(!acknowledgements.containsKey(other.getUUID()),
                "A different player cannot acknowledge another player's offer.");
            policyField.setBoolean(null, !policy);
            handler.invoke(null, payload, context);
            helper.assertTrue(!acknowledgements.containsKey(player.getUUID()),
                "An enforcement policy change must invalidate the old offer even at the same revision.");
            policyField.setBoolean(null, policy);
            handler.invoke(null, payload, context);
            Object accepted = acknowledgements.get(player.getUUID());
            helper.assertTrue(accepted != null, "The exact offered revision and checksum must be accepted.");

            var next = CompiledSnapshot.create(snapshot.revision() + 1, snapshot.stages());
            snapshotField.set(loader, next);
            handler.invoke(null, acknowledgement(next), context);
            helper.assertTrue(acknowledgements.get(player.getUUID()) == accepted,
                "A newly compiled revision must be sent before it can be acknowledged.");
            NetworkHandler.sendCompiledSnapshot(player);
            handler.invoke(null, payload, context);
            helper.assertTrue(acknowledgements.get(player.getUUID()) == accepted,
                "An old acknowledgement must not replace the current offer.");
            handler.invoke(null, acknowledgement(next), context);
            helper.assertTrue(acknowledgements.get(player.getUUID()) != accepted,
                "The next offered revision must become the new acknowledged base.");
            NetworkHandler.clearPlayerRuntimeState(player.getUUID());
            handler.invoke(null, acknowledgement(next), context);
            helper.assertTrue(!acknowledgements.containsKey(player.getUUID()),
                "Disconnect cleanup must invalidate the player's outstanding offer.");
            helper.succeed();
        } finally {
            policyField.setBoolean(null, policy);
            snapshotField.set(loader, snapshot);
            history.clear();
            history.putAll(originalHistory);
            NetworkHandler.clearPlayerRuntimeState(player.getUUID());
            NetworkHandler.clearPlayerRuntimeState(other.getUUID());
            player.discard();
            other.discard();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 400)
    public static void snapshotAcknowledgementBurstsAvoidSnapshotReconstruction(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var history = state("clientSnapshotHistory");
        var originalHistory = Map.copyOf(history);
        var handler = handler();
        var context = context(player);
        var payload = acknowledgement(StageFileLoader.getInstance().getCompiledSnapshot());
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        boolean wasEnabled = bean.isThreadAllocatedMemoryEnabled();
        try {
            NetworkHandler.sendCompiledSnapshot(player);
            for (int index = 0; index < 128; index++) handler.invoke(null, payload, context);
            bean.setThreadAllocatedMemoryEnabled(true);
            long thread = Thread.currentThread().threadId();
            long before = bean.getThreadAllocatedBytes(thread);
            long started = System.nanoTime();
            for (int index = 0; index < 1000; index++) handler.invoke(null, payload, context);
            long elapsed = System.nanoTime() - started;
            long allocated = bean.getThreadAllocatedBytes(thread) - before;
            com.mojang.logging.LogUtils.getLogger().info(
                "Snapshot acknowledgement burst. Requests 1000, elapsed nanoseconds {}, allocated bytes {}",
                elapsed, allocated);
            helper.assertTrue(allocated >= 0 && allocated < 1024 * 1024,
                "Repeated acknowledgements must not rebuild or compress the compiled snapshot.");
            helper.assertTrue(state("acknowledgedClientSnapshots").containsKey(player.getUUID()),
                "Bounded acknowledgement processing must retain the valid base.");
            helper.succeed();
        } finally {
            bean.setThreadAllocatedMemoryEnabled(wasEnabled);
            history.clear();
            history.putAll(originalHistory);
            NetworkHandler.clearPlayerRuntimeState(player.getUUID());
            player.discard();
        }
    }

    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "snapshot-test")) {
            @Override public boolean hasPermissions(int level) { return level <= 0; }
        };
    }

    private static NetworkHandler.ClientSnapshotAckPayload acknowledgement(CompiledSnapshot snapshot) {
        String checksum = ClientSnapshotCodec.checksum(ClientSnapshotCodec.encode(snapshot, StageConfig.isBlockInteractions()));
        return new NetworkHandler.ClientSnapshotAckPayload(snapshot.revision(), checksum);
    }

    private static Method handler() throws ReflectiveOperationException {
        Method method = NetworkHandler.class.getDeclaredMethod("handleClientSnapshotAck",
            NetworkHandler.ClientSnapshotAckPayload.class, IPayloadContext.class);
        method.setAccessible(true);
        return method;
    }

    private static IPayloadContext context(ServerPlayer player) {
        return (IPayloadContext) Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),
            new Class<?>[] {IPayloadContext.class}, (proxy, method, arguments) -> switch (method.getName()) {
                case "player" -> player;
                case "enqueueWork" -> {
                    ((Runnable) arguments[0]).run();
                    yield CompletableFuture.completedFuture(null);
                }
                default -> throw new UnsupportedOperationException(method.getName());
            });
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> state(String name) throws ReflectiveOperationException {
        var field = NetworkHandler.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<Object, Object>) field.get(null);
    }
}
