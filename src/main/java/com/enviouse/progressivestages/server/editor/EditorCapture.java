package com.enviouse.progressivestages.server.editor;

import com.enviouse.progressivestages.common.stage.StageCapabilities;
import com.enviouse.progressivestages.server.enforcement.EditorCaptureRecord;
import com.enviouse.progressivestages.server.enforcement.InteractionCaptureManager;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Map;

final class EditorCapture {
    private final InteractionCaptureManager.EditorOperation operation;
    private final String action;
    private final long requestedRevision;
    private final long beforeRevision;
    private final Map<String, String> before;

    private EditorCapture(InteractionCaptureManager.EditorOperation operation, String action,
                          long requestedRevision, EditorDraft draft) {
        this.operation = operation;
        this.action = switch (action) {
            case "bootstrap", "catalog", "mutate", "undo", "redo", "validate", "review", "apply",
                 "rollback", "scaffold", "duplicate_stage", "delete_stage", "rename_stage", "move_stage",
                 "archive_stage", "restore_stage", "export_stage", "import_stage", "collaborator_add",
                 "collaborator_remove", "simulate", "close" -> action;
            default -> "unknown";
        };
        this.requestedRevision = requestedRevision;
        this.beforeRevision = draft.revision();
        this.before = draft.files();
    }

    static EditorCapture begin(ServerPlayer player, EditorDraft draft, String action, JsonObject request) {
        var operation = InteractionCaptureManager.beginEditor(player);
        if (operation == null) return null;
        try {
            long revision = request.has("revision") && request.get("revision").isJsonPrimitive()
                && request.getAsJsonPrimitive("revision").isNumber() ? request.get("revision").getAsLong() : -1;
            return new EditorCapture(operation, action, revision, draft);
        } catch (RuntimeException failure) {
            operation.fail();
            return null;
        }
    }

    void finish(ServerPlayer player, EditorDraft draft, Object response, String failureCode) {
        try {
            String code = failureCode;
            DraftValidation validation = response instanceof DraftValidation result ? result : null;
            String provider = "not_observed";
            if (response instanceof EditorApplyResult applied) {
                code = applied.code();
                validation = applied.validation();
            } else if (response instanceof Map<?, ?> values) {
                if (values.get("validation") instanceof DraftValidation result) validation = result;
                if (values.containsKey("error")) code = "unknown_action";
                if (values.get("capabilities") instanceof StageCapabilities capabilities) {
                    provider = capabilities.luckPerms().name().toLowerCase(Locale.ROOT);
                }
            }
            String reason = switch (code) {
                case "draft_conflict", "configuration_conflict" -> "stale_revision";
                case "validation_failed" -> "invalid_field";
                case "apply_failed", "reload_failed", "rollback_failed", "rollback_reload_failed" -> "apply_failed";
                case "ok" -> "applied";
                case "" -> validation == null ? (beforeRevision == draft.revision() ? "source_preserved" : "draft_changed")
                    : !validation.valid() ? "invalid_field" : "validated";
                default -> "source_preserved";
            };
            if (code.isEmpty()) code = validation == null ? "not_validated" : validation.valid() ? "valid" : "invalid";
            operation.complete(new EditorCaptureRecord(action, requestedRevision, beforeRevision,
                draft.revision(), draft.baseConfigurationRevision(),
                StageFileLoader.getInstance().getCompiledSnapshot().revision(), player.getServer().getTickCount(),
                reason, code, provider, before, draft.files()));
        } catch (RuntimeException failure) {
            operation.fail();
        }
    }
}
