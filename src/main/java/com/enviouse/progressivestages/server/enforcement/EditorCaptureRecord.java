package com.enviouse.progressivestages.server.enforcement;

import com.google.gson.JsonObject;

import java.util.Map;

public record EditorCaptureRecord(String action, long requestedRevision, long beforeRevision,
                                  long draftRevision, long applyRevision, long definitionRevision,
                                  long completedTick, String reason, String validationCode,
                                  String providerState, Map<String, String> before, Map<String, String> after) {
    static final int MAX_BYTES = 4096;

    public EditorCaptureRecord {
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
        record.addProperty("file_role", "package");
        record.addProperty("field", "draft");
        record.addProperty("rule_id", "");
        record.addProperty("validation_code", InteractionCaptureManager.bounded(validationCode));
        record.addProperty("reason", InteractionCaptureManager.bounded(reason));
        record.addProperty("provider_state", InteractionCaptureManager.bounded(providerState));
        record.addProperty("source_digest_schema", ConfigurationFingerprint.SCHEMA);
        record.addProperty("source_digest_before", ConfigurationFingerprint.of(before));
        record.addProperty("source_digest_after", ConfigurationFingerprint.of(after));
        record.addProperty("source_preserved", before.equals(after));
        record.addProperty("latency_ticks", Math.max(0, completedTick - startedTick));
        return record + "\n";
    }
}
