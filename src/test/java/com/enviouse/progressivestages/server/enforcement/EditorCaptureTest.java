package com.enviouse.progressivestages.server.enforcement;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditorCaptureTest {
    @TempDir Path directory;
    private final java.util.List<InteractionCaptureManager.Capture> captures = new java.util.ArrayList<>();
    private final java.util.List<InteractionCaptureManager.EditorOperation> operations = new java.util.ArrayList<>();

    @AfterEach
    void finishWriters() {
        operations.forEach(InteractionCaptureManager.EditorOperation::fail);
        for (var capture : captures) {
            capture.stop(InteractionCaptureManager.StopReason.MANUAL);
            capture.startWriter();
            await(capture);
        }
    }

    private InteractionCaptureManager.EditorOperation begin(InteractionCaptureManager.Capture capture, long tick) {
        var operation = capture.beginEditor(tick);
        if (operation != null) operations.add(operation);
        return operation;
    }

    @Test
    void recordsOneBoundedFieldDiagnosticWithoutFilePathsOrMessages() {
        var diagnostic = new EditorCaptureRecord.Diagnostic("identity", "luckperms.inbound[63].permissions",
            "chef_rank", com.enviouse.progressivestages.common.stage.FieldDiagnostic.Severity.ERROR, "invalid_type");
        var record = new EditorCaptureRecord("apply", 2, 2, 2, 4, 4, 101, "invalid_field",
            "validation_failed", "not_observed", Map.of("private/stage.toml", "private source"),
            Map.of("private/stage.toml", "private source"), diagnostic, 3);
        String line = record.line("capture", 1, 100);
        var output = JsonParser.parseString(line).getAsJsonObject();
        assertEquals("luckperms.inbound[63].permissions", output.get("field").getAsString());
        assertEquals("chef_rank", output.get("rule_id").getAsString());
        assertEquals("ERROR", output.get("severity").getAsString());
        assertEquals("invalid_type", output.get("validation_code").getAsString());
        assertEquals("validation_failed", output.get("operation_code").getAsString());
        assertEquals(3, output.get("diagnostic_count").getAsInt());
        assertTrue(output.get("diagnostics_truncated").getAsBoolean());
        assertFalse(line.contains("private"));
        assertTrue(line.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= EditorCaptureRecord.MAX_BYTES);
        var invalid = new EditorCaptureRecord.Diagnostic("private/path", "private/path", "private\nsource",
            com.enviouse.progressivestages.common.stage.FieldDiagnostic.Severity.WARNING, "private source");
        assertEquals("package", invalid.fileRole());
        assertEquals("draft", invalid.field());
        assertEquals("", invalid.ruleId());
        assertEquals("invalid", invalid.code());
    }

    @Test
    void reservedOperationSurvivesReloadButNewOperationsReject() throws Exception {
        var capture = capture("editor");
        var operation = begin(capture, 100);
        assertNotNull(operation);
        capture.startWriter();
        capture.stop(InteractionCaptureManager.StopReason.RELOAD);
        assertNull(capture.beginEditor(101));
        assertFalse(capture.canReplace());
        operation.complete(record(Map.of("stage.toml", "before"), Map.of("stage.toml", "after")));
        await(capture);
        var output = JsonParser.parseString(Files.readString(capture.status().output())).getAsJsonObject();
        assertEquals("applied", output.get("reason").getAsString());
        assertEquals(4, output.get("definition_revision").getAsLong());
        assertEquals("reload", capture.status().stopReason());
        assertEquals(1, capture.status().records());
        assertEquals(Files.size(capture.status().output()), capture.status().bytes());
        assertEquals("drained", capture.status().outputState());
    }

    @Test
    void snapshotIsImmutableAndNeverWritesSourceOrPaths() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("private/path.toml", "private source content");
        var record = record(source, source);
        source.clear();
        String line = record.line("capture", 1, 100);
        var output = JsonParser.parseString(line).getAsJsonObject();
        assertFalse(line.contains("private"));
        assertEquals(64, output.get("source_digest_before").getAsString().length());
        assertEquals(output.get("source_digest_before"), output.get("source_digest_after"));
        assertTrue(output.get("source_preserved").getAsBoolean());
        assertNotEquals(output.get("source_digest_before").getAsString(), ConfigurationFingerprint.of(Map.of()));
    }

    @Test
    void sharedBudgetRejectsReservationsBeforeOverflow() throws Exception {
        var capture = capture("editor");
        for (int i = 0; i < InteractionCaptureManager.MAX_OUTPUT_BYTES / EditorCaptureRecord.MAX_BYTES; i++) {
            var operation = begin(capture, 100 + i * 20L);
            assertNotNull(operation);
            operation.complete(record(Map.of(), Map.of()));
        }
        assertEquals("byte_limit", capture.status().stopReason());
        assertNull(capture.beginEditor(1000));
        capture.startWriter();
        await(capture);
        assertTrue(Files.size(capture.status().output()) <= InteractionCaptureManager.MAX_OUTPUT_BYTES);
        assertEquals(Files.size(capture.status().output()), capture.status().bytes());
    }

    @Test
    void failedObservationStopsSafelyAndOtherCategoriesNeverReserveEditorRecords() throws Exception {
        var other = capture("permissions");
        assertNull(other.beginEditor(100));
        other.stop(InteractionCaptureManager.StopReason.MANUAL);
        other.startWriter();
        await(other);
        var capture = capture("editor");
        var operation = begin(capture, 100);
        operation.fail();
        capture.startWriter();
        await(capture);
        assertEquals("output_error", capture.status().stopReason());
        assertEquals(1, capture.status().dropped());
        assertEquals("failed", capture.status().outputState());
        assertFalse(capture.status().active());
    }

    private InteractionCaptureManager.Capture capture(String category) {
        String id = UUID.randomUUID().toString();
        var capture = new InteractionCaptureManager.Capture(id, UUID.randomUUID(), category,
            directory.resolve(id + ".log"), 100);
        captures.add(capture);
        return capture;
    }

    private static EditorCaptureRecord record(Map<String, String> before, Map<String, String> after) {
        return new EditorCaptureRecord("apply", 2, 2, 2, 4, 4, 101,
            "applied", "ok", "not_observed", before, after);
    }

    private static void await(InteractionCaptureManager.Capture capture) {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            while (!capture.canReplace()) Thread.sleep(5);
        });
    }
}
