package com.enviouse.progressivestages.common.rehaul.client;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.lock.PrefixEntry;
import com.enviouse.progressivestages.common.rehaul.CompiledSnapshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record InteractionPrediction(boolean enabled, List<Rule> rules) {
    public static final String CAPABILITY = "interaction_prediction";
    public static final InteractionPrediction EMPTY = new InteractionPrediction(false, List.of());
    private static final int MAGIC = 0x50534931;

    public InteractionPrediction {
        rules = List.copyOf(rules);
    }

    public static InteractionPrediction from(CompiledSnapshot snapshot, boolean enabled) {
        List<Rule> rules = new ArrayList<>();
        snapshot.stages().values().stream().sorted(java.util.Comparator.comparing(stage -> stage.id()))
            .forEach(stage -> {
                for (var interaction : stage.compatibilityView().getLocks().interactions()) {
                    boolean itemRequired = interaction.type().equals("item_on_block");
                    if (!itemRequired && !interaction.type().equals("block_right_click")) continue;
                    PrefixEntry held = selector(itemRequired ? interaction.heldItem() : "all:*");
                    PrefixEntry target = selector(interaction.target());
                    if (held != null && target != null) {
                        rules.add(new Rule(stage.id(), itemRequired, held, target));
                    }
                }
            });
        return new InteractionPrediction(enabled, rules);
    }

    public boolean denies(ItemStack stack, Block block, Predicate<StageId> hasStage) {
        if (!enabled) return false;
        var blockId = BuiltInRegistries.BLOCK.getKey(block);
        var blockHolder = BuiltInRegistries.BLOCK.wrapAsHolder(block);
        for (Rule rule : rules) {
            if (rule.itemRequired() && (stack.isEmpty()
                    || !rule.held().matches(BuiltInRegistries.ITEM.getKey(stack.getItem()),
                        stack.getItemHolder(), PrefixEntry.Keys.ITEM))) continue;
            if (rule.target().matches(blockId, blockHolder, PrefixEntry.Keys.BLOCK)
                    && !hasStage.test(rule.stage())) return true;
        }
        return false;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(enabled);
        out.writeInt(rules.size());
        for (Rule rule : rules) {
            text(out, rule.stage().toString());
            out.writeBoolean(rule.itemRequired());
            text(out, rule.held().raw());
            text(out, rule.target().raw());
        }
    }

    public static void writeFooter(DataOutputStream out, int bytes) throws IOException {
        out.writeInt(bytes);
        out.writeInt(MAGIC);
    }

    public static InteractionPrediction read(byte[] snapshot) {
        if (snapshot.length < 13 || snapshot.length > ClientSnapshotCodec.MAX_SNAPSHOT_BYTES) {
            throw new IllegalArgumentException("Interaction prediction snapshot size is invalid");
        }
        ByteBuffer footer = ByteBuffer.wrap(snapshot, snapshot.length - 8, 8);
        int length = footer.getInt();
        if (footer.getInt() != MAGIC || length < 5 || length > snapshot.length - 8) {
            throw new IllegalArgumentException("Interaction prediction snapshot footer is invalid");
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(snapshot,
                snapshot.length - 8 - length, length));
            boolean enabled = in.readBoolean();
            int count = in.readInt();
            if (count < 0 || count > in.available() / 16) {
                throw new IllegalArgumentException("Interaction prediction rule count is invalid");
            }
            List<Rule> rules = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                StageId stage = StageId.parse(text(in));
                boolean itemRequired = in.readBoolean();
                PrefixEntry held = selector(text(in));
                PrefixEntry target = selector(text(in));
                if (held == null || target == null) {
                    throw new IllegalArgumentException("Interaction prediction selector is invalid");
                }
                rules.add(new Rule(stage, itemRequired, held, target));
            }
            if (in.available() != 0) throw new IllegalArgumentException("Interaction prediction has trailing data");
            return new InteractionPrediction(enabled, rules);
        } catch (IOException error) {
            throw new IllegalArgumentException("Interaction prediction snapshot is incomplete", error);
        }
    }

    private static PrefixEntry selector(String value) {
        return PrefixEntry.parse(value == null || value.isBlank() || value.equals("*") ? "all:*" : value);
    }

    private static void text(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String text(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > in.available()) {
            throw new IllegalArgumentException("Interaction prediction string length is invalid");
        }
        return new String(in.readNBytes(length), StandardCharsets.UTF_8);
    }

    public record Rule(StageId stage, boolean itemRequired, PrefixEntry held, PrefixEntry target) {}
}
