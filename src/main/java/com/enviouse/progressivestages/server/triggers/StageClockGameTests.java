package com.enviouse.progressivestages.server.triggers;

import com.enviouse.progressivestages.common.api.StageCause;
import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.config.StageSlotPolicy;
import com.enviouse.progressivestages.common.data.StageAttachments;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.stage.StageOrder;
import com.enviouse.progressivestages.common.trigger.TriggerCondition;
import com.enviouse.progressivestages.common.trigger.TriggerConditionType;
import com.enviouse.progressivestages.server.rehaul.MinecraftConditionContextFactory;
import com.enviouse.progressivestages.server.rehaul.RehaulRuntime;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("progressivestages")
@PrefixGameTestTemplate(false)
public final class StageClockGameTests {
    private StageClockGameTests() {}

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void unreadableClockSaveCannotBecomeAnEmptyReplacement(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The clock fixture requires an isolated server.");
        var storage = server.overworld().getDataStorage();
        var previous = StageRegressionData.get(server);
        var path = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
            .resolve("data/progressivestages_regression.dat");
        byte[] original = java.nio.file.Files.exists(path) ? java.nio.file.Files.readAllBytes(path) : null;
        try {
            var root = new net.minecraft.nbt.CompoundTag();
            var data = new net.minecraft.nbt.CompoundTag();
            data.putInt("clock_schema", 2);
            root.put("data", data);
            net.minecraft.nbt.NbtIo.writeCompressed(root, path);
            byte[] rejected = java.nio.file.Files.readAllBytes(path);
            var cacheField = net.minecraft.world.level.storage.DimensionDataStorage.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
            ((java.util.Map<?, ?>) cacheField.get(storage)).remove("progressivestages_regression");
            boolean failed = false;
            try { StageRegressionData.get(server); }
            catch (IllegalStateException expected) { failed = expected.getMessage().contains("Preserve the existing regression save"); }
            helper.assertTrue(failed, "An unreadable existing clock save must not be replaced by empty data.");
            storage.save();
            helper.assertTrue(java.util.Arrays.equals(rejected, java.nio.file.Files.readAllBytes(path)),
                "Saving unrelated world data must leave the rejected clock file unchanged.");
            helper.succeed();
        } finally {
            storage.set("progressivestages_regression", previous);
            if (original == null) java.nio.file.Files.deleteIfExists(path);
            else java.nio.file.Files.write(path, original);
        }
    }

    @GameTest(template = "igloo/top", templateNamespace = "minecraft")
    public static void personalClocksDriveExpiryHeldConditionsAndSlotAge(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        helper.assertTrue(server.getPlayerList().getPlayers().isEmpty(), "The clock fixture requires an isolated server.");
        UUID subject = new UUID(0x5727L, 1);
        var actor = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(), new GameProfile(subject, "clock-test"));
        var personal = new OwnerRef(OwnerKind.PERSONAL, subject);
        var team = new OwnerRef(OwnerKind.TEAM, subject);
        var stage = StageId.parse("progressivestages:clock_personal");
        var older = StageId.parse("progressivestages:clock_older");
        var target = StageId.parse("progressivestages:clock_target");
        var stages = List.of(stage, older, target);
        var order = StageOrder.getInstance();
        var previous = order.getOrderedStages().stream().map(id -> order.getStageDefinition(id).orElseThrow()).toList();
        helper.assertTrue(stages.stream().noneMatch(order::stageExists), "Fixture stage IDs must be unused.");
        var manager = StageManager.getInstance();
        var data = server.overworld().getData(StageAttachments.TEAM_STAGES);
        var clocks = StageRegressionData.get(server);
        try {
            order.clear();
            order.registerStage(StageDefinition.builder(stage).teamStage(false).durationMillis(120_000)
                .slotGroup("clock_fixture").build());
            order.registerStage(StageDefinition.builder(older).teamStage(false).slotGroup("clock_fixture").build());
            order.registerStage(StageDefinition.builder(target).teamStage(false).slotGroup("clock_fixture")
                .slotLimit(2).slotPolicy(StageSlotPolicy.REPLACE_OLDEST).build());
            long now = System.currentTimeMillis();
            data.grantStage(subject, stage);
            clocks.markGranted(team, stage, now - 3_600_000);
            manager.grantStageWithCause(actor, stage, StageCause.COMMAND);
            helper.assertTrue(clocks.getGrantTime(personal, stage) >= now,
                "Actual grant events must record a personal clock.");
            helper.assertTrue(clocks.getGrantTime(team, stage) == now - 3_600_000,
                "A personal grant must preserve the colliding team clock.");
            clocks.markGranted(personal, stage, now - 60_000);
            data.grantPersonalStage(subject, older);
            clocks.markGranted(personal, older, now - 90_000);
            helper.assertTrue(manager.getSlotDecision(actor, order.getStageDefinition(target).orElseThrow())
                .replacements().equals(List.of(older)), "Slot age must use the actual owner namespace.");
            var held = StageTriggerEvaluator.class.getDeclaredMethod("stageHeldSeconds", ServerPlayer.class, TriggerCondition.class);
            held.setAccessible(true);
            long seconds = (long) held.invoke(null, actor,
                new TriggerCondition(TriggerConditionType.STAGE_HELD_FOR, stage.toString(), 1));
            helper.assertTrue(seconds >= 60 && seconds < 120, "Legacy held duration must read the personal clock.");
            Object value = MinecraftConditionContextFactory.create(actor, RehaulRuntime.get(), Set.of())
                .values().get("stage_held_for." + stage);
            helper.assertTrue(value instanceof Long millis && millis >= 60_000 && millis < 120_000,
                "Compiled held conditions must read the same personal clock.");
            StageRegressionHandler.onLogin(new PlayerEvent.PlayerLoggedInEvent(actor));
            helper.assertTrue(manager.hasStage(actor, stage), "An old team clock must not expire personal access.");
            clocks.markGranted(personal, stage, now - 180_000);
            StageRegressionHandler.onLogin(new PlayerEvent.PlayerLoggedInEvent(actor));
            helper.assertTrue(!data.hasPersonalStage(subject, stage) && data.hasStage(subject, stage),
                "Personal expiry must remove only personal ownership.");
            helper.assertTrue(clocks.getGrantTime(personal, stage) == -1
                && clocks.getGrantTime(team, stage) == now - 3_600_000,
                "The revoke event must clear only the expired owner's clock.");
            helper.succeed();
        } finally {
            for (StageId id : stages) {
                data.revokePersonalStage(subject, id);
                data.revokeStage(subject, id);
                clocks.clear(personal, id);
                clocks.clear(team, id);
            }
            order.clear();
            previous.forEach(order::registerStage);
            actor.discard();
        }
    }
}
