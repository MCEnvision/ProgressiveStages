package com.enviouse.progressivestages.common.stage;

import java.util.Objects;
import java.util.Optional;

/** Presence-aware ownership settings carried by a stage definition. */
public record StageOwnershipOptions(Scope scope, Optional<Boolean> teamStage) {
    public enum Scope { TEAM, SERVER }

    public StageOwnershipOptions {
        scope = Objects.requireNonNull(scope, "scope");
        teamStage = teamStage == null ? Optional.empty() : teamStage;
        if (scope == Scope.SERVER && teamStage.isPresent()) {
            throw new IllegalArgumentException("server stages cannot declare team_stage");
        }
    }

    public static StageOwnershipOptions from(String scope, Optional<Boolean> teamStage) {
        Scope resolved = "server".equalsIgnoreCase(scope) ? Scope.SERVER : Scope.TEAM;
        return new StageOwnershipOptions(resolved, teamStage);
    }
}
