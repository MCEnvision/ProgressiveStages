package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.server.triggers.StageRegressionData;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@GameTestHolder("progressivestages")
public final class LuckPermsNativeGameTests {
    private LuckPermsNativeGameTests() {}

    @GameTestGenerator
    public static List<TestFunction> nativeProviderTransactions() {
        if (!Boolean.getBoolean("progressivestages.luckPermsGameTests")) return List.of();
        if (!ModList.get().isLoaded("luckperms")) {
            throw new IllegalStateException("Native permission GameTests require the actual LuckPerms artifact.");
        }
        return List.of(new TestFunction("luckperms_native", "luckperms_native_publication_and_context",
            "minecraft:igloo/top", 120, 0, true, helper -> new Fixture(helper).start()));
    }

    private static final class Fixture {
        private final GameTestHelper helper;
        private final net.luckperms.api.LuckPerms api = net.luckperms.api.LuckPermsProvider.get();
        private final StageManager manager = StageManager.getInstance();
        private final StageOrder order = StageOrder.getInstance();
        private final List<StageDefinition> definitions;
        private final com.enviouse.progressivestages.common.data.TeamStageData original;
        private final List<ServerPlayer> online;
        private final Map<UUID, ServerPlayer> byId;
        private final List<FakePlayer> players = new ArrayList<>();
        private final List<net.luckperms.api.model.user.User> users = new ArrayList<>();
        private final List<io.netty.channel.embedded.EmbeddedChannel> channels = new ArrayList<>();
        private final Map<OwnerRef, Map<StageId, Long>> clocks = new HashMap<>();
        private final StageId personal = StageId.parse("progressivestages:native_personal");
        private final StageId contextual = StageId.parse("progressivestages:native_contextual");
        private boolean closed;

        @SuppressWarnings("unchecked")
        Fixture(GameTestHelper helper) {
            this.helper = helper;
            var server = helper.getLevel().getServer();
            helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "Native provider fixtures require an isolated server.");
            definitions = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
            original = server.overworld().getData(StageAttachments.TEAM_STAGES);
            try {
                var listField = PlayerList.class.getDeclaredField("players");
                listField.setAccessible(true);
                online = (List<ServerPlayer>) listField.get(server.getPlayerList());
                var indexField = PlayerList.class.getDeclaredField("playersByUUID");
                indexField.setAccessible(true);
                byId = (Map<UUID, ServerPlayer>) indexField.get(server.getPlayerList());
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Cannot bind the native provider fixtures", failure);
            }
        }

