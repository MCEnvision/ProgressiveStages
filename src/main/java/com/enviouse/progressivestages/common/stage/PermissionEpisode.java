package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.StageId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

/** Durable eligibility history independent of the lifetime of a source contribution. */
public record PermissionEpisode(OwnerRef owner, StageId stage, UUID subject, String row,
                                String observation, boolean positive, boolean suppressed,
                                long acquiredAt, long expiresAt) {
    public static final Codec<PermissionEpisode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("owner_kind").forGetter(value -> value.owner().kind().name()),
        Codec.STRING.fieldOf("owner_id").forGetter(value -> value.owner().id().toString()),
        ResourceLocation.CODEC.fieldOf("stage").forGetter(value -> value.stage().getResourceLocation()),
        Codec.STRING.fieldOf("subject").forGetter(value -> value.subject().toString()),
        Codec.STRING.fieldOf("row").forGetter(PermissionEpisode::row),
        Codec.STRING.fieldOf("observation").forGetter(PermissionEpisode::observation),
        Codec.BOOL.fieldOf("positive").forGetter(PermissionEpisode::positive),
        Codec.BOOL.fieldOf("suppressed").forGetter(PermissionEpisode::suppressed),
        Codec.LONG.fieldOf("acquired_at").forGetter(PermissionEpisode::acquiredAt),
        Codec.LONG.fieldOf("expires_at").forGetter(PermissionEpisode::expiresAt)
    ).apply(instance, (kind, owner, stage, subject, row, observation, positive, suppressed, acquiredAt, expiresAt) ->
        new PermissionEpisode(new OwnerRef(OwnerKind.valueOf(kind), UUID.fromString(owner)),
            StageId.fromResourceLocation(stage), UUID.fromString(subject), row, observation,
            positive, suppressed, acquiredAt, expiresAt)));

    public PermissionEpisode {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(stage);
        new PermissionStageSource(subject, row, false);
        if (observation == null || !observation.matches("[a-f0-9]{64}|")) {
            throw new IllegalArgumentException("Invalid permission episode observation");
        }
        if (acquiredAt < 0 || expiresAt < 0 || expiresAt != 0 && (acquiredAt == 0 || expiresAt < acquiredAt)) {
            throw new IllegalArgumentException("Invalid permission episode clock");
        }
        if (owner.kind() == OwnerKind.SERVER && !owner.id().equals(new UUID(0, 0))) {
            throw new IllegalArgumentException("Invalid permission episode server owner");
        }
    }

    public boolean expired(long now) { return expiresAt != 0 && now >= expiresAt; }

    public PermissionEpisode suppress() {
        return positive && !suppressed
            ? new PermissionEpisode(owner, stage, subject, row, observation, true, true, acquiredAt, expiresAt) : this;
    }

    public PermissionEpisode observe(String fingerprint, boolean eligible) {
        if (!eligible) {
            // A different context or rule is not evidence that the original eligibility was lost.
            return positive && observation.equals(fingerprint)
                ? new PermissionEpisode(owner, stage, subject, row, observation, false, false, acquiredAt, expiresAt) : this;
        }
        if (positive) {
            return observation.isEmpty() || acquiredAt == 0 && !suppressed
                ? new PermissionEpisode(owner, stage, subject, row, fingerprint, true, suppressed, acquiredAt, expiresAt) : this;
        }
        return observation.equals(fingerprint)
            ? new PermissionEpisode(owner, stage, subject, row, fingerprint, true, false, 0, 0) : this;
    }

    public PermissionEpisode acquire(long now, long existingGrantTime, long duration) {
        if (!positive || suppressed || acquiredAt != 0) return this;
        long start = existingGrantTime > 0 ? existingGrantTime : now;
        long expiry = duration <= 0 ? 0 : start > Long.MAX_VALUE - duration ? Long.MAX_VALUE : start + duration;
        return new PermissionEpisode(owner, stage, subject, row, observation, true, false, start, expiry);
    }

    public PermissionEpisode administrativeGrant() {
        return new PermissionEpisode(owner, stage, subject, row, "", true, false, 0, 0);
    }
}
