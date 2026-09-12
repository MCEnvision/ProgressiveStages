package com.enviouse.progressivestages.server.enforcement;

import com.google.gson.JsonObject;
import com.enviouse.progressivestages.common.stage.FieldDiagnostic;

import java.util.Map;

public record EditorCaptureRecord(String action, long requestedRevision, long beforeRevision,
                                  long draftRevision, long applyRevision, long definitionRevision,
                                  long completedTick, String reason, String validationCode,
                                  String providerState, Map<String, String> before, Map<String, String> after,
                                  Diagnostic diagnostic, int diagnosticCount) {
    static final int MAX_BYTES = 4096;

    public EditorCaptureRecord(String action, long requestedRevision, long beforeRevision,
                               long draftRevision, long applyRevision, long definitionRevision,
                               long completedTick, String reason, String validationCode,
                               String providerState, Map<String, String> before, Map<String, String> after) {
        this(action, requestedRevision, beforeRevision, draftRevision, applyRevision, definitionRevision,
            completedTick, reason, validationCode, providerState, before, after, null, 0);
    }

    public EditorCaptureRecord {
        diagnosticCount = Math.max(0, diagnosticCount);
        before = Map.copyOf(before);
        after = Map.copyOf(after);
    }

    String line(String captureId, int sequence, long startedTick) {
        JsonObject record = new JsonObject();
        record.addProperty("capture_id", captureId);
        record.addProperty("candidate_identity", captureId);
        record.addProperty("sequence", sequence);
        record.addProperty("server_tick", startedTick);
        record.addProperty("completed_tick", completedTick);
        record.addProperty("side", "server");
        record.addProperty("category", "editor");
        record.addProperty("target", "selected");
        record.addProperty("owner_label", "target");
        record.addProperty("action", InteractionCaptureManager.bounded(action));
        record.addProperty("draft_revision_before", beforeRevision);
        record.addProperty("draft_revision", draftRevision);
        record.addProperty("apply_revision", applyRevision);
        record.addProperty("definition_revision", definitionRevision);
        record.addProperty("desired", requestedRevision);
        record.addProperty("actual", draftRevision);
        record.addProperty("file_role", diagnostic == null ? "package" : diagnostic.fileRole());
        record.addProperty("field", diagnostic == null ? "draft" : diagnostic.field());
        record.addProperty("rule_id", diagnostic == null ? "" : diagnostic.ruleId());
        record.addProperty("severity", diagnostic == null ? "NONE" : diagnostic.severity().name());
        record.addProperty("operation_code", InteractionCaptureManager.bounded(validationCode));
        record.addProperty("validation_code", diagnostic == null ? InteractionCaptureManager.bounded(validationCode) : diagnostic.code());
        record.addProperty("diagnostic_count", diagnosticCount);
        record.addProperty("diagnostics_truncated", diagnosticCount > (diagnostic == null ? 0 : 1));
        record.addProperty("reason", InteractionCaptureManager.bounded(reason));
        record.addProperty("provider_state", InteractionCaptureManager.bounded(providerState));
        record.addProperty("source_digest_schema", ConfigurationFingerprint.SCHEMA);
        record.addProperty("source_digest_before", ConfigurationFingerprint.of(before));
        record.addProperty("source_digest_after", ConfigurationFingerprint.of(after));
        record.addProperty("source_preserved", before.equals(after));
        record.addProperty("latency_ticks", Math.max(0, completedTick - startedTick));
        return record + "\n";
    }

    public record Diagnostic(String fileRole, String field, String ruleId, FieldDiagnostic.Severity severity, String code) {
        public Diagnostic {
            fileRole = switch (fileRole == null ? "" : fileRole) {
                case "identity", "rules", "progression", "legacy" -> fileRole;
                default -> "package";
            };
            field = field != null && field.matches("[A-Za-z0-9_.\\[\\]]{1,160}") ? field : "draft";
            ruleId = ruleId != null && ruleId.matches("[A-Za-z0-9_.]{1,64}") ? ruleId : "";
            severity = java.util.Objects.requireNonNull(severity, "severity");
            code = code != null && code.matches("[a-z_]{1,64}") ? code : "invalid";
        }
    }
}
