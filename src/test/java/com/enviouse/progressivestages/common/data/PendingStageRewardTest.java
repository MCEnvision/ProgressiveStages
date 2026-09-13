package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.config.StageRewards;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.PendingStageReward;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PendingStageRewardTest {
    private static final UUID ACTOR = new UUID(0x5737, 1);
    private static final StageId STAGE = StageId.parse("progressivestages:offline_reward");
    private static final StageRewards REWARDS = new StageRewards(
        List.of(new StageCost.ItemCost(ResourceLocation.parse("minecraft:bread"), 4)),
        List.of(new StageRewards.EffectReward(ResourceLocation.parse("minecraft:speed"), 1200, 2)),
        List.of("experience add @s 1 levels"), "1 180 1", 5, 7);

    @Test
    void pendingRewardsPersistWithOwnershipAndRemainActorSpecific() {
        TeamStageData data = new TeamStageData();
        data.grantPersonalStage(ACTOR, STAGE);
        data.grantStage(ACTOR, STAGE);
        var personal = new PendingStageReward(new UUID(0x5737, 2), ACTOR, new OwnerRef(OwnerKind.PERSONAL, ACTOR), STAGE, REWARDS);
        var team = new PendingStageReward(new UUID(0x5737, 3), ACTOR, new OwnerRef(OwnerKind.TEAM, ACTOR), STAGE, REWARDS);
        data.queueReward(personal);
        data.queueReward(team);
        var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var restored = TeamStageData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(List.of(personal, team), restored.getPendingRewards(ACTOR));
        assertTrue(restored.hasPersonalStage(ACTOR, STAGE));
        assertTrue(restored.hasStage(ACTOR, STAGE));
        assertThrows(UnsupportedOperationException.class, () -> restored.getPendingRewards(ACTOR).clear());
        assertFalse(restored.consumeReward(personal.receipt(), team.receipt()));
        var copy = restored.copy();
        assertTrue(copy.consumeReward(personal.receipt(), ACTOR));
        assertFalse(copy.consumeReward(personal.receipt(), ACTOR));
        assertEquals(List.of(team), copy.getPendingRewards(ACTOR));
        assertEquals(List.of(personal, team), restored.getPendingRewards(ACTOR));
        var consumed = TeamStageData.CODEC.parse(JsonOps.INSTANCE,
            TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, copy).getOrThrow()).getOrThrow();
        assertEquals(List.of(team), consumed.getPendingRewards(ACTOR));
    }

    @Test
    void malformedOrUnsupportedRewardDataCannotBecomeOwnershipWithoutItsReceipt() {
        TeamStageData data = new TeamStageData();
        var reward = new PendingStageReward(new UUID(0x5737, 2), ACTOR, new OwnerRef(OwnerKind.PERSONAL, ACTOR), STAGE, REWARDS);
        data.queueReward(reward);
        assertThrows(IllegalArgumentException.class, () -> data.queueReward(reward));
        var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow().getAsJsonObject();
        var newer = encoded.deepCopy();
        newer.addProperty("pending_reward_schema", 2);
        assertThrows(RuntimeException.class, () -> TeamStageData.CODEC.parse(JsonOps.INSTANCE, newer).getOrThrow());
        var missing = encoded.deepCopy();
        missing.remove("pending_reward_schema");
        assertThrows(RuntimeException.class, () -> TeamStageData.CODEC.parse(JsonOps.INSTANCE, missing).getOrThrow());
        var duplicate = encoded.deepCopy();
        duplicate.getAsJsonArray("pending_rewards").add(duplicate.getAsJsonArray("pending_rewards").get(0).deepCopy());
        assertThrows(RuntimeException.class, () -> TeamStageData.CODEC.parse(JsonOps.INSTANCE, duplicate).getOrThrow());
        var malformed = encoded.deepCopy();
        malformed.getAsJsonArray("pending_rewards").get(0).getAsJsonObject().addProperty("actor", "0-0-0-0-1");
        assertTrue(TeamStageData.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
        assertEquals(List.of(reward), data.getPendingRewards(ACTOR));
        assertThrows(IllegalArgumentException.class, () -> new PendingStageReward(UUID.randomUUID(), ACTOR,
            new OwnerRef(OwnerKind.PERSONAL, UUID.randomUUID()), STAGE, REWARDS));
    }
}
