package com.enviouse.progressivestages.common.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Optional, presence aware LuckPerms and command gate configuration for one stage. */
public final class LuckPermsStageOptions {
    public enum InboundMode {
        SYNCHRONIZED("synchronized"), PERMANENT("permanent");

        private final String label;
        InboundMode(String label) { this.label = label; }
        public String label() { return label; }
        public static InboundMode parse(Object value) {
            if (value == null) return SYNCHRONIZED;
            String normalized = String.valueOf(value).trim().toLowerCase(java.util.Locale.ROOT);
            for (InboundMode mode : values()) if (mode.label.equals(normalized)) return mode;
            throw new IllegalArgumentException("Invalid luckperms.inbound_mode. " + value);
        }
    }

    public enum Match { ALL("all"), ANY("any");
        private final String label;
        Match(String label) { this.label = label; }
        public String label() { return label; }
        public static Match parse(Object value) {
            if (value == null) return ALL;
            String normalized = String.valueOf(value).trim().toLowerCase(java.util.Locale.ROOT);
            for (Match match : values()) if (match.label.equals(normalized)) return match;
            throw new IllegalArgumentException("Invalid luckperms inbound match. " + value);
        }
    }

    public enum OutboundKind { GROUP("group"), PERMISSION("permission");
        private final String label;
        OutboundKind(String label) { this.label = label; }
        public String label() { return label; }
        public static OutboundKind parse(Object value) {
            if (value == null) throw new IllegalArgumentException("Missing luckperms outbound kind");
            String normalized = String.valueOf(value).trim().toLowerCase(java.util.Locale.ROOT);
            for (OutboundKind kind : values()) if (kind.label.equals(normalized)) return kind;
            throw new IllegalArgumentException("Invalid luckperms outbound kind. " + value);
        }
    }

    public record InboundRule(String id, List<String> groups, List<String> permissions,
                              Match match, Map<String, List<String>> contexts) {
        public InboundRule {
            id = validateId(id, "inbound id");
            groups = normalizeValues(groups, "inbound groups");
            permissions = normalizeValues(permissions, "inbound permissions");
            if (groups.isEmpty() && permissions.isEmpty()) {
                throw new IllegalArgumentException("A LuckPerms inbound rule requires a group or permission");
            }
            if (groups.size() + permissions.size() > 32) {
                throw new IllegalArgumentException("A LuckPerms inbound rule has too many conditions");
            }
            match = match == null ? Match.ALL : match;
            contexts = normalizeContexts(contexts);
        }
    }

    public record OutboundRule(String id, OutboundKind kind, String value,
                               Map<String, List<String>> contexts) {
        public OutboundRule {
            id = validateId(id, "outbound id");
            kind = Objects.requireNonNull(kind, "kind");
            if (value == null || value.isBlank() || value.length() > 256) {
                throw new IllegalArgumentException("LuckPerms outbound value must be 1 to 256 characters");
            }
            value = value.trim();
            contexts = normalizeContexts(contexts);
        }
    }

    public record CommandPermissionRule(String id, String path, boolean descendants) {
        public CommandPermissionRule {
            id = validateId(id, "command permission id");
            if (path == null || path.isBlank() || path.length() > 256 || path.startsWith("/")
                    || path.contains("  ") || path.matches(".*[\\[\\]{}$*].*")) {
                throw new IllegalArgumentException("Command permission path is not a literal dispatcher path");
            }
            path = path.trim();
            for (String token : path.split(" ")) {
                if (token.isBlank() || token.startsWith("/") || !token.matches("[A-Za-z0-9_.:-]+")) {
                    throw new IllegalArgumentException("Command permission path contains an invalid literal");
                }
            }
        }
    }

    private final boolean present;
    private final boolean enabled;
    private final InboundMode inboundMode;
    private final List<InboundRule> inbound;
    private final List<OutboundRule> outbound;
    private final List<CommandPermissionRule> commandPermissions;

    public LuckPermsStageOptions(boolean present, boolean enabled, InboundMode inboundMode,
                                 List<InboundRule> inbound, List<OutboundRule> outbound,
                                 List<CommandPermissionRule> commandPermissions) {
        this.present = present;
        this.enabled = enabled;
        this.inboundMode = inboundMode == null ? InboundMode.SYNCHRONIZED : inboundMode;
        this.inbound = bounded(inbound, 64, "inbound");
        this.outbound = bounded(outbound, 64, "outbound");
        this.commandPermissions = bounded(commandPermissions, 64, "command_permissions");
        ensureUnique(this.inbound.stream().map(InboundRule::id).toList(), "inbound");
        ensureUnique(this.outbound.stream().map(OutboundRule::id).toList(), "outbound");
        ensureUnique(this.commandPermissions.stream().map(CommandPermissionRule::id).toList(), "command_permissions");
    }

    public static LuckPermsStageOptions absent() {
        return new LuckPermsStageOptions(false, true, InboundMode.SYNCHRONIZED, List.of(), List.of(), List.of());
    }

    public boolean present() { return present; }
    public boolean enabled() { return enabled; }
    public InboundMode inboundMode() { return inboundMode; }
    public List<InboundRule> inbound() { return inbound; }
    public List<OutboundRule> outbound() { return outbound; }
    public List<CommandPermissionRule> commandPermissions() { return commandPermissions; }
    public boolean hasBridgeMappings() { return present && (!inbound.isEmpty() || !outbound.isEmpty()); }

    private static String validateId(String value, String label) {
        if (value == null || !value.matches("[A-Za-z0-9_.]{1,64}")) {
            throw new IllegalArgumentException("Invalid " + label + ". " + value);
        }
        return value;
    }

    private static List<String> normalizeValues(List<String> values, String label) {
        if (values == null) return List.of();
        if (values.size() > 32) throw new IllegalArgumentException("Too many values in " + label);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isBlank() || value.length() > 256) {
                throw new IllegalArgumentException("Invalid value in " + label);
            }
            result.add(value.trim());
        }
        return List.copyOf(result);
    }

    private static Map<String, List<String>> normalizeContexts(Map<String, List<String>> values) {
        if (values == null || values.isEmpty()) return Map.of();
        if (values.size() > 8) throw new IllegalArgumentException("Too many LuckPerms context keys");
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (var entry : values.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank() || key.length() > 256 || key.startsWith("progressivestages_bridge")) {
                throw new IllegalArgumentException("Invalid or reserved LuckPerms context key");
            }
            List<String> list = normalizeValues(entry.getValue(), "context " + key);
            if (list.isEmpty()) throw new IllegalArgumentException("A LuckPerms context key requires a value");
            result.put(key.trim(), list);
        }
        long combinations = 1L;
        for (List<String> list : result.values()) {
            combinations *= list.size();
            if (combinations > 64L) throw new IllegalArgumentException("Too many LuckPerms context combinations");
        }
        return Collections.unmodifiableMap(result);
    }

    private static <T> List<T> bounded(List<T> values, int max, String label) {
        List<T> result = values == null ? List.of() : List.copyOf(values);
        if (result.size() > max) throw new IllegalArgumentException("Too many " + label + " entries");
        return result;
    }

    private static void ensureUnique(List<String> ids, String label) {
        if (new LinkedHashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Duplicate " + label + " id");
        }
    }
}
