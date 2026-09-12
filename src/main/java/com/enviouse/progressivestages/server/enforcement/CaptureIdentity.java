package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

record CaptureIdentity(long definitionRevision, Map<String, Object> configuration, List<Artifact> artifacts) {
    static final int MAX_HEADER_BYTES = 16 * 1024;

    static CaptureIdentity snapshot() {
        Map<String, Object> settings = new LinkedHashMap<>();
        try {
            for (var field : StageConfig.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;
                field.setAccessible(true);
                Object value = field.get(null);
                settings.put(field.getName(), value instanceof List<?> list ? List.copyOf(list) : value);
            }
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Could not snapshot diagnostic configuration", failure);
        }
        var stages = StageFileLoader.getInstance().getCompiledSnapshot();
        List<Artifact> artifacts = new ArrayList<>();
        for (var mod : ModList.get().getMods()) {
            var file = ModList.get().getModFileById(mod.getModId());
            artifacts.add(new Artifact(mod.getModId(), mod.getVersion().toString(),
                file == null ? null : file.getFile().getFilePath()));
        }
        artifacts.sort(Comparator.comparingInt((Artifact artifact) -> switch (artifact.id()) {
            case "progressivestages" -> 0;
            case "neoforge" -> 1;
            case "minecraft" -> 2;
            default -> 3;
        }).thenComparing(Artifact::id));
        return new CaptureIdentity(stages.revision(), Map.of("settings", Collections.unmodifiableMap(settings),
            "stages", stages.stages()), List.copyOf(artifacts));
    }

    String header(String captureId, String category, long tick) throws IOException {
        JsonObject header = new JsonObject();
        header.addProperty("type", "capture_header");
        header.addProperty("capture_id", captureId);
        header.addProperty("sequence", 0);
        header.addProperty("server_tick", tick);
        header.addProperty("side", "server");
        header.addProperty("category", category);
        header.addProperty("target", "selected");
        header.addProperty("definition_revision", definitionRevision);
        header.addProperty("configuration_fingerprint_schema", ConfigurationFingerprint.SCHEMA);
        header.addProperty("configuration_sha256", ConfigurationFingerprint.of(configuration));
        header.addProperty("loaded_versions_sha256", ConfigurationFingerprint.of(artifacts.stream()
            .collect(java.util.stream.Collectors.toMap(Artifact::id, Artifact::version))));
        JsonArray files = new JsonArray();
        for (Artifact artifact : artifacts.stream().limit(InteractionCaptureManager.MAX_COLLECTION_LENGTH).toList()) {
            JsonObject file = new JsonObject();
            file.addProperty("id", InteractionCaptureManager.bounded(artifact.id()));
            file.addProperty("version", InteractionCaptureManager.bounded(artifact.version()));
            if (artifact.path() != null && Files.isRegularFile(artifact.path())) {
                file.addProperty("sha256", fileHash(artifact.path()));
                file.addProperty("source", "archive");
                if (artifact.id().equals("progressivestages")) {
                    try (JarFile jar = new JarFile(artifact.path().toFile())) {
                        var manifest = jar.getManifest();
                        var attributes = manifest == null ? null : manifest.getMainAttributes();
                        file.addProperty("build_commit", attribute(attributes, "Build-Commit"));
                        file.addProperty("build_dirty", attribute(attributes, "Build-Dirty"));
                    }
                }
            } else {
                file.addProperty("sha256", "unavailable");
                file.addProperty("source", "development_or_unavailable");
            }
            files.add(file);
        }
        header.add("artifacts", files);
        header.addProperty("artifacts_total", artifacts.size());
        header.addProperty("artifacts_truncated", artifacts.size() > files.size());
        return header + "\n";
    }

    private static String fileHash(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size = Files.size(path);
            var modified = Files.getLastModifiedTime(path);
            try (var input = Files.newInputStream(path)) {
                byte[] buffer = new byte[16384];
                int length;
                while ((length = input.read(buffer)) != -1) digest.update(buffer, 0, length);
            }
            if (size != Files.size(path) || !modified.equals(Files.getLastModifiedTime(path))) {
                throw new IOException("Artifact changed during capture identity collection");
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static String attribute(java.util.jar.Attributes attributes, String name) {
        String value = attributes == null ? null : attributes.getValue(name);
        return value == null ? "unavailable" : InteractionCaptureManager.bounded(value);
    }

    record Artifact(String id, String version, Path path) {}
}
