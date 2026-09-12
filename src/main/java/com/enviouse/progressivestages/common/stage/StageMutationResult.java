package com.enviouse.progressivestages.common.stage;

import java.util.Set;
import java.util.UUID;

/** Committed stage mutation summary published to integrations and diagnostics. */
public record StageMutationResult(boolean changed, String reason, long revision,
                                  Set<OwnerRef> affectedOwners, Set<UUID> affectedPlayers) {
    public StageMutationResult {
        reason = reason == null ? "" : reason;
        affectedOwners = affectedOwners == null ? Set.of() : Set.copyOf(affectedOwners);
        affectedPlayers = affectedPlayers == null ? Set.of() : Set.copyOf(affectedPlayers);
        if (revision < 0) throw new IllegalArgumentException("revision must not be negative");
    }
}
