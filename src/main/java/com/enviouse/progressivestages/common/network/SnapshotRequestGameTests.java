package com.enviouse.progressivestages.common.network;

import com.enviouse.progressivestages.common.rehaul.CompiledSnapshot;
import com.enviouse.progressivestages.common.rehaul.client.ClientSnapshotCodec;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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
public final class SnapshotRequestGameTests {
    private SnapshotRequestGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 400)
    public static void snapshotRequestBurstsDoNotRebuildEveryResponse(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var history = state("clientSnapshotHistory");
        var originalHistory = Map.copyOf(history);
        var handler = handler();
        var context = context(player);
        var payload = new NetworkHandler.ClientSnapshotRequestPayload(0);
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        boolean wasEnabled = bean.isThreadAllocatedMemoryEnabled();
        try {
            for (int index = 0; index < 128; index++) handler.invoke(null, payload, context);
            Object offer = state("offeredClientSnapshots").get(player.getUUID());
            helper.assertTrue(offer != null, "The first recovery request must receive a snapshot.");
            bean.setThreadAllocatedMemoryEnabled(true);
            long thread = Thread.currentThread().threadId();
            long before = bean.getThreadAllocatedBytes(thread);
            long started = System.nanoTime();
            for (int index = 0; index < 1000; index++) handler.invoke(null, payload, context);
            long elapsed = System.nanoTime() - started;
            long allocated = bean.getThreadAllocatedBytes(thread) - before;
            com.mojang.logging.LogUtils.getLogger().info(
                "Snapshot request burst. Requests 1000, elapsed nanoseconds {}, allocated bytes {}",
                elapsed, allocated);
            helper.assertTrue(allocated >= 0 && allocated < 1024 * 1024,
                "Repeated recovery requests must not reconstruct every snapshot response.");
            helper.assertTrue(state("offeredClientSnapshots").get(player.getUUID()) == offer,
                "A request burst must keep one immediate offer and defer redundant responses.");
            helper.succeed();
        } finally {
            bean.setThreadAllocatedMemoryEnabled(wasEnabled);
            history.clear();
            history.putAll(originalHistory);
            NetworkHandler.clearPlayerRuntimeState(player.getUUID());
            player.discard();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", timeoutTicks = 100, batch = "progressivestages_snapshot_pending")
    public static void pendingSnapshotRecoveryUsesCurrentStateOnServerTicks(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var other = player(helper);
        var history = state("clientSnapshotHistory");
        var originalHistory = Map.copyOf(history);
        var loader = StageFileLoader.getInstance();
        var original = loader.getCompiledSnapshot();
        var snapshotField = StageFileLoader.class.getDeclaredField("compiledSnapshot");
        snapshotField.setAccessible(true);
        var playersField = PlayerList.class.getDeclaredField("playersByUUID");
        playersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var players = (Map<UUID, ServerPlayer>) playersField.get(helper.getLevel().getServer().getPlayerList());
        var handler = handler();
        Runnable cleanup = () -> {
            try { snapshotField.set(loader, original); }
            catch (IllegalAccessException error) { throw new IllegalStateException(error); }
            finally {
                players.remove(player.getUUID(), player);
                players.remove(other.getUUID(), other);
                history.clear();
                history.putAll(originalHistory);
                NetworkHandler.clearPlayerRuntimeState(player.getUUID());
                NetworkHandler.clearPlayerRuntimeState(other.getUUID());
                player.discard();
                other.discard();
            }
        };
        try {
            players.put(player.getUUID(), player);
            players.put(other.getUUID(), other);
            handler.invoke(null, new NetworkHandler.ClientSnapshotRequestPayload(0), context(player));
            handler.invoke(null, new NetworkHandler.ClientSnapshotRequestPayload(0), context(other));
            Object first = state("offeredClientSnapshots").get(player.getUUID());
            Object otherFirst = state("offeredClientSnapshots").get(other.getUUID());
            helper.assertTrue(first != null && otherFirst != null,
                "Each player must receive an independent immediate recovery response.");
            var acknowledge = NetworkHandler.class.getDeclaredMethod("handleClientSnapshotAck",
                NetworkHandler.ClientSnapshotAckPayload.class, IPayloadContext.class);
            acknowledge.setAccessible(true);
            acknowledge.invoke(null, new NetworkHandler.ClientSnapshotAckPayload(original.revision(),
                ClientSnapshotCodec.checksum(ClientSnapshotCodec.encode(original,
                    com.enviouse.progressivestages.common.config.StageConfig.isBlockInteractions()))), context(player));
            helper.assertTrue(state("acknowledgedClientSnapshots").containsKey(player.getUUID()),
                "The fixture must establish a valid acknowledged base before full recovery.");
            handler.invoke(null, new NetworkHandler.ClientSnapshotRequestPayload(original.revision()), context(player));
            handler.invoke(null, new NetworkHandler.ClientSnapshotRequestPayload(0), context(player));
            handler.invoke(null, new NetworkHandler.ClientSnapshotRequestPayload(original.revision()), context(player));
            handler.invoke(null, new NetworkHandler.ClientSnapshotRequestPayload(0), context(other));
            NetworkHandler.sendCompiledSnapshot(other);
            helper.assertTrue(state("offeredClientSnapshots").get(other.getUUID()) != otherFirst,
                "Normal server synchronization must bypass the request cooldown.");
            NetworkHandler.clearPlayerRuntimeState(other.getUUID());
            players.remove(other.getUUID(), other);
            helper.assertTrue(state("offeredClientSnapshots").get(player.getUUID()) == first,
                "A queued recovery must not send again in the same server tick.");
            var next = CompiledSnapshot.create(original.revision() + 1, original.stages());
            snapshotField.set(loader, next);
            helper.runAfterDelay(25, () -> {
                try {
                    Object offered = state("offeredClientSnapshots").get(player.getUUID());
                    helper.assertTrue(offered != null && offered != first,
                        "Normal server ticks must eventually deliver the pending recovery.");
                    Method revision = offered.getClass().getDeclaredMethod("revision");
                    revision.setAccessible(true);
                    helper.assertTrue((long) revision.invoke(offered) == next.revision(),
                        "Deferred recovery must use the current compiled revision.");
                    helper.assertTrue(!state("offeredClientSnapshots").containsKey(other.getUUID()),
                        "Disconnect cleanup must cancel the other player's queued response.");
                    helper.assertTrue(!state("acknowledgedClientSnapshots").containsKey(player.getUUID()),
                        "A full recovery request cannot establish an acknowledged base.");
                    helper.succeed();
                } catch (ReflectiveOperationException error) {
                    throw new IllegalStateException(error);
                } finally { cleanup.run(); }
            });
        } catch (Exception | AssertionError error) {
            cleanup.run();
            throw error;
        }
    }

    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "snapshot-request")) {
            @Override public boolean hasPermissions(int level) { return level <= 0; }
        };
    }

    private static Method handler() throws ReflectiveOperationException {
        Method method = NetworkHandler.class.getDeclaredMethod("handleClientSnapshotRequest",
            NetworkHandler.ClientSnapshotRequestPayload.class, IPayloadContext.class);
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
