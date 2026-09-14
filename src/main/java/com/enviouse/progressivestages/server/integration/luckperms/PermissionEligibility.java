package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.config.LuckPermsStageOptions;
import com.enviouse.progressivestages.common.stage.StageManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.TreeSet;

final class PermissionEligibility {
    private PermissionEligibility() {}

    static StageManager.PermissionObservation observe(LuckPermsStageOptions.InboundRule row,
                                                      LuckPermsAdapter.SubjectSnapshot snapshot) {
        if (!snapshot.ready() || !snapshot.permissions().keySet().containsAll(row.permissions())) return null;
        boolean any = false;
        boolean all = true;
        for (String group : row.groups()) {
            boolean value = snapshot.groups().contains(group);
            any |= value;
            all &= value;
        }
        for (String permission : row.permissions()) {
            boolean value = snapshot.permissions().get(permission) == LuckPermsAdapter.PermissionValue.TRUE;
            any |= value;
            all &= value;
        }
        StringBuilder identity = new StringBuilder();
        append(identity, row.match().name());
        append(identity, "groups");
        appendValues(identity, row.groups());
        append(identity, "permissions");
        appendValues(identity, row.permissions());
        append(identity, "required contexts");
        append(identity, Integer.toString(row.contexts().size()));
        new TreeSet<>(row.contexts().keySet()).forEach(key -> {
            append(identity, key);
            appendValues(identity, row.contexts().get(key));
        });
        append(identity, "observed contexts");
        append(identity, Integer.toString(snapshot.contexts().size()));
        new TreeSet<>(snapshot.contexts().keySet()).forEach(key -> {
            append(identity, key);
            appendValues(identity, snapshot.contexts().get(key));
        });
        try {
            String fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(identity.toString().getBytes(StandardCharsets.UTF_8)));
            return new StageManager.PermissionObservation(fingerprint,
                row.match() == LuckPermsStageOptions.Match.ANY ? any : all);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA 256 is unavailable", exception);
        }
    }

    private static void appendValues(StringBuilder target, java.util.Collection<String> values) {
        var ordered = new TreeSet<>(values);
        append(target, Integer.toString(ordered.size()));
        ordered.forEach(value -> append(target, value));
    }

    private static void append(StringBuilder target, String value) {
        target.append(value.length()).append(':').append(value);
    }
}
