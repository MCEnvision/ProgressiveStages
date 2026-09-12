package com.enviouse.progressivestages.common.api;

import java.util.List;

/**
 * The authoritative result of evaluating one server interaction.
 *
 * <p>The matched and missing stages are immutable so the same result can be
 * used by enforcement, player feedback, and diagnostics without re-evaluating
 * selectors or player state.
 */
public record InteractionDecision(List<StageId> matchedStages, List<StageId> missingStages,
                                 Reason reason, boolean allowed) {

    public enum Reason {
        NO_RULE,
        SELECTOR_MISMATCH,
        STAGE_MISSING,
        STAGE_OWNED,
        BYPASS
    }

    public InteractionDecision {
        matchedStages = matchedStages == null ? List.of() : List.copyOf(matchedStages);
        missingStages = missingStages == null ? List.of() : List.copyOf(missingStages);
        reason = reason == null ? Reason.NO_RULE : reason;
    }
}
