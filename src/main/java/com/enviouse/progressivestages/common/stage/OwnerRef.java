package com.enviouse.progressivestages.common.stage;

import java.util.Objects;
import java.util.UUID;

/** Immutable identity of the record that owns a stage. */
public record OwnerRef(OwnerKind kind, UUID id) {
    public OwnerRef {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
    }
}
