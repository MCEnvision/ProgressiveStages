package com.enviouse.progressivestages.common.stage;

import java.util.Objects;
import java.util.Optional;

/** A field-addressed validation or capability diagnostic shared by server and editor code. */
public record FieldDiagnostic(Severity severity, String file, String field, Optional<String> ruleId,
                              String code, String message) {
    public enum Severity { ERROR, WARNING }

    public FieldDiagnostic {
        severity = Objects.requireNonNull(severity, "severity");
        file = Objects.requireNonNullElse(file, "");
        field = Objects.requireNonNullElse(field, "");
        ruleId = ruleId == null ? Optional.empty() : ruleId;
        code = Objects.requireNonNullElse(code, "");
        message = Objects.requireNonNullElse(message, "");
    }
}
