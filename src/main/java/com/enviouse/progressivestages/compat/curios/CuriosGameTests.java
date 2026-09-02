package com.enviouse.progressivestages.compat.curios;

import com.mojang.authlib.GameProfile;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.data.TeamStageData;
import com.enviouse.progressivestages.common.lock.LockDefinition;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side Curios regression coverage. The fixture uses the real Curios API
 * from the runtime artifact and a configured {@code ring} slot.
 */
@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class CuriosGameTests {

    private static final StageId RING_STAGE = StageId.parse("progressivestages:gametest_ring_gate");

    private CuriosGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void lockedRingContentsAreConservedAcrossStageTransitions(GameTestHelper helper) {
        if (!ModList.get().isLoaded("curios")) {
            helper.succeed();
            return;
        }
        UUID playerId = UUID.randomUUID();
        TestServerPlayer player = detachedPlayer(helper, playerId);
        StageDefinition definition = StageDefinition.builder(RING_STAGE)
            .locks(new LockDefinition.Builder().curioLockedSlots(List.of("ring")).build())
            .build();

        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        order.registerStage(definition);
        registry.registerStage(definition);

        try {
            CuriosApiAccess access = CuriosApiAccess.resolve(CuriosGameTests.class.getClassLoader());
            Object stacks = ringStacks(access, player);

            access.setStackInSlot(stacks, 0, new ItemStack(Items.DIAMOND, 3));
            CuriosCompat.scanAndEject(player);
            assertEmpty(access, stacks, "missing stage must eject the ring contents");
            helper.assertTrue(count(player, Items.DIAMOND) == 3,
                "ejection must retain every diamond in the player inventory");

            CuriosCompat.scanAndEject(player);
            helper.assertTrue(count(player, Items.DIAMOND) == 3,
                "repeating an ejection sweep must not duplicate items");

            stages.grantStage(teamId, RING_STAGE);
            helper.assertTrue(StageManager.getInstance().hasStage(player, RING_STAGE),
                "the fixture stage must grant");
            access.setStackInSlot(stacks, 0, new ItemStack(Items.IRON_INGOT, 5));
            CuriosCompat.scanAndEject(player);
            helper.assertTrue(access.getStackInSlot(stacks, 0).is(Items.IRON_INGOT),
                "owned stages must retain ring contents");

            registry.clear();
            registry.registerStage(definition);
            CuriosCompat.scanAndEject(player);
            helper.assertTrue(access.getStackInSlot(stacks, 0).is(Items.IRON_INGOT),
                "rule reload must retain contents for an owned stage");

            TestServerPlayer reconnectedPlayer = detachedPlayer(helper, playerId);
            helper.assertTrue(StageManager.getInstance().hasStage(reconnectedPlayer, RING_STAGE),
                "the owned stage must remain available after reconnect");
            Object reconnectedStacks = ringStacks(access, reconnectedPlayer);
            access.setStackInSlot(reconnectedStacks, 0, new ItemStack(Items.EMERALD, 4));
            CuriosCompat.scanAndEject(reconnectedPlayer);
            helper.assertTrue(access.getStackInSlot(reconnectedStacks, 0).is(Items.EMERALD),
                "reconnected players must retain contents in an owned ring slot");

            stages.revokeStage(teamId, RING_STAGE);
            CuriosCompat.scanAndEject(player);
            assertEmpty(access, stacks, "revoking the stage must eject the ring contents");
            helper.assertTrue(count(player, Items.IRON_INGOT) == 5,
                "revoke ejection must retain every iron ingot");

            fillInventory(player);
            access.setStackInSlot(stacks, 0, new ItemStack(Items.GOLD_INGOT, 2));
            CuriosCompat.scanAndEject(player);
            assertEmpty(access, stacks, "a full inventory must still clear the locked slot");
            helper.assertTrue(player.droppedItemCount(Items.GOLD_INGOT) == 2,
                "a full inventory must hand every item to the player drop path without loss");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Curios slot transition failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    private static Object ringStacks(CuriosApiAccess access, ServerPlayer player) throws Exception {
        Object inventory = unwrap(access.getCuriosInventory(player));
        if (inventory == null) throw new IllegalStateException("Curios inventory was unavailable");
        Object value = access.getCurios(inventory);
        if (!(value instanceof Map<?, ?> curios)) throw new IllegalStateException("Curios slot map was unavailable");
        Object handler = curios.get("ring");
        if (handler == null) throw new IllegalStateException("configured ring slot was unavailable");
        Object stacks = access.getStacks(handler);
        if (stacks == null || access.getSlots(handler) < 1) {
            throw new IllegalStateException("configured ring slot had no storage");
        }
        return stacks;
    }

    private static TestServerPlayer detachedPlayer(GameTestHelper helper, UUID playerId) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
            new GameProfile(playerId, "curios-gametest"), false);
        return new TestServerPlayer(helper, cookie);
    }

    private static Object unwrap(Object value) {
        if (value instanceof Optional<?> optional) return optional.orElse(null);
        return value;
    }

    private static void assertEmpty(CuriosApiAccess access, Object stacks, String message) throws Exception {
        if (!access.getStackInSlot(stacks, 0).isEmpty()) throw new IllegalStateException(message);
    }

    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static void fillInventory(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
    }

    private static final class TestServerPlayer extends ServerPlayer {
        private final List<ItemStack> droppedItems = new java.util.ArrayList<>();

        private TestServerPlayer(GameTestHelper helper, CommonListenerCookie cookie) {
            super(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }

        @Override
        public net.minecraft.world.entity.item.ItemEntity drop(ItemStack stack, boolean throwRandomly) {
            droppedItems.add(stack.copy());
            return super.drop(stack, throwRandomly);
        }

        private int droppedItemCount(net.minecraft.world.item.Item item) {
            return droppedItems.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
        }
    }
}
