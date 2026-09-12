package com.enviouse.progressivestages.client;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.rehaul.CompiledSnapshot;
import com.enviouse.progressivestages.common.rehaul.client.ClientSnapshotChunk;
import com.enviouse.progressivestages.common.rehaul.client.ClientSnapshotCodec;
import com.enviouse.progressivestages.common.rehaul.client.ClientSnapshotManifest;
import com.enviouse.progressivestages.common.rehaul.client.InteractionPrediction;
import com.enviouse.progressivestages.server.loader.Schema4StageCompiler;
import com.enviouse.progressivestages.server.loader.StagePackageParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClientInteractionPredictionTest {
    private static final StageId STAGE = StageId.parse("test:chef");

    @AfterEach
    void clear() {
        ClientCompiledSnapshotCache.clear();
    }

    @Test
    void breadRulePreservesOtherItemsEmptyHandAndOtherBlocksAcrossGrantAndRevoke() {
        activate(snapshot(1, "item_on_block", "id:minecraft:bread"), true);
        var prediction = ClientCompiledSnapshotCache.interactions();
        Set<StageId> owned = new HashSet<>();
        assertTrue(prediction.denies(new ItemStack(Items.BREAD), Blocks.BARREL, owned::contains));
        assertFalse(prediction.denies(new ItemStack(Items.CARROT), Blocks.BARREL, owned::contains));
        assertFalse(prediction.denies(ItemStack.EMPTY, Blocks.BARREL, owned::contains));
        assertFalse(prediction.denies(new ItemStack(Items.BREAD), Blocks.CHEST, owned::contains));
        owned.add(STAGE);
        assertFalse(prediction.denies(new ItemStack(Items.BREAD), Blocks.BARREL, owned::contains));
        owned.clear();
        assertTrue(prediction.denies(new ItemStack(Items.BREAD), Blocks.BARREL, owned::contains));
    }

    @Test
    void wildcardStillRequiresAnItemAndWholeBlockAccessRemainsIndependent() {
        activate(snapshot(1, "item_on_block", "all:*"), true);
        assertTrue(denies(new ItemStack(Items.IRON_CHESTPLATE)));
        assertFalse(denies(ItemStack.EMPTY));
        activate(snapshot(2, "block_right_click", "all:*"), true);
        assertTrue(denies(ItemStack.EMPTY));
        activate(snapshot(3, "item_on_block", "all:*"), false);
        assertFalse(denies(new ItemStack(Items.BREAD)));
    }

    @Test
    void modernAndLegacyArmorTagsUseLiveHolderMembership() throws ReflectiveOperationException {
        var holder = net.minecraft.core.registries.BuiltInRegistries.ITEM.getHolderOrThrow(
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.parse("minecraft:iron_chestplate")));
        var before = holder.tags().toList();
        var bind = holder.getClass().getDeclaredMethod("bindTags", java.util.Collection.class);
        bind.setAccessible(true);
        var armor = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            net.minecraft.resources.ResourceLocation.parse("c:armors"));
        try {
            bind.invoke(holder, java.util.List.of(armor));
            for (String selector : java.util.List.of("tag:c:armors", "#c:armors")) {
                activate(snapshot(1, "item_on_block", selector), true);
                assertTrue(denies(new ItemStack(Items.IRON_CHESTPLATE)));
                assertFalse(denies(new ItemStack(Items.BREAD)));
                assertFalse(denies(ItemStack.EMPTY));
            }
            bind.invoke(holder, java.util.List.of());
            assertFalse(denies(new ItemStack(Items.IRON_CHESTPLATE)));
        } finally {
            bind.invoke(holder, before);
        }
    }

    @Test
    void partialSnapshotKeepsLastRulesUntilVerifiedReplacementAndDisconnectClearsThem() {
        var old = snapshot(1, "item_on_block", "id:minecraft:bread");
        activate(old, true);
        var next = snapshot(2, "item_on_block", "id:minecraft:carrot");
        byte[] raw = ClientSnapshotCodec.encode(next);
        byte[] compressed = ClientSnapshotCodec.compress(raw);
        var manifest = new ClientSnapshotManifest(2, 4, 2, 1, ClientSnapshotCodec.checksum(raw),
            2, compressed.length, raw.length, Set.of(InteractionPrediction.CAPABILITY), false);
        int split = compressed.length / 2;
        ClientCompiledSnapshotCache.begin(manifest);
        assertFalse(ClientCompiledSnapshotCache.accept(new ClientSnapshotChunk(2, 0,
            java.util.Arrays.copyOfRange(compressed, 0, split))));
        assertTrue(denies(new ItemStack(Items.BREAD)));
        assertFalse(denies(new ItemStack(Items.CARROT)));
        assertTrue(ClientCompiledSnapshotCache.accept(new ClientSnapshotChunk(2, 1,
            java.util.Arrays.copyOfRange(compressed, split, compressed.length))));
        assertFalse(denies(new ItemStack(Items.BREAD)));
        assertTrue(denies(new ItemStack(Items.CARROT)));
        ClientCompiledSnapshotCache.clear();
        assertFalse(denies(new ItemStack(Items.CARROT)));
    }

    @Test
    void deltaRefreshesThePairAndLegacySnapshotRemovesPrediction() {
        var old = snapshot(1, "item_on_block", "id:minecraft:bread");
        activate(old, true);
        var next = snapshot(2, "item_on_block", "id:minecraft:carrot");
        var prepared = ClientSnapshotCodec.prepare(next, 1, ClientSnapshotCodec.encode(old));
        ClientCompiledSnapshotCache.begin(prepared.manifest());
        prepared.chunks().forEach(ClientCompiledSnapshotCache::accept);
        assertFalse(denies(new ItemStack(Items.BREAD)));
        assertTrue(denies(new ItemStack(Items.CARROT)));
        byte[] legacy = new byte[]{1, 2, 3};
        activateRaw(legacy, Set.of());
        assertFalse(denies(new ItemStack(Items.CARROT)));
    }

    @Test
    void malformedCountsLengthsAndFooterCannotReplaceValidRules() {
        var snapshot = snapshot(1, "item_on_block", "all:*");
        activate(snapshot, true);
        byte[] good = ClientSnapshotCodec.encode(snapshot);
        int start = good.length - 8 - ByteBuffer.wrap(good).getInt(good.length - 8);
        for (int offset : new int[]{start + 1, start + 5, good.length - 8}) {
            byte[] bad = good.clone();
            ByteBuffer.wrap(bad).putInt(offset, Integer.MAX_VALUE);
            assertThrows(IllegalArgumentException.class,
                () -> activateRaw(bad, Set.of(InteractionPrediction.CAPABILITY)));
            assertTrue(denies(new ItemStack(Items.BREAD)));
        }
    }

    @Test
    void allGatingStagesAreRequiredForOverlappingPairs() {
        var first = snapshot(1, "item_on_block", "id:minecraft:bread");
        var otherId = StageId.parse("test:merchant");
        var firstStage = first.stages().get(STAGE);
        var otherStage = new com.enviouse.progressivestages.common.rehaul.CompiledStage(otherId,
            "Merchant", "", 0, 4, "test", java.util.List.of(), Map.of(),
            firstStage.compatibilityView(), firstStage.provenance());
        activate(CompiledSnapshot.create(2, Map.of(STAGE, firstStage, otherId, otherStage)), true);
        var prediction = ClientCompiledSnapshotCache.interactions();
        assertTrue(prediction.denies(new ItemStack(Items.BREAD), Blocks.BARREL, Set.of(STAGE)::contains));
        assertFalse(prediction.denies(new ItemStack(Items.BREAD), Blocks.BARREL,
            Set.of(STAGE, otherId)::contains));
    }

    private static boolean denies(ItemStack item) {
        return ClientCompiledSnapshotCache.interactions().denies(item, Blocks.BARREL, stage -> false);
    }

    private static CompiledSnapshot snapshot(long revision, String type, String held) {
        String stage = "[schema]\nversion = 4\n[stage]\nid = \"test:chef\"\nname = \"Chef\"\n";
        String rules = "[[interactions]]\ntype = \"" + type + "\"\nheld_item = \"" + held
            + "\"\ntarget_block = \"id:minecraft:barrel\"\n";
        var parsed = StagePackageParser.parseContents("test", "stage.toml", stage,
            "rules.toml", rules, "progression.toml", "");
        assertTrue(parsed.isSuccess(), parsed.getErrorMessage());
        var compiled = Schema4StageCompiler.compile(parsed.getStageDefinition(), parsed.getSourceConfig(), "test", 0);
        return CompiledSnapshot.create(revision, Map.of(compiled.id(), compiled));
    }

    private static void activate(CompiledSnapshot snapshot, boolean enabled) {
        var prepared = ClientSnapshotCodec.prepare(snapshot, 0, null, enabled);
        ClientCompiledSnapshotCache.begin(prepared.manifest());
        prepared.chunks().forEach(ClientCompiledSnapshotCache::accept);
    }

    private static void activateRaw(byte[] raw, Set<String> capabilities) {
        byte[] compressed = ClientSnapshotCodec.compress(raw);
        ClientCompiledSnapshotCache.begin(new ClientSnapshotManifest(2, 4, 3, 0,
            ClientSnapshotCodec.checksum(raw), 1, compressed.length, raw.length, capabilities, false));
        ClientCompiledSnapshotCache.accept(new ClientSnapshotChunk(3, 0, compressed));
    }
}
