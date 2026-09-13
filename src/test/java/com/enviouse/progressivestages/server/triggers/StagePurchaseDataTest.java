package com.enviouse.progressivestages.server.triggers;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StagePurchaseDataTest {

    private static final UUID PAYER = new UUID(0x5735, 1);
    private static final UUID OTHER = new UUID(0x5735, 2);
    private static final StageId STAGE = StageId.parse("example:purchased_profession");
    private static final OwnerRef PERSONAL = new OwnerRef(OwnerKind.PERSONAL, PAYER);
    private static final OwnerRef TEAM = new OwnerRef(OwnerKind.TEAM, PAYER);
    private static final StageCost COST = new StageCost(10,
        List.of(new StageCost.ItemCost(ResourceLocation.parse("minecraft:bread"), 4)), true, 30, 50);

    @Test
    void purchasesPreservePayersAndSeparateCollidingOwnersAcrossReload() {
        StagePurchaseData data = new StagePurchaseData();
        data.markPaid(PAYER, STAGE);
        assertTrue(data.markActorPurchase(PERSONAL, STAGE, PAYER, COST));
        assertTrue(data.isPaid(PAYER, STAGE));
        assertTrue(data.markActorPurchase(TEAM, STAGE, OTHER, COST));
        assertFalse(data.isPaid(PAYER, STAGE));
        assertFalse(data.markActorPurchase(TEAM, STAGE, PAYER, COST));
        StagePurchaseData loaded = StagePurchaseData.load(data.save(new CompoundTag(), null), null);
        assertEquals(PAYER, loaded.getActorPurchase(PERSONAL, STAGE).orElseThrow().payer());
        assertEquals(OTHER, loaded.getActorPurchase(TEAM, STAGE).orElseThrow().payer());
        var personalRefund = loaded.deferActorRefund(PERSONAL, STAGE).orElseThrow();
        assertFalse(loaded.consumeActorRefund(personalRefund.receiptId(), OTHER));
        assertTrue(loaded.getPendingActorRefunds(OTHER).isEmpty());
        assertEquals(COST.items(), personalRefund.cost().items());
        assertEquals(10, personalRefund.cost().xpLevels());
        assertEquals(50, personalRefund.cost().refundPercent());
        assertTrue(loaded.consumeActorRefund(personalRefund.receiptId(), PAYER));
        assertFalse(loaded.consumeActorRefund(personalRefund.receiptId(), PAYER));
        assertTrue(loaded.getActorPurchase(TEAM, STAGE).isPresent());
    }

    @Test
    void repeatedPurchasesRetainSeparatePendingRefundsForTheOriginalPayer() {
        StagePurchaseData data = new StagePurchaseData();
        assertTrue(data.markActorPurchase(TEAM, STAGE, PAYER, COST));
        var first = data.deferActorRefund(TEAM, STAGE).orElseThrow();
        assertTrue(data.deferActorRefund(TEAM, STAGE).isEmpty());
        assertTrue(data.markActorPurchase(TEAM, STAGE, PAYER, COST));
        var second = data.deferActorRefund(TEAM, STAGE).orElseThrow();
        assertNotEquals(first.receiptId(), second.receiptId());
        StagePurchaseData loaded = StagePurchaseData.load(data.save(new CompoundTag(), null), null);
        assertEquals(2, loaded.getPendingActorRefunds(PAYER).size());
        assertTrue(loaded.consumeActorRefund(first.receiptId(), PAYER));
        assertEquals(List.of(second), loaded.getPendingActorRefunds(PAYER));
        assertThrows(UnsupportedOperationException.class, () -> loaded.getPendingActorRefunds(PAYER).clear());
    }

    @Test
    void legacyRecordsRemainLegacyAndNewCorruptReceiptsFailWithoutRewritingInput() {
        StagePurchaseData data = new StagePurchaseData();
        data.markPaid(PAYER, STAGE);
        data.deferRefund(PAYER, STAGE);
        assertTrue(data.markActorPurchase(PERSONAL, STAGE, PAYER, COST));
        CompoundTag tag = data.save(new CompoundTag(), null);
        StagePurchaseData loaded = StagePurchaseData.load(tag, null);
        assertEquals(Set.of(STAGE), loaded.getPendingRefunds(PAYER));
        assertTrue(loaded.getPendingActorRefunds(PAYER).isEmpty());
        CompoundTag bad = tag.copy();
        bad.putInt("purchase_schema", 2);
        assertThrows(IllegalArgumentException.class, () -> StagePurchaseData.load(bad, null));
        bad.putInt("purchase_schema", 1);
        bad.getList("actor_paid", Tag.TAG_COMPOUND).getCompound(0).putString("payer", "unknown");
        CompoundTag before = bad.copy();
        assertThrows(IllegalArgumentException.class, () -> StagePurchaseData.load(bad, null));
        assertEquals(before, bad);
        CompoundTag duplicate = tag.copy();
        duplicate.put("actor_pending_refunds", tag.getList("actor_paid", Tag.TAG_COMPOUND).copy());
        assertThrows(IllegalArgumentException.class, () -> StagePurchaseData.load(duplicate, null));
        CompoundTag missingVersion = tag.copy();
        missingVersion.remove("purchase_schema");
        assertThrows(IllegalArgumentException.class, () -> StagePurchaseData.load(missingVersion, null));
        CompoundTag malformedList = tag.copy();
        malformedList.putString("actor_paid", "invalid");
        assertThrows(IllegalArgumentException.class, () -> StagePurchaseData.load(malformedList, null));
        CompoundTag malformedLegacy = tag.copy();
        malformedLegacy.putString("paid", "invalid");
        assertThrows(IllegalArgumentException.class, () -> StagePurchaseData.load(malformedLegacy, null));
        assertThrows(IllegalArgumentException.class, () -> data.markActorPurchase(PERSONAL, STAGE, OTHER, COST));
    }

    @Test
    void pendingRefundsRetainNamespacedStageIdentifiers() {
        StagePurchaseData data = new StagePurchaseData();
        UUID team = UUID.randomUUID();
        StageId stage = StageId.parse("example:advanced_stage");

        data.markPaid(team, stage);
        assertTrue(data.deferRefund(team, stage));
        assertEquals(Set.of(stage), data.getPendingRefunds(team));
    }
}
