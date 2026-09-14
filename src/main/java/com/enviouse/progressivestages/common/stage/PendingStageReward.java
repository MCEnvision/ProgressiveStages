package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.config.StageRewards;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

/** Original acquisition rewards reserved for an explicitly identified offline actor. */
public record PendingStageReward(UUID receipt, UUID actor, OwnerRef owner, StageId stage, StageRewards rewards) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(value -> {
        try {
            UUID id = UUID.fromString(value);
            return id.toString().equals(value) ? DataResult.success(id) : DataResult.error(() -> "Invalid reward UUID");
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Invalid reward UUID");
        }
    }, UUID::toString);
    private static final Codec<StageCost.ItemCost> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("item").forGetter(StageCost.ItemCost::item),
        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count").forGetter(StageCost.ItemCost::count)
    ).apply(instance, StageCost.ItemCost::new));
    private static final Codec<StageRewards.EffectReward> EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("effect").forGetter(StageRewards.EffectReward::effect),
        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("duration").forGetter(StageRewards.EffectReward::durationTicks),
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("amplifier").forGetter(StageRewards.EffectReward::amplifier)
    ).apply(instance, StageRewards.EffectReward::new));
    private static final Codec<StageRewards> REWARDS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ITEM_CODEC.listOf().fieldOf("items").forGetter(StageRewards::items),
        EFFECT_CODEC.listOf().fieldOf("effects").forGetter(StageRewards::effects),
        Codec.STRING.listOf().fieldOf("commands").forGetter(StageRewards::commands),
        Codec.STRING.fieldOf("teleport").forGetter(StageRewards::teleport),
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("xp_levels").forGetter(StageRewards::xpLevels),
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("xp_points").forGetter(StageRewards::xpPoints)
    ).apply(instance, StageRewards::new));
    public static final Codec<PendingStageReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUID_CODEC.fieldOf("receipt").forGetter(PendingStageReward::receipt),
        UUID_CODEC.fieldOf("actor").forGetter(PendingStageReward::actor),
        Codec.STRING.fieldOf("owner_kind").forGetter(value -> value.owner().kind().name()),
        UUID_CODEC.fieldOf("owner").forGetter(value -> value.owner().id()),
        ResourceLocation.CODEC.fieldOf("stage").forGetter(value -> value.stage().getResourceLocation()),
        REWARDS_CODEC.fieldOf("rewards").forGetter(PendingStageReward::rewards)
    ).apply(instance, (receipt, actor, kind, owner, stage, rewards) -> new PendingStageReward(receipt, actor,
        new OwnerRef(OwnerKind.valueOf(kind), owner), StageId.fromResourceLocation(stage), rewards)));

    public PendingStageReward {
        Objects.requireNonNull(receipt, "receipt");
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(rewards, "rewards");
        for (var item : rewards.items()) {
            if (item.item() == null || item.count() < 1) throw new IllegalArgumentException("Invalid reward item");
        }
        for (var effect : rewards.effects()) {
            if (effect.effect() == null || effect.durationTicks() < 1 || effect.amplifier() < 0) {
                throw new IllegalArgumentException("Invalid reward effect");
            }
        }
        if (owner.kind() == OwnerKind.PERSONAL && !owner.id().equals(actor)
            || owner.kind() == OwnerKind.SERVER && !owner.id().equals(new UUID(0, 0))) {
            throw new IllegalArgumentException("Invalid reward owner");
        }
    }
}
