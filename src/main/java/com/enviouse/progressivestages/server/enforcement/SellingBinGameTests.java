package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.lock.LockDefinition;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("progressivestages")
public final class SellingBinGameTests {
    private static final ResourceLocation BIN = ResourceLocation.parse("selling_bin:selling_bin");
    private static final StageId STAGE = StageId.parse("progressivestages:gametest_selling_bin");

    private SellingBinGameTests() {}

    @GameTestGenerator
    public static List<TestFunction> sellingBinTransactions() {
        if (!Boolean.getBoolean("progressivestages.sellingBinGameTests")) return List.of();
        if (!ModList.get().isLoaded("selling_bin")) {
            throw new IllegalStateException("Selling Bin GameTests require the actual Selling Bin artifact.");
        }
        List<TestFunction> tests = new ArrayList<>();
        for (boolean automatic : List.of(false, true)) {
            String suffix = automatic ? "automatic" : "manual";
            tests.add(new TestFunction("selling_bin_armor_" + suffix, "selling_bin_armor_" + suffix,
                "minecraft:igloo/top", 200, 0, true,
                helper -> directUse(helper, "tag:c:armors", Items.IRON_CHESTPLATE, automatic)));
            tests.add(new TestFunction("selling_bin_wildcard_" + suffix, "selling_bin_wildcard_" + suffix,
                "minecraft:igloo/top", 200, 0, true,
                helper -> directUse(helper, "all:*", Items.BREAD, automatic)));
            tests.add(new TestFunction("selling_bin_selective_" + suffix, "selling_bin_selective_" + suffix,
                "minecraft:igloo/top", 200, 0, true,
                helper -> selectiveInsertion(helper, automatic)));
            tests.add(new TestFunction("selling_bin_partial_" + suffix, "selling_bin_partial_" + suffix,
                "minecraft:igloo/top", 200, 0, true,
                helper -> partialInput(helper, automatic)));
        }
        return tests;
    }

    private static void directUse(GameTestHelper helper, String selector, Item item, boolean automatic) {
        Fixture fixture = new Fixture(helper, selector, false, automatic);
        int itemValue = fixture.itemValue(item);
        fixture.check(() -> {
            fixture.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            helper.assertTrue(fixture.use(InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                    && fixture.use(InteractionHand.MAIN_HAND, fixture.part) == InteractionResult.FAIL,
                "The original held item rule must deny the real bin dispatch.");
            fixture.afterTicks(() -> {
                fixture.assertUnchanged(item, InteractionHand.MAIN_HAND, 1, 0);
                fixture.grant();
                helper.assertTrue(fixture.use(InteractionHand.MAIN_HAND, fixture.part).consumesAction()
                        && fixture.player.getMainHandItem().isEmpty(),
                    "Granting the stage must permit the actual valued item insertion.");
                fixture.sellIfManual();
                fixture.afterTicks(() -> {
                    fixture.assertSold(itemValue);
                    fixture.revoke();
                    fixture.player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(item));
                    helper.assertTrue(fixture.use(InteractionHand.OFF_HAND, fixture.part) == InteractionResult.FAIL,
                        "Revocation must deny the next actual offhand insertion.");
                    fixture.afterTicks(() -> {
                        fixture.assertUnchanged(item, InteractionHand.OFF_HAND, 1, itemValue);
                        fixture.finish();
                    });
                });
            });
        });
    }

