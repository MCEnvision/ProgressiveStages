package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.lock.LockRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * v2.5: drives advancement HIDING via {@code [advancements].locked}.
 *
 * <p>The actual filtering happens in {@code ServerAdvancementHidingMixin}, which strips gated
 * advancements (whose stage the player lacks) from every {@code ClientboundUpdateAdvancementsPacket}
 * before it reaches the client — so they never appear in the advancements screen. This helper just
 * forces a fresh full re-send when a player's stages change, so advancements that became reachable
 * pop into view (and revoked ones disappear) without a relog. No-op unless some stage gates an
 * advancement.
 */
public final class AdvancementHider {

    private AdvancementHider() {}

    public static net.minecraft.advancements.AdvancementHolder filterToast(ServerPlayer player,
            net.minecraft.advancements.AdvancementHolder holder) {
        var value = holder.value();
        var display = value.display().orElse(null);
        if (display == null || !display.shouldShowToast()
                || !LockRegistry.getInstance().isAdvancementToastHiddenFor(player, holder.id())) return holder;
        var filtered = new net.minecraft.advancements.DisplayInfo(display.getIcon(), display.getTitle(),
            display.getDescription(), display.getBackground(), display.getType(), false,
            display.shouldAnnounceChat(), display.isHidden());
        filtered.setLocation(display.getX(), display.getY());
        return new net.minecraft.advancements.AdvancementHolder(holder.id(), new net.minecraft.advancements.Advancement(
            value.parent(), java.util.Optional.of(filtered), value.rewards(), value.criteria(), value.requirements(),
            value.sendsTelemetryEvent(), value.name()));
    }

    public static ClientboundUpdateAdvancementsPacket filterUpdate(ServerPlayer player,
            ClientboundUpdateAdvancementsPacket adv, Set<ResourceLocation> suppressedToasts) {
        LockRegistry reg = LockRegistry.getInstance();
        if (adv.shouldReset()) suppressedToasts.clear();
        suppressedToasts.removeAll(adv.getRemoved());
        boolean changed = false;

        List<AdvancementHolder> keptAdded = new ArrayList<>(adv.getAdded().size());
        for (AdvancementHolder h : adv.getAdded()) {
            if (reg.isAdvancementHiddenFor(player, h.id())) { changed = true; continue; }
            var filtered = filterToast(player, h);
            if (filtered != h) suppressedToasts.add(h.id());
            else suppressedToasts.remove(h.id());
            changed |= filtered != h;
            keptAdded.add(filtered);
        }

        Map<ResourceLocation, AdvancementProgress> keptProgress = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, AdvancementProgress> e : adv.getProgress().entrySet()) {
            if (reg.isAdvancementHiddenFor(player, e.getKey())) { changed = true; continue; }
            keptProgress.put(e.getKey(), e.getValue());
            if (keptAdded.stream().noneMatch(holder -> holder.id().equals(e.getKey()))) {
                var holder = player.server.getAdvancements().get(e.getKey());
                if (holder != null) {
                    var filtered = filterToast(player, holder);
                    boolean wasSuppressed = suppressedToasts.remove(holder.id());
                    if (filtered != holder) suppressedToasts.add(holder.id());
                    if (filtered != holder || wasSuppressed) {
                        keptAdded.add(filtered);
                        changed = true;
                    }
                }
            }
        }

        if (!changed) return adv;
        return new ClientboundUpdateAdvancementsPacket(
            adv.shouldReset(), keptAdded, adv.getRemoved(), keptProgress);
    }

    public static void resyncIfNeeded(ServerPlayer player) {
        if (player == null || player.server == null) return;
        if (!LockRegistry.getInstance().hasAdvancementLocks()) return;
        // reload() clears in-memory progress and re-reads it from disk, so we save() FIRST to flush
        // any progress earned since the last periodic save — otherwise the reload would discard it.
        // The reload then re-sends the player's whole advancement state, which the mixin re-filters
        // against the now-current stages. Stage changes are infrequent, so this is fine.
        var advancements = player.getAdvancements();
        advancements.save();
        advancements.reload(player.server.getAdvancements());
    }
}
