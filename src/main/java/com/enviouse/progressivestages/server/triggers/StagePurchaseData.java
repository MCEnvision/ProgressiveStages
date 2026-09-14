package com.enviouse.progressivestages.server.triggers;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageCost;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * v3.0: records which stages a team actually PURCHASED from the skill tree, so {@code [cost]}'s
 * {@code refund_percent} only ever refunds stages that were paid for. Without this, a stage earned
 * via a trigger/command/quest — or a purchasable temporary stage that auto-expires and is re-bought —
 * would mint free items/xp on every revoke. Anchored to the overworld; saved in
 * {@code world/data/progressivestages_purchases.dat}.
 */
public class StagePurchaseData extends SavedData {

    private static final String DATA_NAME = "progressivestages_purchases";

    /** Set of keys "teamId|stageId" for stages this team has paid for and not yet been refunded. */
    private final Set<String> paid = new HashSet<>();
    /** Paid stages revoked while their team was offline; delivered to the next member who joins. */
    private final Set<String> pendingRefunds = new HashSet<>();
    private record PurchaseKey(OwnerRef owner, StageId stage) {}
    public record ActorPurchase(UUID receiptId, OwnerRef owner, StageId stage, UUID payer, StageCost cost) {}
    private final Map<PurchaseKey, ActorPurchase> actorPaid = new HashMap<>();
    private final Map<UUID, ActorPurchase> actorPending = new HashMap<>();

