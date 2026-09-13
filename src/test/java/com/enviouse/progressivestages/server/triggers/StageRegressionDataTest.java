package com.enviouse.progressivestages.server.triggers;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StageRegressionDataTest {
    private static final UUID ID = new UUID(0, 91);
    private static final StageId STAGE = StageId.parse("example:timed_profession");
    private static final OwnerRef PERSONAL = new OwnerRef(OwnerKind.PERSONAL, ID);
    private static final OwnerRef TEAM = new OwnerRef(OwnerKind.TEAM, ID);

    @Test
    void ownerKindsRemainIndependentThroughSaveLoadAndRemoval() {
        StageRegressionData data = new StageRegressionData();
        OwnerRef server = new OwnerRef(OwnerKind.SERVER, ID);
        data.markGranted(PERSONAL, STAGE, 100);
        data.markGranted(TEAM, STAGE, 200);
        data.markGranted(server, STAGE, 300);
        StageRegressionData loaded = StageRegressionData.load(data.save(new CompoundTag(), null), null);
        assertEquals(100, loaded.getGrantTime(PERSONAL, STAGE));
        assertEquals(200, loaded.getGrantTime(TEAM, STAGE));
        assertEquals(300, loaded.getGrantTime(server, STAGE));
        loaded.clear(PERSONAL, STAGE);
        assertEquals(-1, loaded.getGrantTime(PERSONAL, STAGE));
        assertEquals(200, loaded.getGrantTime(TEAM, STAGE));
        assertEquals(300, loaded.getGrantTime(server, STAGE));
    }

    @Test
    void legacyTeamHistoryNeverBecomesPersonalHistory() {
        CompoundTag tag = new CompoundTag();
        CompoundTag times = new CompoundTag();
        times.putLong(ID + "|" + STAGE, 123);
        times.putLong("unrecognized retained record", 456);
        tag.put("grant_times", times);
        StageRegressionData data = StageRegressionData.load(tag, null);
        assertEquals(123, data.getGrantTime(TEAM, STAGE));
        assertEquals(123, data.getGrantTime(ID, STAGE));
        assertEquals(-1, data.getGrantTime(PERSONAL, STAGE));
        data.markGranted(PERSONAL, STAGE, 789);
        assertEquals(123, data.getGrantTime(TEAM, STAGE));
        data.clear(PERSONAL, STAGE);
        assertEquals(times, data.save(new CompoundTag(), null).getCompound("grant_times"));
        data.markGranted(ID, STAGE, 900);
        CompoundTag saved = data.save(new CompoundTag(), null);
        assertFalse(saved.getCompound("grant_times").contains(ID + "|" + STAGE));
        assertEquals(456, saved.getCompound("grant_times").getLong("unrecognized retained record"));
        assertEquals(900, StageRegressionData.load(saved, null).getGrantTime(ID, STAGE));
        data.clear(ID, STAGE);
        assertEquals(-1, data.getGrantTime(TEAM, STAGE));
    }

    @Test
    void legacyServerClockDoesNotLeakIntoZeroUuidTeamOrPersonalOwners() {
        UUID zero = new UUID(0, 0);
        CompoundTag tag = new CompoundTag();
        CompoundTag times = new CompoundTag();
        times.putLong(zero + "|" + STAGE, 333);
        tag.put("grant_times", times);
        StageRegressionData data = StageRegressionData.load(tag, null);
        assertEquals(333, data.getGrantTime(new OwnerRef(OwnerKind.SERVER, zero), STAGE));
        assertEquals(-1, data.getGrantTime(new OwnerRef(OwnerKind.TEAM, zero), STAGE));
        assertEquals(-1, data.getGrantTime(new OwnerRef(OwnerKind.PERSONAL, zero), STAGE));
        data.clear(zero, STAGE);
        assertEquals(-1, data.getGrantTime(zero, STAGE));
    }

    @Test
    void unsupportedAndMalformedClockRecordsFailWithoutChangingInput() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("clock_schema", 2);
        assertThrows(IllegalArgumentException.class, () -> StageRegressionData.load(tag, null));
        tag.putString("clock_schema", "1");
        assertThrows(IllegalArgumentException.class, () -> StageRegressionData.load(tag, null));
        tag.putInt("clock_schema", 1);
        tag.putString("owner_grant_times", "invalid");
        assertThrows(IllegalArgumentException.class, () -> StageRegressionData.load(tag, null));
        CompoundTag times = new CompoundTag();
        times.putString("personal:" + ID + "|" + STAGE, "invalid");
        tag.put("owner_grant_times", times);
        CompoundTag before = tag.copy();
        assertThrows(IllegalArgumentException.class, () -> StageRegressionData.load(tag, null));
        assertEquals(before, tag);
    }
}