        void start() {
            check(() -> {
                var server = helper.getLevel().getServer();
                server.overworld().setData(StageAttachments.TEAM_STAGES, original.copy());
                order.clear();
                order.registerStage(definition(personal, "fixture.profession", List.of()));
                order.registerStage(definition(contextual, "fixture.context", List.of(new LuckPermsStageOptions.InboundRule(
                    "survival", List.of(), List.of("fixture.input"), LuckPermsStageOptions.Match.ALL,
                    Map.of("gamemode", List.of("survival"))))));
                LuckPermsBridge.getInstance().setAdapterForTests(null);
                for (int index = 1; index <= 2; index++) {
                    UUID id = new UUID(0x5741, index);
                    helper.assertTrue(!byId.containsKey(id) && api.getUserManager().getUser(id) == null,
                        "Native provider fixture identities must be unused.");
                    var player = new FakePlayer(helper.getLevel(), new GameProfile(id, "native-permission" + index));
                    players.add(player);
                    var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
                    channels.add(new io.netty.channel.embedded.EmbeddedChannel(connection));
                    var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false);
                    player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player, cookie) {
                        @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                            // this provider fixture checks server state without a client.
                        }
                    };
                    online.add(player);
                    byId.put(id, player);
                    var owner = new OwnerRef(OwnerKind.PERSONAL, id);
                    var data = StageRegressionData.get(server);
                    clocks.put(owner, Map.of(personal, data.getGrantTime(owner, personal), contextual, data.getGrantTime(owner, contextual)));
                    var user = api.getUserManager().loadUser(id).join();
                    users.add(user);
                    user.transientData().add(net.luckperms.api.node.types.PermissionNode.builder("fixture.input").build());
                    player.setGameMode(GameType.SURVIVAL);
                }
                manager.grantStage(players.getFirst(), personal);
                LuckPermsBridge.reconcileAll();
                later(10, () -> {
                    requirePermission(0, "fixture.profession", true);
                    requirePermission(0, "fixture.context", true);
                    helper.assertTrue(LuckPermsBridge.capabilities().queuedSubjects() == 0,
                        "Owned context notifications must not perpetually enqueue reconciliation.");
                    manager.grantStage(players.get(1), personal);
                    requirePermission(0, "fixture.profession", true);
                    users.get(1).transientData().add(net.luckperms.api.node.types.PermissionNode.builder("fixture.unrelated").build());
                    later(10, () -> {
                        requirePermission(0, "fixture.profession", true);
                        requirePermission(1, "fixture.profession", true);
                        players.getFirst().setGameMode(GameType.CREATIVE);
                        later(10, () -> {
                            helper.assertTrue(!manager.hasStage(players.getFirst(), contextual),
                                "The real provider game mode event must withdraw synchronized context ownership.");
                            requirePermission(0, "fixture.context", false);
                            requirePermission(0, "fixture.profession", true);
                            players.getFirst().setGameMode(GameType.SURVIVAL);
                            later(10, () -> {
                                helper.assertTrue(manager.hasStage(players.getFirst(), contextual),
                                    "Returning to the matching context must restore synchronized access.");
                                requirePermission(0, "fixture.context", true);
                                requirePermission(0, "fixture.profession", true);
                                requirePermission(1, "fixture.profession", true);
                                close();
                                helper.succeed();
                            });
                        });
                    });
                });
            });
            helper.runAtTickTime(100, this::close);
        }

        private StageDefinition definition(StageId id, String permission, List<LuckPermsStageOptions.InboundRule> inbound) {
            return StageDefinition.builder(id).teamStage(false).luckPerms(new LuckPermsStageOptions(true, true,
                LuckPermsStageOptions.InboundMode.SYNCHRONIZED, inbound,
                List.of(new LuckPermsStageOptions.OutboundRule("output", LuckPermsStageOptions.OutboundKind.PERMISSION,
                    permission, Map.of())), List.of())).build();
        }

        private void requirePermission(int index, String permission, boolean allowed) {
            var player = players.get(index);
            var user = api.getUserManager().getUser(player.getUUID());
            helper.assertTrue(user == users.get(index), "Permission verification must use the already loaded native user.");
            var query = api.getContextManager().getQueryOptions(player);
            var result = user.getCachedData().getPermissionData(query).checkPermission(permission);
            helper.assertTrue((result == net.luckperms.api.util.Tristate.TRUE) == allowed,
                "Native permission " + permission + " expected " + allowed + " but was " + result + " in " + query.context());
        }

        private void later(int ticks, Runnable action) { helper.runAfterDelay(ticks, () -> check(action)); }
        private void check(Runnable action) {
            if (closed) return;
            try { action.run(); }
            catch (Throwable failure) { close(); throw failure; }
        }

        private void close() {
            if (closed) return;
            closed = true;
            players.forEach(LuckPermsBridge::disconnect);
            LuckPermsBridge.getInstance().setAdapterForTests(new InMemoryLuckPermsAdapter());
            players.forEach(player -> { online.remove(player); byId.remove(player.getUUID(), player); player.discard(); });
            users.forEach(user -> { user.transientData().clear(); api.getUserManager().cleanupUser(user); });
            channels.forEach(io.netty.channel.embedded.EmbeddedChannel::finishAndReleaseAll);
            LuckPermsBridge.getInstance().setAdapterForTests(null);
            var server = helper.getLevel().getServer();
            server.overworld().setData(StageAttachments.TEAM_STAGES, original);
            order.clear();
            definitions.forEach(order::registerStage);
            var data = StageRegressionData.get(server);
            clocks.forEach((owner, stages) -> stages.forEach((stage, clock) -> {
                if (clock <= 0) data.clear(owner, stage); else data.markGranted(owner, stage, clock);
            }));
        }
    }
}
