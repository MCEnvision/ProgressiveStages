package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.StageId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Immutable effective stage view for one actor. */
public record EffectiveStageSnapshot(UUID actorId, long revision, Set<StageId> stages,
                                     Map<StageId, Set<StageSourceKind>> sources) {
    public EffectiveStageSnapshot {
        stages = stages == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(stages));
        if (sources == null) {
            sources = Map.of();
        } else {
            Map<StageId, Set<StageSourceKind>> copy = new LinkedHashMap<>();
            sources.forEach((stage, kinds) -> copy.put(stage,
                kinds == null ? Set.of() : Set.copyOf(kinds)));
            sources = Collections.unmodifiableMap(copy);
        }
        if (revision < 0) throw new IllegalArgumentException("revision must not be negative");
    }

    public boolean contains(StageId stageId) { return stages.contains(stageId); }
}
