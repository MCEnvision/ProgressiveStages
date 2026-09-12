package com.enviouse.progressivestages.common.stage;

import java.util.Objects;
import java.util.UUID;

/** Stable actor and owner identity used while resolving a stage mutation. */
public record StageActorContext(UUID actorId, OwnerRef owner,
                                long definitionRevision, long membershipRevision) {
    public StageActorContext {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(owner, "owner");
        if (definitionRevision < 0 || membershipRevision < 0) {
            throw new IllegalArgumentException("revision must not be negative");
        }
    }

    public UUID teamId() { return owner.kind() == OwnerKind.TEAM ? owner.id() : actorId; }
}