    private static void partialInput(GameTestHelper helper, boolean automatic) {
        Fixture fixture = new Fixture(helper, "id:minecraft:bread", true, automatic);
        int breadValue = fixture.itemValue(Items.BREAD);
        fixture.check(() -> {
            fixture.bin.setItem(0, new ItemStack(Items.BREAD, 1));
            fixture.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
            helper.assertTrue(fixture.use(InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                    && fixture.use(InteractionHand.MAIN_HAND, fixture.part) == InteractionResult.FAIL
                    && fixture.player.getMainHandItem().getCount() == 3
                    && fixture.bin.getItem(0).getCount() == 1,
                "Denied merging must preserve the held stack and previously accepted deposit.");
            fixture.afterTicks(() -> {
                helper.assertTrue(fixture.player.getMainHandItem().getCount() == 3
                        && fixture.value() == (automatic ? breadValue : 0)
                        && fixture.bin.getItem(0).getCount() == (automatic ? 0 : 1),
                    "Only the previously accepted deposit may sell while the new bread remains denied.");
                fixture.sellIfManual();
                fixture.afterTicks(() -> {
                    fixture.assertSold(breadValue);
                    fixture.grant();
                    fixture.bin.setItem(0, new ItemStack(Items.BREAD, 63));
                    helper.assertTrue(fixture.use(InteractionHand.MAIN_HAND, fixture.part).consumesAction()
                            && fixture.player.getMainHandItem().getCount() == 2
                            && fixture.bin.getItem(0).getCount() == 64,
                        "An allowed partial merge must move only the one item that fits.");
                    fixture.sellIfManual();
                    fixture.afterTicks(() -> {
                        fixture.assertSold(breadValue * 65);
                        helper.assertTrue(fixture.player.getMainHandItem().getCount() == 2,
                            "Selling the accepted stack must preserve the two items that did not fit.");
                        fixture.finish();
                    });
                });
            });
        });
    }

    private static void selectiveInsertion(GameTestHelper helper, boolean automatic) {
        Fixture fixture = new Fixture(helper, "id:minecraft:bread", true, automatic);
        int breadValue = fixture.itemValue(Items.BREAD);
        int carrotValue = fixture.itemValue(Items.CARROT);
        fixture.check(() -> {
            fixture.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
            helper.assertTrue(fixture.use(InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                    && fixture.use(InteractionHand.MAIN_HAND, fixture.part) == InteractionResult.FAIL,
                "The paired bread rule must deny direct insertion.");
            fixture.menu = ((MenuProvider)fixture.entity).createMenu(1, fixture.player.getInventory(), fixture.player);
            helper.assertTrue(fixture.menu != null, "The real bin must provide its menu.");
            fixture.player.containerMenu = fixture.menu;
            int hotbar = fixture.menu.slots.stream()
                .filter(slot -> slot.container == fixture.player.getInventory() && slot.getContainerSlot() == 0)
                .findFirst().orElseThrow().index;
            fixture.menu.clicked(hotbar, 0, ClickType.QUICK_MOVE, fixture.player);
            helper.assertTrue(fixture.player.getMainHandItem().getCount() == 3 && fixture.bin.getItem(0).isEmpty(),
                "The real bin quick move must not insert locked bread.");
            fixture.menu.clicked(hotbar, 0, ClickType.PICKUP, fixture.player);
            fixture.menu.clicked(0, 0, ClickType.PICKUP, fixture.player);
            helper.assertTrue(fixture.menu.getCarried().is(Items.BREAD)
                    && fixture.menu.getCarried().getCount() == 3 && fixture.bin.getItem(0).isEmpty(),
                "The real bin normal click must retain every denied carried bread.");
            fixture.menu.clicked(hotbar, 0, ClickType.PICKUP, fixture.player);
            fixture.afterTicks(() -> {
                fixture.assertUnchanged(Items.BREAD, InteractionHand.MAIN_HAND, 3, 0);
                fixture.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
                helper.assertTrue(fixture.use(InteractionHand.MAIN_HAND, fixture.part).consumesAction()
                        && fixture.player.getMainHandItem().isEmpty(),
                    "A nonmatching valued food must remain insertable without the bread stage.");
                fixture.sellIfManual();
                fixture.afterTicks(() -> {
                    fixture.assertSold(carrotValue);
                    fixture.grant();
                    fixture.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
                    fixture.menu.clicked(hotbar, 0, ClickType.QUICK_MOVE, fixture.player);
                    helper.assertTrue(fixture.player.getMainHandItem().isEmpty(),
                        "Granting the stage must allow bread through the real bin menu.");
                    fixture.sellIfManual();
                    fixture.afterTicks(() -> {
                        fixture.assertSold(carrotValue + breadValue * 3);
                        fixture.revoke();
                        fixture.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
                        fixture.menu.clicked(hotbar, 0, ClickType.QUICK_MOVE, fixture.player);
                        fixture.afterTicks(() -> {
                            fixture.assertUnchanged(Items.BREAD, InteractionHand.MAIN_HAND, 1, carrotValue + breadValue * 3);
                            fixture.finish();
                        });
                    });
                });
            });
        });
    }

    private static final class Fixture {
        private final GameTestHelper helper;
        private final ServerPlayer player;
        private final BlockPos position;
        private final BlockPos part;
        private final BlockEntity entity;
        private final Container bin;
        private final UUID owner;
        private final boolean automatic;
        private AbstractContainerMenu menu;

        private Fixture(GameTestHelper helper, String selector, boolean paired, boolean automatic) {
            this.helper = helper;
            this.automatic = automatic;
            CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "bin-test"), false);
            player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
            var connection = new net.minecraft.network.Connection(
                net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
            new io.netty.channel.embedded.EmbeddedChannel(connection);
            player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(
                helper.getLevel().getServer(), connection, player, cookie) {
                @Override
                public void send(net.minecraft.network.protocol.Packet<?> packet) {
                    // this fixture has no client and asserts authoritative server state only.
                }
            };
            owner = TeamProvider.getInstance().getTeamId(player);
            position = helper.absolutePos(new BlockPos(3, 1, 3));
            player.setPos(Vec3.atCenterOf(position));
            for (BlockPos pos : BlockPos.betweenClosed(position.offset(-2, 0, -2), position.offset(2, 2, 2))) {
                helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            helper.getLevel().setBlockAndUpdate(position.below(), Blocks.STONE.defaultBlockState());
            Item item = BuiltInRegistries.ITEM.get(BIN);
            helper.assertTrue(item instanceof BlockItem, "The actual Selling Bin block item must be installed.");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            BlockHitResult floor = new BlockHitResult(Vec3.atCenterOf(position.below()), Direction.UP,
                position.below(), false);
            helper.assertTrue(((BlockItem)item).place(new BlockPlaceContext(player,
                    InteractionHand.MAIN_HAND, player.getMainHandItem(), floor)).consumesAction(),
                "The actual bin placement must create the multiblock fixture.");
            entity = helper.getLevel().getBlockEntity(position);
            helper.assertTrue(entity instanceof Container && entity instanceof MenuProvider,
                "The actual bin center must expose its inventory and menu.");
            bin = (Container)entity;
            List<BlockPos> parts = new ArrayList<>();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = position.relative(direction);
                if (helper.getLevel().getBlockState(candidate).is(BuiltInRegistries.BLOCK.get(BIN))) {
                    parts.add(candidate);
                }
            }
            helper.assertTrue(parts.size() == 1, "The real bin placement must create exactly one adjacent part.");
            part = parts.getFirst();
            try {
                entity.getClass().getField("instaSell").setBoolean(entity, automatic);
                entity.getClass().getField("sound").setBoolean(entity, false);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("The installed Selling Bin fixture API differs.", failure);
            }
            List<LockDefinition.InteractionLock> rules = new ArrayList<>();
            rules.add(new LockDefinition.InteractionLock("item_on_block", selector, "id:" + BIN,
                "Selling Bin direct insertion regression"));
            if (paired) rules.add(new LockDefinition.InteractionLock("item_into_inventory", selector,
                "id:" + BIN, "block", "lock", 100, "Selling Bin menu insertion regression"));
            StageDefinition definition = StageDefinition.builder(STAGE)
                .locks(LockDefinition.builder().interactions(rules).build()).build();
            StageOrder.getInstance().registerStage(definition);
            LockRegistry.getInstance().registerStage(definition);
        }

        private InteractionResult use(InteractionHand hand) {
            return use(hand, position);
        }

        private InteractionResult use(InteractionHand hand, BlockPos target) {
            return player.gameMode.useItemOn(player, helper.getLevel(), player.getItemInHand(hand), hand,
                new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false));
        }

        private void grant() {
            helper.getLevel().getData(StageAttachments.TEAM_STAGES).grantStage(owner, STAGE);
        }

        private void revoke() {
            helper.getLevel().getData(StageAttachments.TEAM_STAGES).revokeStage(owner, STAGE);
        }

        private int itemValue(Item item) {
            try {
                Class<?> currency = Class.forName("com.wdiscute.sellingbin.bin.Currency");
                return (Integer)currency.getMethod("calculateValueFromSingleStack", ItemStack.class,
                        BlockEntity.class).invoke(null, new ItemStack(item), entity);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Cannot inspect the installed Selling Bin item value.", failure);
            }
        }

        private int value() {
            try {
                return (Integer)entity.getClass().getMethod("getProgressAvailable").invoke(entity);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Cannot inspect authoritative bin currency.", failure);
            }
        }

        private void assertUnchanged(Item item, InteractionHand hand, int expectedCount, int expectedValue) {
            helper.assertTrue(player.getItemInHand(hand).is(item)
                    && player.getItemInHand(hand).getCount() == expectedCount && bin.getItem(0).isEmpty()
                    && value() == expectedValue,
                "Denied insertion must preserve the held item, empty bin input and authoritative currency after ticks.");
        }

        private void assertSold(int expectedValue) {
            helper.assertTrue(bin.getItem(0).isEmpty() && value() == expectedValue,
                "The actual sale must consume only allowed deposits and produce the configured currency value.");
        }

        private void sellIfManual() {
            if (automatic) return;
            AbstractContainerMenu saleMenu = ((MenuProvider)entity).createMenu(2, player.getInventory(), player);
            try {
                saleMenu.clickMenuButton(player, 68);
            } finally {
                saleMenu.removed(player);
            }
        }

        private void afterTicks(Runnable action) {
            helper.runAfterDelay(20, () -> check(action));
        }

        private void check(Runnable action) {
            try {
                action.run();
            } catch (Throwable failure) {
                cleanup();
                throw failure;
            }
        }

        private void finish() {
            cleanup();
            helper.succeed();
        }

        private void cleanup() {
            revoke();
            if (menu != null) menu.removed(player);
            LockRegistry.getInstance().clear();
            StageOrder.getInstance().clear();
        }
    }
}
