package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.common.rehaul.CompiledSnapshot;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.server.loader.Schema4StageCompiler;
import com.enviouse.progressivestages.server.loader.StagePackageParser;
import com.enviouse.progressivestages.server.rehaul.RehaulRuntime;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class EditorRuleGameTests {
    private static final StageId STAGE = StageId.parse("progressivestages:editor_rule_test");

    private EditorRuleGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void pickupRuleDoesNotBecomeUseOrInventoryLock(GameTestHelper helper) {
        withRule(helper, "items", "pickup", "minecraft:bread", player -> {
            ItemStack bread = new ItemStack(Items.BREAD);
            helper.assertTrue(!ItemEnforcer.canPickupItem(player, bread), "Pickup must consult the selected action.");
            helper.assertTrue(ItemEnforcer.canUseItem(player, bread), "A pickup rule must not block item use.");
            helper.assertTrue(ItemEnforcer.canHoldItem(player, bread), "A pickup rule must not remove held items.");
            grant(helper, player);
            helper.assertTrue(ItemEnforcer.canPickupItem(player, bread), "The owned stage must permit pickup.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void placementRuleDoesNotBecomeInteractionLock(GameTestHelper helper) {
        withRule(helper, "blocks", "place", "minecraft:chest", player -> {
            helper.assertTrue(!BlockEnforcer.canPlaceBlock(player, Blocks.CHEST), "The placement rule must deny placement.");
            helper.assertTrue(BlockEnforcer.canInteractWithBlock(player, Blocks.CHEST), "Placement must not gate opening a placed chest.");
            grant(helper, player);
            helper.assertTrue(BlockEnforcer.canPlaceBlock(player, Blocks.CHEST), "The owned stage must permit placement.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void cropRuleUsesItsActionAndStageOwnership(GameTestHelper helper) {
        withRule(helper, "crops", "bonemeal", "minecraft:wheat", player -> {
            helper.assertTrue(!CropEnforcer.canBonemeal(player, Blocks.WHEAT), "The crop rule must reach bonemeal enforcement.");
            helper.assertTrue(CropEnforcer.canPlace(player, Blocks.WHEAT), "A bonemeal rule must leave planting available.");
            grant(helper, player);
            helper.assertTrue(CropEnforcer.canBonemeal(player, Blocks.WHEAT), "The owned stage must permit bonemeal.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void screenRuleReachesTheBlockOpenGate(GameTestHelper helper) {
        withRule(helper, "screens", "open", "minecraft:chest", player -> {
            helper.assertTrue(!ScreenEnforcer.canOpenScreen(player, Blocks.CHEST), "The generic screen rule must deny opening the chest.");
            helper.assertTrue(ScreenEnforcer.canOpenScreen(player, Blocks.BARREL), "Unmatched screens must remain available.");
            grant(helper, player);
            helper.assertTrue(ScreenEnforcer.canOpenScreen(player, Blocks.CHEST), "The owned stage must permit opening the chest.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void enchantAnvilRuleDoesNotStripOwnedEquipment(GameTestHelper helper) {
        withRule(helper, "enchants", "anvil", "minecraft:sharpness", player -> {
            var enchantment = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(enchantment, 1);
            helper.assertTrue(EnchantEnforcer.anyEnchantLocked(player, sword), "The generic enchant rule must reach the anvil gate.");
            helper.assertTrue(!EnchantEnforcer.stripLockedEnchants(player, sword), "An anvil rule must not strip an existing enchantment.");
            grant(helper, player);
            helper.assertTrue(!EnchantEnforcer.anyEnchantLocked(player, sword), "The owned stage must permit the anvil input.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void tradePurchaseRuleLeavesTheOfferVisible(GameTestHelper helper) {
        withRule(helper, "trades", "purchase", "minecraft:bread", player -> {
            var offer = new net.minecraft.world.item.trading.MerchantOffer(
                new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, 1), new ItemStack(Items.BREAD), 10, 1, 0);
            helper.assertTrue(TradeEnforcer.isOfferLocked(player, offer), "A purchase rule must block the transaction.");
            helper.assertTrue(!TradeEnforcer.isOfferLocked(player, offer, "display"), "The purchase rule must not hide the offer.");
            grant(helper, player);
            helper.assertTrue(!TradeEnforcer.isOfferLocked(player, offer), "The owned stage must permit the transaction.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void professionRuleBlocksTheVillagerTradeGate(GameTestHelper helper) {
        withRule(helper, "professions", "trade", "minecraft:farmer", player -> {
            var villager = net.minecraft.world.entity.EntityType.VILLAGER.create(helper.getLevel());
            villager.setVillagerData(villager.getVillagerData().setProfession(net.minecraft.world.entity.npc.VillagerProfession.FARMER));
            helper.assertTrue(!VillagerProfessionEnforcer.canTradeWith(player, villager), "The profession rule must block the farmer trade gate.");
            grant(helper, player);
            helper.assertTrue(VillagerProfessionEnforcer.canTradeWith(player, villager), "The owned stage must permit farmer trades.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void petBreedingRuleDoesNotBlockCommands(GameTestHelper helper) {
        withRule(helper, "pets", "breed", "minecraft:wolf", player -> {
            var wolf = net.minecraft.world.entity.EntityType.WOLF.create(helper.getLevel());
            wolf.setTame(true, false);
            wolf.setOwnerUUID(player.getUUID());
            helper.assertTrue(!PetEnforcer.canInteract(player, wolf.getType(), wolf, new ItemStack(Items.BEEF)),
                "Feeding an owned pet must consult the breeding action.");
            helper.assertTrue(PetEnforcer.canInteract(player, wolf.getType(), wolf, ItemStack.EMPTY),
                "The breeding rule must leave pet commands available.");
            grant(helper, player);
            helper.assertTrue(PetEnforcer.canInteract(player, wolf.getType(), wolf, new ItemStack(Items.BEEF)),
                "The owned stage must permit feeding the pet.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void beaconRuleReachesEffectEligibility(GameTestHelper helper) {
        withRule(helper, "beacon", "apply", "minecraft:speed", player -> {
            var registry = LockRegistry.getInstance();
            var id = ResourceLocation.parse("minecraft:speed");
            helper.assertTrue(registry.hasBeaconLocks() && registry.isBeaconEffectBlockedFor(player, id),
                "The generic beacon rule must enable and reach effect enforcement.");
            grant(helper, player);
            helper.assertTrue(!registry.isBeaconEffectBlockedFor(player, id), "The owned stage must permit the effect.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void brewingTakeRuleLeavesBrewingAvailable(GameTestHelper helper) {
        withRule(helper, "brewing", "take", "minecraft:swiftness", player -> {
            var registry = LockRegistry.getInstance();
            var id = ResourceLocation.parse("minecraft:swiftness");
            helper.assertTrue(registry.hasBrewingLocks() && registry.isBrewingBlockedFor(player, id, "take"),
                "The generic brewing rule must enable the output gate.");
            helper.assertTrue(!registry.isBrewingBlockedFor(player, id, "brew"), "The take rule must leave brewing available.");
            grant(helper, player);
            helper.assertTrue(!registry.isBrewingBlockedFor(player, id, "take"), "The owned stage must permit collecting the potion.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void toastRulePreservesAdvancementVisibilityAndSharedDefinition(GameTestHelper helper) {
        withRule(helper, "advancements", "toast", "minecraft:story/mine_stone", player -> {
            var holder = helper.getLevel().getServer().getAdvancements().get(ResourceLocation.parse("minecraft:story/mine_stone"));
            var filtered = AdvancementHider.filterToast(player, holder);
            helper.assertTrue(!LockRegistry.getInstance().isAdvancementHiddenFor(player, holder.id()),
                "A toast rule must leave the advancement visible.");
            helper.assertTrue(!filtered.value().display().orElseThrow().shouldShowToast()
                    && holder.value().display().orElseThrow().shouldShowToast(),
                "The toast rule must change only the receiving player's display copy.");
            grant(helper, player);
            helper.assertTrue(AdvancementHider.filterToast(player, holder) == holder, "The owned stage must restore the normal toast.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void selectedDropRulePreservesTheInventoryBeforeRemoval(GameTestHelper helper) {
        withRule(helper, "items", "drop", "minecraft:bread", player -> {
            player.getInventory().setItem(0, new ItemStack(Items.BREAD, 4));
            helper.assertTrue(!player.drop(false) && player.getMainHandItem().getCount() == 4,
                "The actual selected drop must stop before removing an item.");
            helper.assertTrue(ItemEnforcer.canUseItem(player, player.getMainHandItem()), "A drop rule must leave use available.");
            grant(helper, player);
            helper.assertTrue(ItemEnforcer.canDropItem(player, player.getMainHandItem()), "The owned stage must permit dropping.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void bucketPlacementStopsBeforeWorldOrStackMutation(GameTestHelper helper) {
        withRule(helper, "fluids", "place", "minecraft:water", player -> {
            var ground = helper.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
            var target = ground.above();
            helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(target.above(), Blocks.AIR.defaultBlockState());
            player.setPos(target.getX() + 0.5, target.getY() + 0.01, target.getZ() + 0.5);
            player.setXRot(90);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            var denied = Items.WATER_BUCKET.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
            helper.assertTrue(denied.getResult() == net.minecraft.world.InteractionResult.FAIL
                    && helper.getLevel().getFluidState(target).isEmpty()
                    && player.getMainHandItem().is(Items.WATER_BUCKET),
                "A locked bucket must preserve both the water stack and destination.");
            helper.assertTrue(!LockRegistry.getInstance().isFluidBlockedFor(player,
                    ResourceLocation.parse("minecraft:water"), "pickup"),
                "A placement rule must leave fluid pickup available.");
            grant(helper, player);
            var allowed = Items.WATER_BUCKET.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
            helper.assertTrue(allowed.getResult().consumesAction()
                    && !helper.getLevel().getFluidState(target).isEmpty()
                    && allowed.getObject().is(Items.BUCKET),
                "The owned stage must let the actual bucket place water and return an empty bucket.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void abilityUseRuleReachesTheMovementGate(GameTestHelper helper) {
        withRule(helper, "abilities", "use", "minecraft:sprint", player -> {
            helper.assertTrue(AbilityEnforcer.lockedAbilities(player).contains("sprint"),
                "The builder use action must reach the movement permission query.");
            grant(helper, player);
            helper.assertTrue(!AbilityEnforcer.lockedAbilities(player).contains("sprint"),
                "The owned stage must restore sprinting.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void teleportRuleDoesNotBecomeAPortalRule(GameTestHelper helper) {
        withRule(helper, "dimensions", "teleport", "minecraft:the_nether", player -> {
            helper.assertTrue(!DimensionEnforcer.canTravelToDimension(player, net.minecraft.world.level.Level.NETHER),
                "Direct dimension travel must query the teleport action.");
            helper.assertTrue(LockRegistry.getInstance().restrictionStagesForDimension(player,
                    net.minecraft.world.level.Level.NETHER.location(), "portal").isEmpty(),
                "The teleport rule must leave portal travel available.");
            grant(helper, player);
            helper.assertTrue(DimensionEnforcer.canTravelToDimension(player, net.minecraft.world.level.Level.NETHER),
                "The owned stage must permit direct travel.");
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void flowingWaterWaitsForTheNearbyPlayersStage(GameTestHelper helper) {
        withRule(helper, "fluids", "flow", "minecraft:water", player -> {
            var source = helper.absolutePos(new net.minecraft.core.BlockPos(2, 3, 2));
            var target = source.below();
            helper.getLevel().setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            player.setPos(source.getX(), source.getY(), source.getZ());
            helper.getLevel().players().add(player);
            try {
                helper.getLevel().getFluidState(source).tick(helper.getLevel(), source);
                helper.assertTrue(helper.getLevel().getFluidState(target).isEmpty(),
                    "Native water spreading must stop before filling the neighboring block.");
                grant(helper, player);
                helper.getLevel().getFluidState(source).tick(helper.getLevel(), source);
                helper.assertTrue(!helper.getLevel().getFluidState(target).isEmpty(),
                    "The owned stage must permit actual water spreading.");
            } finally { helper.getLevel().players().remove(player); }
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void brewingStopsBeforeConsumingTheIngredient(GameTestHelper helper) {
        withRule(helper, "brewing", "brew", "minecraft:swiftness", player -> {
            var pos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
            helper.getLevel().setBlockAndUpdate(pos, Blocks.BREWING_STAND.defaultBlockState());
            var stand = (net.minecraft.world.level.block.entity.BrewingStandBlockEntity) helper.getLevel().getBlockEntity(pos);
            stand.setItem(0, net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,
                net.minecraft.world.item.alchemy.Potions.AWKWARD));
            stand.setItem(3, new ItemStack(Items.SUGAR, 2));
            stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            player.setPos(pos.getX(), pos.getY(), pos.getZ());
            helper.getLevel().players().add(player);
            try {
                for (int tick = 0; tick < 405; tick++) {
                    net.minecraft.world.level.block.entity.BrewingStandBlockEntity.serverTick(helper.getLevel(), pos,
                        helper.getLevel().getBlockState(pos), stand);
                }
                helper.assertTrue(stand.getItem(3).getCount() == 2 && stand.getItem(0)
                        .get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)
                        .is(net.minecraft.world.item.alchemy.Potions.AWKWARD),
                    "A locked brew must preserve its ingredient and original potion.");
                grant(helper, player);
                for (int tick = 0; tick < 405; tick++) {
                    net.minecraft.world.level.block.entity.BrewingStandBlockEntity.serverTick(helper.getLevel(), pos,
                        helper.getLevel().getBlockState(pos), stand);
                }
                helper.assertTrue(stand.getItem(3).getCount() == 1 && stand.getItem(0)
                        .get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)
                        .is(net.minecraft.world.item.alchemy.Potions.SWIFTNESS),
                    "The owned stage must permit the native potion transaction exactly once.");
            } finally { helper.getLevel().players().remove(player); }
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void toastReturnsWhenTheWeatherConditionEnds(GameTestHelper helper) {
        withRule(helper, "advancements", "toast", "minecraft:story/mine_stone",
            "while={type=\"weather\",value=\"rain\"}\n", player -> {
            var level = helper.getLevel();
            float rain = level.getRainLevel(1);
            var suppressed = new java.util.HashSet<ResourceLocation>();
            var holder = level.getServer().getAdvancements().get(ResourceLocation.parse("minecraft:story/mine_stone"));
            var progress = new net.minecraft.advancements.AdvancementProgress();
            progress.update(holder.value().requirements());
            var update = new net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket(false,
                java.util.List.of(), java.util.Set.of(), Map.of(holder.id(), progress));
            try {
                level.setRainLevel(1);
                var hidden = AdvancementHider.filterUpdate(player, update, suppressed);
                helper.assertTrue(hidden.getAdded().stream().anyMatch(value -> value.id().equals(holder.id())
                        && !value.value().display().orElseThrow().shouldShowToast()),
                    "A progress packet during rain must suppress its toast.");
                level.setRainLevel(0);
                var restored = AdvancementHider.filterUpdate(player, update, suppressed);
                helper.assertTrue(restored.getAdded().stream().anyMatch(value -> value.id().equals(holder.id())
                        && value.value().display().orElseThrow().shouldShowToast()) && suppressed.isEmpty(),
                    "Ending rain must restore the client holder without granting a stage or reloading.");
            } finally { level.setRainLevel(rain); }
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void lavaConversionWaitsForTheNearbyPlayersStage(GameTestHelper helper) {
        withRule(helper, "fluids", "flow", "minecraft:lava", player -> {
            var source = helper.absolutePos(new net.minecraft.core.BlockPos(2, 3, 2));
            var target = source.below();
            helper.getLevel().setBlockAndUpdate(target, Blocks.WATER.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(source, Blocks.LAVA.defaultBlockState());
            player.setPos(source.getX(), source.getY(), source.getZ());
            helper.getLevel().players().add(player);
            try {
                helper.getLevel().getFluidState(source).tick(helper.getLevel(), source);
                helper.assertTrue(helper.getLevel().getBlockState(target).is(Blocks.WATER),
                    "Locked downward lava must preserve the water below it.");
                grant(helper, player);
                helper.getLevel().getFluidState(source).tick(helper.getLevel(), source);
                helper.assertTrue(helper.getLevel().getBlockState(target).is(Blocks.STONE),
                    "The owned stage must permit native lava and water conversion.");
            } finally { helper.getLevel().players().remove(player); }
        });
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft", batch = "progressivestages_editor_rules")
    public static void legacyPotionLocksStillAllowBrewingButBlockExtraction(GameTestHelper helper) {
        withRule(helper, "items", "use", "minecraft:stick",
            "[brewing]\nlocked=[\"id:minecraft:swiftness\"]\n", player -> {
            var pos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
            helper.getLevel().setBlockAndUpdate(pos, Blocks.BREWING_STAND.defaultBlockState());
            var stand = (net.minecraft.world.level.block.entity.BrewingStandBlockEntity) helper.getLevel().getBlockEntity(pos);
            stand.setItem(0, net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,
                net.minecraft.world.item.alchemy.Potions.AWKWARD));
            stand.setItem(3, new ItemStack(Items.SUGAR, 2));
            stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
            player.setPos(pos.getX(), pos.getY(), pos.getZ());
            helper.getLevel().players().add(player);
            try {
                for (int tick = 0; tick < 405; tick++) {
                    net.minecraft.world.level.block.entity.BrewingStandBlockEntity.serverTick(helper.getLevel(), pos,
                        helper.getLevel().getBlockState(pos), stand);
                }
                var potion = stand.getItem(0);
                helper.assertTrue(stand.getItem(3).getCount() == 1 && potion
                        .get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)
                        .is(net.minecraft.world.item.alchemy.Potions.SWIFTNESS),
                    "A legacy extraction lock must still let the potion brew.");
                helper.assertTrue(!stand.canTakeItemThroughFace(0, potion, net.minecraft.core.Direction.DOWN)
                        && LockRegistry.getInstance().isBrewingBlockedFor(player, ResourceLocation.parse("minecraft:swiftness"), "take"),
                    "Legacy locks must retain both hopper and player extraction checks.");
                grant(helper, player);
                helper.assertTrue(stand.canTakeItemThroughFace(0, potion, net.minecraft.core.Direction.DOWN),
                    "The owned stage must release hopper extraction.");
            } finally { helper.getLevel().players().remove(player); }
        });
    }

    private static void grant(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getData(StageAttachments.TEAM_STAGES)
            .grantStage(TeamProvider.getInstance().getTeamId(player), STAGE);
    }

    private static void withRule(GameTestHelper helper, String category, String action, String target,
                                 Consumer<ServerPlayer> assertions) {
        withRule(helper, category, action, target, "", assertions);
    }

    private static void withRule(GameTestHelper helper, String category, String action, String target,
                                 String extraSettings, Consumer<ServerPlayer> assertions) {
        String identity = "[schema]\nversion=4\n[stage]\nid=\"" + STAGE + "\"\n";
        String rules = """
            [enforcement]
            block_item_use = true
            block_item_pickup = true
            block_item_inventory = true
            block_block_placement = true
            block_block_interaction = true
            block_crop_growth = true
            block_screen_open = true
            block_enchants = true
            block_pet_interact = true
            block_dimension_travel = true

            [[rules]]
            id = "progressivestages:editor_rule_test/rule"
            effect = "lock"
            priority = 100
            action = "%s"
            targets.%s = ["id:%s"]
            """.formatted(action, category, target) + extraSettings;
        var parsed = StagePackageParser.parseContents("editor", "stage.toml", identity, "rules.toml", rules,
            "progression.toml", "");
        helper.assertTrue(parsed.isSuccess(), parsed.getErrorMessage());
        var definition = parsed.getStageDefinition();
        var compiled = Schema4StageCompiler.compile(definition, parsed.getSourceConfig(), "editor", 0);
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "editor-test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var runtime = RehaulRuntime.get();
        var previous = runtime.snapshot();
        StageOrder.getInstance().registerStage(definition);
        LockRegistry.getInstance().registerStage(definition);
        runtime.rules().rebuild(CompiledSnapshot.create(1, Map.of(STAGE, compiled)));
        try {
            assertions.accept(player);
            helper.succeed();
        } finally {
            helper.getLevel().getData(StageAttachments.TEAM_STAGES)
                .revokeStage(TeamProvider.getInstance().getTeamId(player), STAGE);
            runtime.rules().rebuild(previous);
            LockRegistry.getInstance().clear();
            StageOrder.getInstance().clear();
        }
    }
}
