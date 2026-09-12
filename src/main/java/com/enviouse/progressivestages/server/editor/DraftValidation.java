package com.enviouse.progressivestages.server.editor;

import com.enviouse.progressivestages.common.stage.FieldDiagnostic;
import com.enviouse.progressivestages.common.stage.StageCapabilities;

import java.util.List;

public record DraftValidation(boolean valid, List<String> errors, List<String> warnings,
                              int stages, long validatedRevision, List<Diagnostic> diagnostics,
                              StageCapabilities stageCapabilities, String teamMode) {
    public DraftValidation(boolean valid, List<String> errors, List<String> warnings,
                           int stages, long validatedRevision, List<Diagnostic> diagnostics) {
        this(valid, errors, warnings, stages, validatedRevision, diagnostics, null, null);
    }
    public DraftValidation(boolean valid, List<String> errors, List<String> warnings,
                           int stages, long validatedRevision) {
        this(valid, errors, warnings, stages, validatedRevision, List.of());
    }

    public DraftValidation {
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        errors = errors == null ? List.of() : List.copyOf(errors);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public record Diagnostic(FieldDiagnostic.Severity severity, String file, String field,
                             String ruleId, String code, String message) {
        static Diagnostic from(FieldDiagnostic diagnostic) {
            return new Diagnostic(diagnostic.severity(), diagnostic.file(), diagnostic.field(),
                diagnostic.ruleId().orElse(null), diagnostic.code(), diagnostic.message());
        }
    }
}
