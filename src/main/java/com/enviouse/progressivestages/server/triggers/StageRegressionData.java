package com.enviouse.progressivestages.server.triggers;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * v2.4: persists the real-world grant time (epoch millis) of temporary stages, so a stage with a
 * {@code [stage].duration} expires after that much WALL-CLOCK time — counting down even while the
 * server is offline. Anchored to the overworld; saved in {@code world/data/progressivestages_regression.dat}.
 */
public class StageRegressionData extends SavedData {

    private static final String DATA_NAME = "progressivestages_regression";

    /** key = teamId|stageId  ->  grant epoch millis */
    private final Map<String, Long> grantTimes = new HashMap<>();
    private final Map<String, Long> ownerGrantTimes = new HashMap<>();

    public static StageRegressionData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new Factory<>(() -> {
                var existing = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(DATA_NAME + ".dat");
                if (java.nio.file.Files.exists(existing)) {
                    throw new IllegalStateException("Stage grant clocks could not be loaded. Preserve the existing regression save before recovery.");
                }
                return new StageRegressionData();
            }, StageRegressionData::load), DATA_NAME);
    }

    private static String key(UUID teamId, StageId stageId) {
        return teamId.toString() + "|" + stageId.toString();
    }

    public void markGranted(UUID teamId, StageId stageId, long epochMillis) {
        markGranted(legacyOwner(teamId), stageId, epochMillis);
    }

    public void markGranted(OwnerRef owner, StageId stageId, long epochMillis) {
        ownerGrantTimes.put(ownerKey(owner, stageId), epochMillis);
        if (owner.equals(legacyOwner(owner.id()))) grantTimes.remove(key(owner.id(), stageId));
        setDirty();
    }

    public long getGrantTime(UUID teamId, StageId stageId) {
        return getGrantTime(legacyOwner(teamId), stageId);
    }

    public long getGrantTime(OwnerRef owner, StageId stageId) {
        Long current = ownerGrantTimes.get(ownerKey(owner, stageId));
        if (current != null) return current;
        return owner.equals(legacyOwner(owner.id())) ? grantTimes.getOrDefault(key(owner.id(), stageId), -1L) : -1L;
    }

    public void clear(UUID teamId, StageId stageId) {
        clear(legacyOwner(teamId), stageId);
    }

    public void clear(OwnerRef owner, StageId stageId) {
        boolean changed = ownerGrantTimes.remove(ownerKey(owner, stageId)) != null;
        if (owner.equals(legacyOwner(owner.id()))) changed |= grantTimes.remove(key(owner.id(), stageId)) != null;
        if (changed) setDirty();
    }

    private static OwnerRef legacyOwner(UUID id) {
        return new OwnerRef(id.equals(new UUID(0L, 0L)) ? OwnerKind.SERVER : OwnerKind.TEAM, id);
    }

    private static String ownerKey(OwnerRef owner, StageId stageId) {
        return owner.kind().name().toLowerCase(java.util.Locale.ROOT) + ":" + key(owner.id(), stageId);
    }

    public static StageRegressionData load(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("clock_schema") && (!tag.contains("clock_schema", Tag.TAG_INT)
                || tag.getInt("clock_schema") < 0 || tag.getInt("clock_schema") > 1)) {
            throw new IllegalArgumentException("Unsupported stage clock schema");
        }
        StageRegressionData d = new StageRegressionData();
        readTimes(tag, "grant_times", d.grantTimes);
        readTimes(tag, "owner_grant_times", d.ownerGrantTimes);
        return d;
    }

    private static void readTimes(CompoundTag tag, String field, Map<String, Long> destination) {
        if (tag.contains(field) && !tag.contains(field, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Invalid stage clock map. " + field);
        }
        CompoundTag times = tag.getCompound(field);
        for (String key : times.getAllKeys()) {
            if (!times.contains(key, Tag.TAG_LONG)) throw new IllegalArgumentException("Invalid stage clock timestamp. " + key);
            destination.put(key, times.getLong(key));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag times = new CompoundTag();
        for (Map.Entry<String, Long> e : grantTimes.entrySet()) times.putLong(e.getKey(), e.getValue());
        tag.put("grant_times", times);
        CompoundTag owners = new CompoundTag();
        ownerGrantTimes.forEach(owners::putLong);
        tag.put("owner_grant_times", owners);
        tag.putInt("clock_schema", 1);
        return tag;
    }
}
