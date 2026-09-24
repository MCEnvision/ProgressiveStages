package com.enviouse.progressivestages.server.editor;

import java.util.List;

public record EditorApplyResult(boolean success, String transactionId, long configurationRevision,
                                List<DraftDiffEntry> diff, DraftValidation validation,
                                String code, String explanation, boolean restartRequired,
                                List<String> pendingRestartSettings) {
    public EditorApplyResult(boolean success, String transactionId, long configurationRevision,
                             List<DraftDiffEntry> diff, DraftValidation validation,
                             String code, String explanation) {
        this(success, transactionId, configurationRevision, diff, validation, code, explanation,
            false, List.of());
    }

    public EditorApplyResult {
        diff = diff == null ? List.of() : List.copyOf(diff);
        code = code == null ? "" : code;
        explanation = explanation == null ? "" : explanation;
        pendingRestartSettings = pendingRestartSettings == null ? List.of() : List.copyOf(pendingRestartSettings);
    }
}
