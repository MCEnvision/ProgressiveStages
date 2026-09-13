package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.stage.StageOrder;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class LuckPermsQueueGameTests {
    private LuckPermsQueueGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    @SuppressWarnings("unchecked")
    public static void overflowingPermissionEventsReachEveryOnlineSubject(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        var playerList = server.getPlayerList();
        helper.assertTrue(playerList.getPlayers().isEmpty(), "The queue fixture requires an isolated server.");
        var playersField = PlayerList.class.getDeclaredField("players");
        playersField.setAccessible(true);
        var players = (List<ServerPlayer>) playersField.get(playerList);
        var indexField = PlayerList.class.getDeclaredField("playersByUUID");
        indexField.setAccessible(true);
        var byId = (Map<UUID, ServerPlayer>) indexField.get(playerList);
        var bridge = LuckPermsBridge.getInstance();
        var dirtyField = LuckPermsBridge.class.getDeclaredField("dirty");
        dirtyField.setAccessible(true);
        var queue = (SubjectReconciliationQueue) dirtyField.get(bridge);
        helper.assertTrue(queue.size() == 0 && !queue.hasRescan(), "No unrelated reconciliation may be pending.");
        var markDirty = LuckPermsBridge.class.getDeclaredMethod("markDirty", UUID.class);
        markDirty.setAccessible(true);
        var order = StageOrder.getInstance();
        var previous = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        var fixturePlayers = new ArrayList<ServerPlayer>();
        var adapter = new CountingAdapter();
        bridge.setAdapterForTests(adapter);
        order.clear();
        try {
            for (int i = 1; i <= 300; i++) {
                var profile = new GameProfile(new UUID(0x5719L, i), "queue" + i);
                helper.assertTrue(!byId.containsKey(profile.getId()), "Fixture identities must be unused.");
                var cookie = CommonListenerCookie.createInitial(profile, false);
                var player = new ServerPlayer(server, helper.getLevel(), profile, cookie.clientInformation());
                fixturePlayers.add(player);
                players.add(player);
                byId.put(profile.getId(), player);
                markDirty.invoke(bridge, profile.getId());
            }
            helper.assertTrue(queue.size() == 256 && queue.hasRescan(), "Overflow must retain the bounded queue and request a scan.");
            for (int tick = 0; tick < 64; tick++) {
                int before = adapter.queries;
                LuckPermsBridge.tick(server);
                helper.assertTrue(adapter.queries - before <= 16, "A bridge tick may reconcile at most sixteen subjects.");
                helper.assertTrue(queue.size() <= 256, "The dirty queue must remain bounded during a scan.");
            }
            helper.assertTrue(adapter.visited.size() == 300, "Every queued and overflow subject must reach reconciliation.");
            helper.assertTrue(queue.size() == 0 && !queue.hasRescan(), "The completed scan must leave no pending work.");
            helper.succeed();
        } finally {
            for (var player : fixturePlayers) {
                players.remove(player);
                byId.remove(player.getUUID(), player);
                player.discard();
            }
            queue.clear();
            bridge.setAdapterForTests(null);
            order.clear();
            previous.forEach(order::registerStage);
        }
    }

    private static final class CountingAdapter implements LuckPermsAdapter {
        private final Set<UUID> visited = new HashSet<>();
        private int queries;
        @Override public State state() { return State.READY; }
        @Override public SubjectSnapshot snapshot(UUID subject) {
            queries++;
            visited.add(subject);
            return SubjectSnapshot.unavailable();
        }
        @Override public boolean groupExists(String group) { return false; }
        @Override public MutationResult addTransient(UUID subject, NodeKind kind, String value,
                                                      Map<String, String> contexts, String owner) {
            throw new AssertionError("No stage output is configured in the queue fixture");
        }
        @Override public MutationResult removeTransient(UUID subject, NodeKind kind, String value,
                                                         Map<String, String> contexts, String owner) {
            throw new AssertionError("No stage output is configured in the queue fixture");
        }
    }
}