    public static StagePurchaseData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new Factory<>(() -> {
                var existing = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(DATA_NAME + ".dat");
                if (java.nio.file.Files.exists(existing)) {
                    throw new IllegalStateException("Stage purchases could not be loaded. Preserve the purchase save before recovery.");
                }
                return new StagePurchaseData();
            }, StagePurchaseData::load), DATA_NAME);
    }

    public boolean markActorPurchase(OwnerRef owner, StageId stage, UUID payer, StageCost cost) {
        java.util.Objects.requireNonNull(owner);
        java.util.Objects.requireNonNull(stage);
        java.util.Objects.requireNonNull(payer);
        java.util.Objects.requireNonNull(cost);
        if (cost.xpLevels() < 0 || owner.kind() == OwnerKind.PERSONAL && !owner.id().equals(payer)) {
            throw new IllegalArgumentException("Invalid stage purchase actor or cost");
        }
        PurchaseKey key = new PurchaseKey(owner, stage);
        if (actorPaid.containsKey(key)) return false;
        StageCost recorded = new StageCost(cost.xpLevels(), cost.items(), false, 0, cost.refundPercent());
        actorPaid.put(key, new ActorPurchase(UUID.randomUUID(), owner, stage, payer, recorded));
        OwnerKind legacyKind = owner.id().equals(new UUID(0, 0)) ? OwnerKind.SERVER : OwnerKind.TEAM;
        if (owner.kind() == legacyKind) paid.remove(key(owner.id(), stage));
        setDirty();
        return true;
    }

    public Optional<ActorPurchase> getActorPurchase(OwnerRef owner, StageId stage) {
        return Optional.ofNullable(actorPaid.get(new PurchaseKey(owner, stage)));
    }

    /** Move the exact purchase to its payer queue before attempting delivery. */
    public Optional<ActorPurchase> deferActorRefund(OwnerRef owner, StageId stage) {
        ActorPurchase purchase = actorPaid.remove(new PurchaseKey(owner, stage));
        if (purchase == null) return Optional.empty();
        actorPending.put(purchase.receiptId(), purchase);
        setDirty();
        return Optional.of(purchase);
    }

    public List<ActorPurchase> getPendingActorRefunds(UUID payer) {
        return actorPending.values().stream().filter(purchase -> purchase.payer().equals(payer))
            .sorted(java.util.Comparator.comparing(ActorPurchase::receiptId)).toList();
    }

    public boolean consumeActorRefund(UUID receiptId, UUID payer) {
        ActorPurchase purchase = actorPending.get(receiptId);
        if (purchase == null || !purchase.payer().equals(payer)) return false;
        actorPending.remove(receiptId);
        setDirty();
        return true;
    }

    private static String key(UUID teamId, StageId stageId) {
        return teamId.toString() + "|" + stageId.toString();
    }

    public void markPaid(UUID teamId, StageId stageId) {
        if (paid.add(key(teamId, stageId))) setDirty();
    }

    public boolean isPaid(UUID teamId, StageId stageId) {
        return paid.contains(key(teamId, stageId));
    }

    /** Consume the paid flag (call when refunding) so a stage is refunded at most once per purchase. */
    public boolean consumePaid(UUID teamId, StageId stageId) {
        boolean removed = paid.remove(key(teamId, stageId));
        if (removed) setDirty();
        return removed;
    }

    /** Move a paid flag into the offline-refund queue. */
    public boolean deferRefund(UUID teamId, StageId stageId) {
        String key = key(teamId, stageId);
        if (!paid.remove(key)) return false;
        pendingRefunds.add(key);
        setDirty();
        return true;
    }

    public Set<StageId> getPendingRefunds(UUID teamId) {
        String prefix = teamId + "|";
        Set<StageId> result = new HashSet<>();
        for (String entry : pendingRefunds) {
            if (!entry.startsWith(prefix)) continue;
            try { result.add(StageId.parse(entry.substring(prefix.length()))); }
            catch (IllegalArgumentException ignored) {}
        }
        return Set.copyOf(result);
    }

    public boolean consumePendingRefund(UUID teamId, StageId stageId) {
        boolean removed = pendingRefunds.remove(key(teamId, stageId));
        if (removed) setDirty();
        return removed;
    }

    public static StagePurchaseData load(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("purchase_schema") && (!tag.contains("purchase_schema", Tag.TAG_INT)
                || tag.getInt("purchase_schema") < 0 || tag.getInt("purchase_schema") > 1)) {
            throw new IllegalArgumentException("Unsupported stage purchase schema");
        }
        if ((tag.contains("actor_paid") || tag.contains("actor_pending_refunds")) && tag.getInt("purchase_schema") != 1) {
            throw new IllegalArgumentException("Actor purchases require a schema version");
        }
        StagePurchaseData d = new StagePurchaseData();
        for (String field : List.of("paid", "pending_refunds")) {
            if (tag.contains(field) && (!(tag.get(field) instanceof ListTag entries)
                    || !entries.isEmpty() && entries.getElementType() != Tag.TAG_STRING)) {
                throw new IllegalArgumentException("Invalid legacy stage purchase list. " + field);
            }
        }
        net.minecraft.nbt.ListTag list = tag.getList("paid", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) d.paid.add(list.getString(i));
        net.minecraft.nbt.ListTag pending = tag.getList("pending_refunds", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < pending.size(); i++) d.pendingRefunds.add(pending.getString(i));
        Set<UUID> receipts = new HashSet<>();
        for (ActorPurchase purchase : readActorPurchases(tag, "actor_paid")) {
            if (!receipts.add(purchase.receiptId())
                    || d.actorPaid.putIfAbsent(new PurchaseKey(purchase.owner(), purchase.stage()), purchase) != null) {
                throw new IllegalArgumentException("Duplicate stage purchase receipt or owner");
            }
        }
        for (ActorPurchase purchase : readActorPurchases(tag, "actor_pending_refunds")) {
            if (!receipts.add(purchase.receiptId())) throw new IllegalArgumentException("Duplicate stage purchase receipt");
            d.actorPending.put(purchase.receiptId(), purchase);
        }
        return d;
    }

    private static List<ActorPurchase> readActorPurchases(CompoundTag tag, String field) {
        if (!tag.contains(field)) return List.of();
        if (!(tag.get(field) instanceof ListTag list) || !list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND) {
            throw new IllegalArgumentException("Invalid stage purchase list. " + field);
        }
        List<ActorPurchase> result = new ArrayList<>();
        for (Tag entry : list) {
            CompoundTag record = (CompoundTag) entry;
            for (String key : List.of("receipt", "owner_kind", "owner", "stage", "payer")) {
                if (!record.contains(key, Tag.TAG_STRING)) throw new IllegalArgumentException("Invalid purchase field. " + key);
            }
            if (!record.contains("xp_levels", Tag.TAG_INT) || record.getInt("xp_levels") < 0
                    || !record.contains("refund_percent", Tag.TAG_INT) || record.getInt("refund_percent") < 0
                    || record.getInt("refund_percent") > 100 || !(record.get("items") instanceof ListTag items)
                    || !items.isEmpty() && items.getElementType() != Tag.TAG_COMPOUND) {
                throw new IllegalArgumentException("Invalid stage purchase cost");
            }
            List<StageCost.ItemCost> costs = new ArrayList<>();
            for (Tag item : items) {
                CompoundTag value = (CompoundTag) item;
                if (!value.contains("item", Tag.TAG_STRING) || !value.contains("count", Tag.TAG_INT)
                        || value.getInt("count") <= 0) throw new IllegalArgumentException("Invalid stage purchase item");
                costs.add(new StageCost.ItemCost(ResourceLocation.parse(value.getString("item")), value.getInt("count")));
            }
            OwnerRef owner = new OwnerRef(OwnerKind.valueOf(record.getString("owner_kind")), parseUuid(record, "owner"));
            UUID payer = parseUuid(record, "payer");
            if (owner.kind() == OwnerKind.PERSONAL && !owner.id().equals(payer)) {
                throw new IllegalArgumentException("Invalid personal purchase payer");
            }
            result.add(new ActorPurchase(parseUuid(record, "receipt"), owner, StageId.parse(record.getString("stage")), payer,
                new StageCost(record.getInt("xp_levels"), costs, false, 0, record.getInt("refund_percent"))));
        }
        return result;
    }

    private static UUID parseUuid(CompoundTag record, String key) {
        String value = record.getString(key);
        UUID parsed = UUID.fromString(value);
        if (!parsed.toString().equals(value)) throw new IllegalArgumentException("Invalid purchase identity. " + key);
        return parsed;
    }

    private static ListTag writeActorPurchases(java.util.Collection<ActorPurchase> purchases) {
        ListTag list = new ListTag();
        purchases.stream().sorted(java.util.Comparator.comparing(ActorPurchase::receiptId)).forEach(purchase -> {
            CompoundTag record = new CompoundTag();
            record.putString("receipt", purchase.receiptId().toString());
            record.putString("owner_kind", purchase.owner().kind().name());
            record.putString("owner", purchase.owner().id().toString());
            record.putString("stage", purchase.stage().toString());
            record.putString("payer", purchase.payer().toString());
            record.putInt("xp_levels", purchase.cost().xpLevels());
            record.putInt("refund_percent", purchase.cost().refundPercent());
            ListTag items = new ListTag();
            for (StageCost.ItemCost item : purchase.cost().items()) {
                CompoundTag value = new CompoundTag();
                value.putString("item", item.item().toString());
                value.putInt("count", item.count());
                items.add(value);
            }
            record.put("items", items);
            list.add(record);
        });
        return list;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (String k : paid) list.add(net.minecraft.nbt.StringTag.valueOf(k));
        tag.put("paid", list);
        net.minecraft.nbt.ListTag pending = new net.minecraft.nbt.ListTag();
        for (String k : pendingRefunds) pending.add(net.minecraft.nbt.StringTag.valueOf(k));
        tag.put("pending_refunds", pending);
        tag.putInt("purchase_schema", 1);
        tag.put("actor_paid", writeActorPurchases(actorPaid.values()));
        tag.put("actor_pending_refunds", writeActorPurchases(actorPending.values()));
        return tag;
    }
}
