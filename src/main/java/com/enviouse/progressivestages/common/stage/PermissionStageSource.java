package com.enviouse.progressivestages.common.stage;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record PermissionStageSource(UUID subject, String row, boolean permanent) {
    public PermissionStageSource {
        Objects.requireNonNull(subject, "subject");
        if (row == null || !row.matches("[A-Za-z0-9_.]{1,64}")) {
            throw new IllegalArgumentException("Invalid permission source row");
        }
    }

    public String label() {
        return "luckperms:" + (permanent ? "permanent" : "synchronized") + ":" + subject + ":" + row;
    }

    public static Optional<PermissionStageSource> parse(String label) {
        if (label == null) return Optional.empty();
        String[] parts = label.split(":", -1);
        if (parts.length != 4 || !parts[0].equals("luckperms")
                || !(parts[1].equals("synchronized") || parts[1].equals("permanent"))) {
            return Optional.empty();
        }
        try {
            UUID subject = UUID.fromString(parts[2]);
            if (!subject.toString().equals(parts[2])) return Optional.empty();
            return Optional.of(new PermissionStageSource(subject, parts[3], parts[1].equals("permanent")));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
