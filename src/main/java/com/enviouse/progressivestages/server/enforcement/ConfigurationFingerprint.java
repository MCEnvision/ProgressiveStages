package com.enviouse.progressivestages.server.enforcement;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.rehaul.ConfigProvenance;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class ConfigurationFingerprint {
    static final int SCHEMA = 1;
    private final IdentityHashMap<Object, Boolean> visiting = new IdentityHashMap<>();
    private int remaining = 200000;

    private ConfigurationFingerprint() {}

    static String of(Object value) {
        return new ConfigurationFingerprint().hash(value, 0);
    }

    private String hash(Object value, int depth) {
        if (--remaining < 0 || depth > 64) throw new IllegalArgumentException("Configuration fingerprint limit exceeded");
        MessageDigest digest = digest();
        if (value == null || value instanceof ConfigProvenance) {
            add(digest, "null");
        } else if (value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof Enum<?> || value instanceof ResourceLocation
                || value instanceof StageId) {
            add(digest, value.getClass().getName());
            add(digest, value instanceof Enum<?> entry ? entry.name() : value.toString());
        } else {
            if (visiting.put(value, true) != null) throw new IllegalArgumentException("Cyclic configuration fingerprint input");
            try {
                if (value instanceof Map<?, ?> map) {
                    add(digest, "map");
                    List<String> entries = new ArrayList<>();
                    map.forEach((key, entry) -> entries.add(hash(Arrays.asList(key, entry), depth + 1)));
                    entries.stream().sorted().forEach(entry -> add(digest, entry));
                } else if (value instanceof Set<?> set) {
                    add(digest, "set");
                    List<String> entries = new ArrayList<>();
                    set.forEach(entry -> entries.add(hash(entry, depth + 1)));
                    entries.stream().sorted().forEach(entry -> add(digest, entry));
                } else if (value instanceof List<?> list) {
                    add(digest, "list");
                    list.forEach(entry -> add(digest, hash(entry, depth + 1)));
                } else if (value instanceof Optional<?> optional) {
                    add(digest, "optional");
                    add(digest, hash(optional.orElse(null), depth + 1));
                } else {
                    Class<?> type = value.getClass();
                    if (!type.getName().startsWith("com.enviouse.progressivestages.")) {
                        throw new IllegalArgumentException("Unsupported configuration fingerprint input");
                    }
                    add(digest, type.getName());
                    var fields = Arrays.stream(type.getDeclaredFields())
                        .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
                        .filter(field -> field.getType() != ConfigProvenance.class)
                        .filter(field -> !(value instanceof com.enviouse.progressivestages.common.rehaul.CompiledStage
                            && field.getName().equals("sourceId")))
                        .sorted(Comparator.comparing(java.lang.reflect.Field::getName)).toList();
                    for (var field : fields) {
                        field.setAccessible(true);
                        add(digest, field.getName());
                        add(digest, hash(field.get(value), depth + 1));
                    }
                }
            } catch (IllegalAccessException failure) {
                throw new IllegalArgumentException("Configuration fingerprint input is inaccessible", failure);
            } finally {
                visiting.remove(value);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    private static void add(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }
}
