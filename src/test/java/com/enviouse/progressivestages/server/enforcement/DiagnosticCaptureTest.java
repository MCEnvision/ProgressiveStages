package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.enviouse.progressivestages.server.enforcement.InteractionCaptureManager.*;
import static org.junit.jupiter.api.Assertions.*;

class DiagnosticCaptureTest {
    @TempDir Path directory;
    private final UUID target = UUID.randomUUID();
    private final List<Capture> captures = new ArrayList<>();

    @AfterEach
    void finishWriters() throws Exception {
        for (var capture : captures) {
            capture.stop(StopReason.MANUAL);
            capture.startWriter();
            awaitWriter(capture);
        }
    }

    @Test
    void selectsOnlyTheActiveCategoryAndActor() {
        var capture = capture();
        assertTrue(capture.accepts("permissions", target));
        assertFalse(capture.accepts("interactions", target));
        assertFalse(capture.accepts("permissions", UUID.randomUUID()));
        capture.stop(StopReason.MANUAL);
        assertFalse(capture.accepts("permissions", target));
        capture.recordLine(100, () -> { throw new AssertionError("Stopped capture formatted a record."); });
        assertEquals(0, capture.status().records());
    }

    @Test
    void timeoutUsesOneTickOriginAndStopsBeforeFormatting() {
        var capture = capture();
        capture.checkTimeout(1299);
        assertTrue(capture.status().active());
        assertEquals(1, capture.status().remainingSeconds());
        capture.recordLine(1300, () -> { throw new AssertionError("Expired capture formatted a record."); });
        assertEquals("timeout", capture.status().stopReason());
        assertEquals(0, capture.status().records());
    }

    @Test
    void rateExhaustionStopsImmediatelyAndKeepsAcceptedRecords() {
        var capture = capture();
        for (int i = 0; i < MAX_DECISIONS_PER_SECOND; i++) capture.recordLine(100, () -> "{}\n");
        assertFalse(capture.status().active());
        assertEquals("rate_limit", capture.status().stopReason());
        assertEquals(20, capture.status().records());
        capture.recordLine(120, () -> { throw new AssertionError("Exhausted capture resumed."); });
        assertEquals(60, capture.status().bytes());
    }

    @Test
    void totalSampleLimitAppliesAcrossRateWindows() {
        var capture = capture();
        for (int i = 0; i < MAX_DECISIONS; i++) capture.recordLine(100 + (i / 19) * 20L, () -> "{}\n");
        assertEquals(200, capture.status().records());
        assertEquals("decision_limit", capture.status().stopReason());
        assertTrue(capture.status().queued() <= MAX_QUEUE);
    }

    @Test
    void byteLimitCountsUtf8AndRejectsTheWholeExcessRecord() throws Exception {
        var capture = capture();
        String first = "é".repeat(MAX_OUTPUT_BYTES / 2 - 1);
        capture.recordLine(100, () -> first);
        capture.recordLine(101, () -> "€");
        assertEquals("output_limit", capture.status().stopReason());
        assertEquals(MAX_OUTPUT_BYTES - 2, capture.status().bytes());
        assertEquals(1, capture.status().records());
        capture.startWriter();
        awaitWriter(capture);
        assertArrayEquals(first.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(capture.status().output()));
    }

    @Test
    void exactByteLimitStopsWithoutAnExtraAttempt() {
        var capture = capture();
        capture.recordLine(100, () -> "x".repeat(MAX_OUTPUT_BYTES));
        assertFalse(capture.status().active());
        assertEquals("output_limit", capture.status().stopReason());
        assertEquals(MAX_OUTPUT_BYTES, capture.status().bytes());
    }

    @Test
    void stoppedCaptureCannotBeReplacedUntilOutputCloses() throws Exception {
        var capture = capture();
        capture.recordLine(100, () -> "first\n");
        capture.recordLine(101, () -> "second\n");
        capture.stop(StopReason.MANUAL);
        assertEquals("draining", capture.status().outputState());
        assertFalse(capture.canReplace());
        capture.startWriter();
        awaitWriter(capture);
        assertEquals("drained", capture.status().outputState());
        assertEquals(0, capture.status().queued());
        assertEquals("first\nsecond\n", Files.readString(capture.status().output()));
    }

    @Test
    void lateOutputFailureOverridesManualStopAndPreservesExistingFile() throws Exception {
        var capture = capture();
        Files.writeString(capture.status().output(), "existing data");
        capture.recordLine(100, () -> "must not replace existing data");
        capture.stop(StopReason.MANUAL);
        capture.startWriter();
        awaitWriter(capture);
        assertEquals("output_error", capture.status().stopReason());
        assertEquals("failed", capture.status().outputState());
        assertEquals(1, capture.status().dropped());
        assertEquals("existing data", Files.readString(capture.status().output()));
    }

    @Test
    void encodesControlCharactersAndBoundsDecodedStrings() {
        String value = "quote\" slash\\ tab\t backspace\b formfeed\f newline\n return\r null\u0000 emoji😀";
        String escaped = esc(value);
        assertTrue(escaped.chars().noneMatch(c -> c < 32));
        assertEquals(value, JsonParser.parseString("\"" + escaped + "\"").getAsString());
        String bounded = JsonParser.parseString("\"" + esc("x".repeat(252) + "😀".repeat(10)) + "\"").getAsString();
        assertTrue(bounded.length() <= MAX_STRING_LENGTH);
        assertTrue(bounded.endsWith("..."));
        assertFalse(Character.isHighSurrogate(bounded.charAt(bounded.length() - 4)));
    }

    @Test
    void stageListsRetainTotalsWithoutAddingAnExtraElement() {
        var capture = capture();
        var stages = IntStream.range(0, 35).mapToObj(i -> StageId.parse("test:stage" + i)).toList();
        var json = JsonParser.parseString("{" + capture.stageFields("stages", stages) + "}").getAsJsonObject();
        assertEquals(32, json.getAsJsonArray("stages").size());
        assertEquals(35, json.get("stages_total").getAsInt());
        assertTrue(json.get("stages_truncated").getAsBoolean());
    }

    @Test
    void ownerLabelsAreBoundedAndLocalToEachCapture() {
        var first = capture();
        var second = capture();
        var owner = new OwnerRef(OwnerKind.PERSONAL, UUID.randomUUID());
        assertEquals("owner1", first.ownerLabel(owner));
        assertEquals("owner1", first.ownerLabel(owner));
        assertEquals("owner1", second.ownerLabel(new OwnerRef(OwnerKind.PERSONAL, UUID.randomUUID())));
        for (int i = 1; i < MAX_COLLECTION_LENGTH; i++) first.ownerLabel(new OwnerRef(OwnerKind.PERSONAL, UUID.randomUUID()));
        assertEquals("owner_truncated", first.ownerLabel(new OwnerRef(OwnerKind.PERSONAL, UUID.randomUUID())));
        assertEquals("owner1", first.ownerLabel(owner));
    }

    private Capture capture() {
        var value = new Capture("capture" + captures.size(), target, "permissions",
            directory.resolve("capture" + captures.size() + ".log"), 100);
        captures.add(value);
        return value;
    }

    private static void awaitWriter(Capture capture) throws Exception {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            while (!capture.canReplace()) Thread.sleep(5);
        });
    }
}
