package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.rehaul.ConditionNode;
import com.enviouse.progressivestages.server.loader.StageFileParser;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.*;

class CaptureIdentityTest {
    @TempDir Path directory;

    @Test
    void fingerprintsMapsAndSetsIndependentlyOfInsertionOrder() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("team", false);
        first.put("groups", new LinkedHashSet<>(List.of("chef", "farmer")));
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("groups", new LinkedHashSet<>(List.of("farmer", "chef")));
        second.put("team", false);
        assertEquals(ConfigurationFingerprint.of(first), ConfigurationFingerprint.of(second));
        assertNotEquals(ConfigurationFingerprint.of(List.of("chef", "farmer")),
            ConfigurationFingerprint.of(List.of("farmer", "chef")));
    }

    @Test
    void distinguishesConditionKindsAndGlobalValues() {
        var children = List.<ConditionNode>of(new ConditionNode.Constant(true), new ConditionNode.Constant(false));
        assertNotEquals(ConfigurationFingerprint.of(new ConditionNode.All(children)),
            ConfigurationFingerprint.of(new ConditionNode.Any(children)));
        assertNotEquals(ConfigurationFingerprint.of(Map.of("teamMode", "solo")),
            ConfigurationFingerprint.of(Map.of("teamMode", "ftb_teams")));
    }

    @Test
    void includesInteractionOwnershipAndPermissionValuesWithoutSourcePaths() {
        String source = """
            [stage]
            id = "test:chef"
            team_stage = false
            [luckperms]
            inbound_mode = "synchronized"
            [[luckperms.inbound]]
            id = "chef"
            groups = ["chef"]
            [[interactions]]
            type = "item_on_block"
            held_item = "id:minecraft:bread"
            target_block = "id:minecraft:chest"
            """;
        String original = fingerprint(source, "first/stage.toml");
        assertEquals(original, fingerprint(source, "different/stage.toml"));
        assertNotEquals(original, fingerprint(source.replace("false", "true"), "first/stage.toml"));
        assertNotEquals(original, fingerprint(source.replace("synchronized", "permanent"), "first/stage.toml"));
        assertNotEquals(original, fingerprint(source.replace("minecraft:bread", "minecraft:carrot"), "first/stage.toml"));
    }

    @Test
    void refusesCyclesAndUnsupportedValuesWithoutAnUnboundedTraversal() {
        List<Object> cycle = new ArrayList<>();
        cycle.add(cycle);
        assertThrows(IllegalArgumentException.class, () -> ConfigurationFingerprint.of(cycle));
        assertThrows(IllegalArgumentException.class, () -> ConfigurationFingerprint.of(new Object()));
    }

    @Test
    void headerIdentifiesTheArchiveWithoutDisclosingItsPath() throws Exception {
        Path archive = directory.resolve("private-location.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Build-Commit", "a".repeat(40));
        manifest.getMainAttributes().putValue("Build-Dirty", "false");
        try (var ignored = new JarOutputStream(Files.newOutputStream(archive), manifest)) {}
        var identity = new CaptureIdentity(7, Map.of("stage", "chef"),
            List.of(new CaptureIdentity.Artifact("progressivestages", "3.0.5", archive)));
        String line = identity.header("capture", "interactions", 100);
        var header = JsonParser.parseString(line).getAsJsonObject();
        var artifact = header.getAsJsonArray("artifacts").get(0).getAsJsonObject();
        assertEquals("a".repeat(40), artifact.get("build_commit").getAsString());
        assertEquals("false", artifact.get("build_dirty").getAsString());
        assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(archive))),
            artifact.get("sha256").getAsString());
        assertEquals(7, header.get("definition_revision").getAsLong());
        assertEquals(64, header.get("configuration_sha256").getAsString().length());
        assertFalse(line.contains(directory.toString()));
        assertFalse(line.contains("private-location"));
    }

    @Test
    void headerSharesTheOutputBudgetAndPrecedesAcceptedDecisions() throws Exception {
        var identity = new CaptureIdentity(1, Map.of(), List.of());
        var capture = new InteractionCaptureManager.Capture("capture", UUID.randomUUID(), "permissions",
            directory.resolve("capture.log"), 100, identity);
        capture.recordLine(100, () -> "{}\n");
        capture.recordLine(101, () -> "x".repeat(InteractionCaptureManager.MAX_OUTPUT_BYTES));
        assertEquals("output_limit", capture.status().stopReason());
        capture.startWriter();
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () -> {
            while (!capture.canReplace()) Thread.sleep(5);
        });
        String output = Files.readString(capture.status().output());
        assertEquals("capture_header", JsonParser.parseString(output.lines().findFirst().orElseThrow())
            .getAsJsonObject().get("type").getAsString());
        assertTrue(output.endsWith("{}\n"));
        assertEquals(1, capture.status().records());
        assertEquals(output.getBytes(StandardCharsets.UTF_8).length, capture.status().bytes());
        assertTrue(capture.status().bytes() <= InteractionCaptureManager.MAX_OUTPUT_BYTES);
        assertEquals("drained", capture.status().outputState());
    }

    @Test
    void headerBoundsTheManifestButFingerprintsAllLoadedVersions() throws Exception {
        List<CaptureIdentity.Artifact> artifacts = new ArrayList<>();
        for (int i = 0; i < 35; i++) artifacts.add(new CaptureIdentity.Artifact("mod" + i, "1", null));
        var first = JsonParser.parseString(new CaptureIdentity(1, Map.of(), List.copyOf(artifacts))
            .header("capture", "permissions", 100)).getAsJsonObject();
        assertEquals(32, first.getAsJsonArray("artifacts").size());
        assertEquals(35, first.get("artifacts_total").getAsInt());
        assertTrue(first.get("artifacts_truncated").getAsBoolean());
        assertEquals("unavailable", first.getAsJsonArray("artifacts").get(0).getAsJsonObject()
            .get("sha256").getAsString());
        artifacts.set(34, new CaptureIdentity.Artifact("mod34", "2", null));
        var changed = JsonParser.parseString(new CaptureIdentity(1, Map.of(), List.copyOf(artifacts))
            .header("capture", "permissions", 100)).getAsJsonObject();
        assertNotEquals(first.get("loaded_versions_sha256"), changed.get("loaded_versions_sha256"));
    }

    @Test
    void identityFailureStopsTheWriterAndDropsPendingRecords() {
        var identity = new CaptureIdentity(1, Map.of("unsupported", new Object()), List.of());
        var capture = new InteractionCaptureManager.Capture("failed", UUID.randomUUID(), "interactions",
            directory.resolve("failed.log"), 100, identity);
        capture.recordLine(100, () -> "{}\n");
        capture.startWriter();
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () -> {
            while (!capture.canReplace()) Thread.sleep(5);
        });
        assertFalse(capture.status().active());
        assertEquals("failed", capture.status().outputState());
        assertEquals("output_error", capture.status().stopReason());
        assertEquals(0, capture.status().queued());
        assertEquals(1, capture.status().dropped());
    }

    private static String fingerprint(String source, String path) {
        var parsed = StageFileParser.parseText(source, "stage.toml", path, false);
        assertTrue(parsed.isSuccess(), parsed.getErrorMessage());
        return ConfigurationFingerprint.of(parsed.getStageDefinition());
    }
}
