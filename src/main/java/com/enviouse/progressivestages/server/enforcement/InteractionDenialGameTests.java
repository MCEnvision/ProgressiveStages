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
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class InteractionDenialGameTests {
    private InteractionDenialGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void deniedBlockUseCorrectsPredictionBeforeGrantAndRevoke(GameTestHelper helper) {
        StageId stage = StageId.parse("progressivestages:gametest_interaction_denial");
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "interaction-test"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            cookie.gameProfile(), cookie.clientInformation());
        InventoryCorrection correction = new InventoryCorrection();
        player.inventoryMenu.setSynchronizer(correction);
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(position), Direction.UP, position, false);
        helper.getLevel().setBlockAndUpdate(position, Blocks.CAULDRON.defaultBlockState());
        StageDefinition definition = StageDefinition.builder(stage).locks(LockDefinition.builder()
            .interactions(List.of(new LockDefinition.InteractionLock("item_on_block", "all:*",
                "id:minecraft:cauldron", "Interaction denial regression"))).build()).build();
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        var stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID owner = TeamProvider.getInstance().getTeamId(player);
        order.registerStage(definition);
        registry.registerStage(definition);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            player.inventoryMenu.sendAllDataToRemote();
            int initialCorrections = correction.fullUpdates;
            InteractionResult denied = player.gameMode.useItemOn(player, helper.getLevel(),
                player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(helper.getLevel().getBlockState(position).is(Blocks.CAULDRON),
                "Denied block use must not fill the cauldron.");
            helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET),
                "Denied block use must retain the authoritative bucket.");
            helper.assertTrue(correction.fullUpdates > initialCorrections,
                "Denied block use must resend unchanged inventory state to correct prediction.");
            helper.assertTrue(denied == InteractionResult.FAIL,
                "Denied block use must return a terminal failure rather than permit fallback.");

            stages.grantStage(owner, stage);
            InteractionResult allowed = player.gameMode.useItemOn(player, helper.getLevel(),
                player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(allowed.consumesAction()
                    && helper.getLevel().getBlockState(position).is(Blocks.WATER_CAULDRON)
                    && player.getMainHandItem().is(Items.BUCKET),
                "Granting the stage must permit the real block use and bucket exchange.");

            stages.revokeStage(owner, stage);
            helper.getLevel().setBlockAndUpdate(position, Blocks.CAULDRON.defaultBlockState());
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.WATER_BUCKET));
            InteractionResult revoked = player.gameMode.useItemOn(player, helper.getLevel(),
                player.getOffhandItem(), InteractionHand.OFF_HAND, hit);
            helper.assertTrue(revoked == InteractionResult.FAIL
                    && player.getOffhandItem().is(Items.WATER_BUCKET)
                    && helper.getLevel().getBlockState(position).is(Blocks.CAULDRON),
                "Revoking the stage must deny the next offhand attempt without mutation.");
            helper.succeed();
        } finally {
            stages.revokeStage(owner, stage);
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void deniedWholeBlockRuleBlocksEmptyHandMenuOpen(GameTestHelper helper) {
        StageId stage = StageId.parse("progressivestages:gametest_empty_menu_denial");
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "empty-menu-test"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(
            net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(
            helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };

        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(position, Blocks.CHEST.defaultBlockState());
        StageDefinition definition = StageDefinition.builder(stage).locks(LockDefinition.builder()
            .interactions(List.of(new LockDefinition.InteractionLock("item_on_block", "all:*",
                "id:minecraft:chest", "Empty hand menu denial regression"))).build()).build();
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        var stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID owner = TeamProvider.getInstance().getTeamId(player);
        order.registerStage(definition);
        registry.registerStage(definition);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(position), Direction.UP, position, false);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            InteractionResult denied = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY,
                InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(denied == InteractionResult.FAIL && player.containerMenu == player.inventoryMenu,
                "A whole block interaction lock must deny an empty hand menu open.");

            stages.grantStage(owner, stage);
            InteractionResult allowed = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY,
                InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(allowed.consumesAction() && player.containerMenu != player.inventoryMenu,
                "Granting the stage must permit the empty hand menu open.");
            player.closeContainer();

            stages.revokeStage(owner, stage);
            InteractionResult revoked = player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY,
                InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(revoked == InteractionResult.FAIL && player.containerMenu == player.inventoryMenu,
                "Revocation must deny a fresh empty hand menu open.");
            helper.succeed();
        } finally {
            player.closeContainer();
            stages.revokeStage(owner, stage);
            registry.clear();
            order.clear();
        }
    }

    private static final class InventoryCorrection implements ContainerSynchronizer {
        private int fullUpdates;

        @Override
        public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> items,
                                    ItemStack carried, int[] data) {
            fullUpdates++;
        }

        @Override
        public void sendSlotChange(AbstractContainerMenu menu, int slot, ItemStack stack) {}

        @Override
        public void sendCarriedChange(AbstractContainerMenu menu, ItemStack stack) {}

        @Override
        public void sendDataChange(AbstractContainerMenu menu, int id, int value) {}
    }
}
