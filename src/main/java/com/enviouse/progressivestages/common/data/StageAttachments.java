package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.util.Constants;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registry for data attachments used by the mod
 */
public class StageAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    /**
     * Attachment for storing team stage data on the server level
     */
    public static final Supplier<AttachmentType<TeamStageData>> TEAM_STAGES = ATTACHMENT_TYPES.register(
        "team_stages",
        () -> AttachmentType.builder(TeamStageData::new)
            .serialize(new IAttachmentSerializer<Tag, TeamStageData>() {
                @Override public TeamStageData read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
                    try {
                        return TeamStageData.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
                    } catch (RuntimeException exception) {
                        com.mojang.logging.LogUtils.getLogger().error(
                            "Stage ownership data is unreadable. Progression mutations are disabled and the original data is retained. Restore a compatible backup.");
                        return TeamStageData.preserveUnreadable(tag);
                    }
                }

                @Override public Tag write(TeamStageData attachment, HolderLookup.Provider provider) {
                    Tag unreadable = attachment.unreadableTag();
                    return unreadable != null ? unreadable
                        : TeamStageData.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment).getOrThrow();
                }
            })
            .copyOnDeath()
            .build()
    );
}
