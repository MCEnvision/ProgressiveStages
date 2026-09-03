package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.data.TeamStageData;
import com.enviouse.progressivestages.common.lock.CategoryLocks;
import com.enviouse.progressivestages.common.lock.LockDefinition;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class InventoryInsertionGameTests {
    private static final StageId STAGE = StageId.parse("progressivestages:gametest_inventory_gate");

    private InventoryInsertionGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void menuInsertionDenialPreservesTheAuthoritativeStacks(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(1, player.getInventory(), destination);
        StageDefinition definition = StageDefinition.builder(STAGE).locks(LockDefinition.builder()
            .interactions(List.of(new LockDefinition.InteractionLock("item_into_inventory",
                "id:minecraft:diamond", "id:minecraft:generic_9x3", "menu", "lock", 100,
                "GameTest inventory insertion lock")))
            .build()).build();
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.PICKUP, player);
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND), "the first click must pick up the source stack");
            int deniedStateId = menu.getStateId();
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).isEmpty(), "a denied insertion must not change the destination");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND), "a denied insertion must retain the carried stack");
            helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "the source slot must remain empty while carried");
            helper.assertTrue(menu.getStateId() == deniedStateId,
                "a denied insertion must not advance the authoritative menu state");

            stages.grantStage(teamId, STAGE);
            helper.assertTrue(StageManager.getInstance().hasStage(player, STAGE), "the fixture stage must grant");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND), "an eligible player must insert the same stack");
            helper.assertTrue(menu.getCarried().isEmpty(), "the accepted insertion must clear the carried stack");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Inventory insertion transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void pickupSwapDenialPreservesBothStacks(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(12, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        destination.setItem(0, new ItemStack(Items.DIRT, 2));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.PICKUP, player);
            int deniedStateId = menu.getStateId();
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIRT) && destination.getItem(0).getCount() == 2,
                "a denied pickup swap must retain the destination stack");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 3,
                "a denied pickup swap must retain the carried source stack");
            helper.assertTrue(menu.getStateId() == deniedStateId,
                "a denied pickup swap must not advance the authoritative menu state");

            stages.grantStage(teamId, STAGE);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND) && destination.getItem(0).getCount() == 3,
                "an eligible pickup swap must insert the carried source stack");
            helper.assertTrue(menu.getCarried().is(Items.DIRT) && menu.getCarried().getCount() == 2,
                "an eligible pickup swap must return the prior destination stack to the cursor");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Inventory pickup swap transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void partialStackDenialAndEligibleTransferConserveEveryItem(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(13, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        destination.setItem(0, new ItemStack(Items.DIAMOND, 62));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.PICKUP, player);
            int deniedStateId = menu.getStateId();
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND) && destination.getItem(0).getCount() == 62,
                "a denied partial-stack insertion must retain the destination count");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 3,
                "a denied partial-stack insertion must retain every carried item");
            helper.assertTrue(menu.getStateId() == deniedStateId,
                "a denied partial-stack insertion must not advance the authoritative menu state");

            stages.grantStage(teamId, STAGE);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND) && destination.getItem(0).getCount() == 64,
                "an eligible partial-stack insertion must fill only the available destination space");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 1,
                "an eligible partial-stack insertion must retain the uninserted item on the cursor");
            helper.assertTrue(destination.getItem(0).getCount() + menu.getCarried().getCount() == 65,
                "the accepted partial-stack transfer must conserve the complete item count");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Partial-stack inventory transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void quickMoveDenialLeavesSourceAndDestinationUntouched(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(2, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.QUICK_MOVE, player);
            helper.assertTrue(destination.getItem(0).isEmpty(), "a denied shift-click must not change the destination");
            helper.assertTrue(player.getInventory().getItem(0).is(Items.DIAMOND),
                "a denied shift-click must retain the source stack");

            stages.grantStage(teamId, STAGE);
            menu.clicked(hotbarSlot, 0, ClickType.QUICK_MOVE, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND),
                "an eligible shift-click must insert the same stack");
            helper.assertTrue(player.getInventory().getItem(0).isEmpty(),
                "an accepted shift-click must clear the source slot");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Inventory quick move transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void hotbarSwapDenialLeavesBothSidesUntouched(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(3, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(0, 0, ClickType.SWAP, player);
            helper.assertTrue(destination.getItem(0).isEmpty(), "a denied hotbar swap must not change the destination");
            helper.assertTrue(player.getInventory().getItem(0).is(Items.DIAMOND),
                "a denied hotbar swap must retain the source stack");

            stages.grantStage(teamId, STAGE);
            menu.clicked(0, 0, ClickType.SWAP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND),
                "an eligible hotbar swap must insert the same stack");
            helper.assertTrue(player.getInventory().getItem(0).isEmpty(),
                "an accepted hotbar swap must clear the source slot");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Inventory hotbar swap transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void quickCraftDenialLeavesEverySlotAndTheCarriedStackUntouched(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(4, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.PICKUP, player);
            quickCraft(menu, 0, player);
            helper.assertTrue(destination.getItem(0).isEmpty(), "a denied drag must not change the destination");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND), "a denied drag must retain the carried stack");

            stages.grantStage(teamId, STAGE);
            quickCraft(menu, 0, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND),
                "an eligible drag must insert the carried stack");
            helper.assertTrue(menu.getCarried().isEmpty(), "an accepted drag must clear the carried stack");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Inventory quick craft transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void pickupAllOnlyExtractsAndNeverAppliesAnInsertionRule(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(5, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();

        order.registerStage(definition);
        registry.registerStage(definition);
        destination.setItem(0, new ItemStack(Items.DIAMOND, 3));
        menu.setCarried(new ItemStack(Items.DIAMOND, 1));

        try {
            menu.clicked(1, 0, ClickType.PICKUP_ALL, player);
            helper.assertTrue(destination.getItem(0).isEmpty(),
                "double click pickup must still remove matching items from the container");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 4,
                "double click pickup must increase the carried stack without requiring the insertion stage");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Inventory pickup all transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void playerInventoryTargetUsesTheActualReverseQuickMoveDirection(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer source = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(6, player.getInventory(), source);
        StageDefinition definition = insertionDefinition("inventory", "id:minecraft:player_inventory");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);

        order.registerStage(definition);
        registry.registerStage(definition);
        source.setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            helper.assertTrue(source.getItem(0).is(Items.DIAMOND),
                "a denied container to player quick move must retain the source stack");
            helper.assertTrue(player.getInventory().items.stream().noneMatch(stack -> stack.is(Items.DIAMOND)),
                "a denied container to player quick move must not insert into player inventory");

            stages.grantStage(teamId, STAGE);
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            helper.assertTrue(source.getItem(0).isEmpty(),
                "an eligible reverse quick move must clear the source container slot");
            helper.assertTrue(player.getInventory().items.stream().anyMatch(stack -> stack.is(Items.DIAMOND)),
                "an eligible reverse quick move must insert into player inventory");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Player inventory quick move transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void sharedContainerKeepsEachPlayersStageDecisionIndependent(GameTestHelper helper) {
        TestServerPlayer deniedPlayer = detachedPlayer(helper);
        TestServerPlayer eligiblePlayer = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu deniedMenu = ChestMenu.threeRows(9, deniedPlayer.getInventory(), destination);
        ChestMenu eligibleMenu = ChestMenu.threeRows(10, eligiblePlayer.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID eligibleTeam = TeamProvider.getInstance().getTeamId(eligiblePlayer);
        int deniedHotbar = menuSlot(deniedMenu, deniedPlayer, 0);
        int eligibleHotbar = menuSlot(eligibleMenu, eligiblePlayer, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        deniedPlayer.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        eligiblePlayer.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 2));
        stages.grantStage(eligibleTeam, STAGE);

        try {
            deniedMenu.clicked(deniedHotbar, 0, ClickType.PICKUP, deniedPlayer);
            deniedMenu.clicked(0, 0, ClickType.PICKUP, deniedPlayer);
            helper.assertTrue(destination.getItem(0).isEmpty(),
                "a player without the stage must not insert into the shared container");
            helper.assertTrue(deniedMenu.getCarried().is(Items.DIAMOND),
                "a denied player must retain the carried stack in the shared container");

            eligibleMenu.clicked(eligibleHotbar, 0, ClickType.PICKUP, eligiblePlayer);
            eligibleMenu.clicked(0, 0, ClickType.PICKUP, eligiblePlayer);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND) && destination.getItem(0).getCount() == 2,
                "an eligible player must insert into the same shared container");
            helper.assertTrue(eligibleMenu.getCarried().isEmpty(),
                "the eligible transaction must complete without inheriting another player's stage state");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Shared inventory transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void alreadyOpenMenuUsesTheCurrentRuleSnapshotOnItsNextTransaction(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(11, player.getInventory(), destination);
        StageDefinition definition = insertionDefinition("menu", "id:minecraft:generic_9x3");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        int firstHotbar = menuSlot(menu, player, 0);
        int secondHotbar = menuSlot(menu, player, 1);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        player.getInventory().setItem(1, new ItemStack(Items.DIAMOND, 2));

        try {
            menu.clicked(firstHotbar, 0, ClickType.PICKUP, player);
            registry.clear();
            order.clear();
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND) && destination.getItem(0).getCount() == 3,
                "removing the live rule must allow the next transaction in an already-open menu");

            menu.clicked(secondHotbar, 0, ClickType.PICKUP, player);
            order.registerStage(definition);
            registry.registerStage(definition);
            int deniedStateId = menu.getStateId();
            menu.clicked(1, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(1).isEmpty(),
                "adding the live rule must deny the next transaction in the same open menu");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 2,
                "a newly denied transaction must preserve the carried stack");
            helper.assertTrue(menu.getStateId() == deniedStateId,
                "a newly denied transaction must not advance the menu state");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Open menu rule refresh failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void registeredInventoryOwnerUsesItsStableServerResolver(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        SimpleContainer destination = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(7, player.getInventory(), destination);
        ResourceLocation resolverId = ResourceLocation.fromNamespaceAndPath("progressivestages", "gametest_selling_bin");
        ResourceLocation target = ResourceLocation.fromNamespaceAndPath("gametest", "selling_bin");
        InventoryTargetResolverRegistry.get().register(resolverId,
            (initiator, currentMenu, destinationSlot) -> currentMenu == menu && destinationSlot.container == destination
                ? Optional.of(new InventoryTargetResolverRegistry.InventoryTarget(target,
                    Set.of(ResourceLocation.fromNamespaceAndPath("gametest", "selling_bins"))))
                : Optional.empty(),
            List.of(new InventoryTargetResolverRegistry.InventoryTargetDescriptor(target, "GameTest selling bin",
                Set.of(ResourceLocation.fromNamespaceAndPath("gametest", "selling_bins")), resolverId)));
        StageDefinition definition = insertionDefinition("inventory", "id:gametest:selling_bin");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.PICKUP, player);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).isEmpty(),
                "a registered inventory owner must deny insertion before changing the custom container");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND),
                "a denied custom inventory insertion must retain the carried stack");

            stages.grantStage(teamId, STAGE);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(destination.getItem(0).is(Items.DIAMOND),
                "an eligible player must insert into the registered inventory owner");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Registered inventory owner transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void blockTargetUsesTheContainerBlockIdentity(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        BlockPos chestPos = new BlockPos(1, 2, 1);
        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);
        ChestMenu menu = ChestMenu.threeRows(8, player.getInventory(), chest);
        StageDefinition definition = insertionDefinition("block", "id:minecraft:chest");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);
        int hotbarSlot = menuSlot(menu, player, 0);

        order.registerStage(definition);
        registry.registerStage(definition);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            menu.clicked(hotbarSlot, 0, ClickType.PICKUP, player);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(chest.getItem(0).isEmpty(),
                "a block target must deny insertion before changing the chest inventory");
            helper.assertTrue(menu.getCarried().is(Items.DIAMOND),
                "a denied block target insertion must retain the carried stack");

            stages.grantStage(teamId, STAGE);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            helper.assertTrue(chest.getItem(0).is(Items.DIAMOND),
                "an eligible player must insert into the same block-backed inventory");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Block target transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void recipeOutputLockUsesTheSelectedOutputItem(GameTestHelper helper) {
        TestServerPlayer player = detachedPlayer(helper);
        StageDefinition definition = StageDefinition.builder(STAGE).locks(LockDefinition.builder()
            .recipeOutputs(CategoryLocks.builder().addLocked(List.of("id:minecraft:oak_planks")).build())
            .build()).build();
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();
        TeamStageData stages = helper.getLevel().getData(StageAttachments.TEAM_STAGES);
        UUID teamId = TeamProvider.getInstance().getTeamId(player);

        order.registerStage(definition);
        registry.registerStage(definition);

        try {
            helper.assertTrue(RecipeEnforcer.isOutputItemRecipeLocked(player, Items.OAK_PLANKS),
                "a missing stage must block the selected recipe output item");
            helper.assertTrue(!RecipeEnforcer.isOutputItemRecipeLocked(player, Items.STICK),
                "an unrelated recipe output item must remain eligible");

            stages.grantStage(teamId, STAGE);
            helper.assertTrue(!RecipeEnforcer.isOutputItemRecipeLocked(player, Items.OAK_PLANKS),
                "owning the stage must permit the selected recipe output item");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Recipe output transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void playerlessHopperTransferNeverUsesAStageRule(GameTestHelper helper) {
        BlockPos hopperPos = new BlockPos(1, 3, 1);
        BlockPos chestPos = hopperPos.below();
        helper.setBlock(hopperPos, Blocks.HOPPER);
        helper.setBlock(chestPos, Blocks.CHEST);
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);
        StageDefinition definition = insertionDefinition("block", "id:minecraft:chest");
        LockRegistry registry = LockRegistry.getInstance();
        StageOrder order = StageOrder.getInstance();

        order.registerStage(definition);
        registry.registerStage(definition);
        hopper.setItem(0, new ItemStack(Items.DIAMOND, 3));

        try {
            HopperBlockEntity.pushItemsTick(helper.getLevel(), hopperPos, helper.getBlockState(hopperPos), hopper);
            helper.assertTrue(hopper.getItem(0).is(Items.DIAMOND) && hopper.getItem(0).getCount() == 2,
                "a playerless hopper transfer must leave its normal one-item transfer in the hopper");
            helper.assertTrue(chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 1,
                "a playerless hopper transfer must not inherit a player stage restriction");
            helper.succeed();
        } catch (Throwable failure) {
            helper.fail("Playerless hopper transaction failed: " + failure.getMessage());
        } finally {
            registry.clear();
            order.clear();
        }
    }

    private static StageDefinition insertionDefinition(String targetKind, String target) {
        return StageDefinition.builder(STAGE).locks(LockDefinition.builder()
            .interactions(List.of(new LockDefinition.InteractionLock("item_into_inventory",
                "id:minecraft:diamond", target, targetKind, "lock", 100,
                "GameTest inventory insertion lock")))
            .build()).build();
    }

    private static int menuSlot(ChestMenu menu, ServerPlayer player, int inventorySlot) {
        for (int index = 0; index < menu.slots.size(); index++) {
            Slot slot = menu.slots.get(index);
            if (slot.container == player.getInventory() && slot.getContainerSlot() == inventorySlot) return index;
        }
        throw new IllegalStateException("Player inventory slot is missing from the menu");
    }

    private static void quickCraft(ChestMenu menu, int destinationSlot, ServerPlayer player) {
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ClickType.QUICK_CRAFT, player);
        menu.clicked(destinationSlot, AbstractContainerMenu.getQuickcraftMask(1, 0), ClickType.QUICK_CRAFT, player);
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(2, 0), ClickType.QUICK_CRAFT, player);
    }

    private static TestServerPlayer detachedPlayer(GameTestHelper helper) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "inventory-gametest"), false);
        return new TestServerPlayer(helper, cookie);
    }

    private static final class TestServerPlayer extends ServerPlayer {
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
    }
}
