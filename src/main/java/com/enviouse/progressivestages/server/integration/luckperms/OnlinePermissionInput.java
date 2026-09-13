package com.enviouse.progressivestages.server.integration.luckperms;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.stage.StageManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

record OnlinePermissionInput(LuckPermsAdapter.SubjectSnapshot snapshot,
                             Map<StageId, Map<String, StageManager.PermissionObservation>> observations) {
    OnlinePermissionInput {
        var copied = new LinkedHashMap<StageId, Map<String, StageManager.PermissionObservation>>();
        observations.forEach((stage, rows) -> copied.put(stage, Map.copyOf(rows)));
        observations = Map.copyOf(copied);
    }

    static OnlinePermissionInput capture(LuckPermsAdapter adapter, UUID subject,
                                         List<StageDefinition> definitions, boolean ready) {
        if (!ready) return unavailable();
        var snapshot = adapter.snapshot(subject);
        if (!snapshot.ready()) return unavailable();
        var permissions = new LinkedHashMap<String, LuckPermsAdapter.PermissionValue>();
        for (var definition : definitions) {
            var options = definition.getLuckPerms();
            if (!options.present() || !options.enabled()) continue;
            for (var row : options.inbound()) {
                for (String permission : row.permissions()) {
                    if (permissions.containsKey(permission)) continue;
                    var result = adapter.permissionResult(subject, permission);
                    if (!result.ready()) return unavailable();
                    permissions.put(permission, result.value());
                }
            }
        }
        snapshot = new LuckPermsAdapter.SubjectSnapshot(true, snapshot.groups(), permissions, snapshot.contexts());
        var observations = new LinkedHashMap<StageId, Map<String, StageManager.PermissionObservation>>();
        for (var definition : definitions) {
            var options = definition.getLuckPerms();
            if (!options.present() || !options.enabled()) continue;
            var rows = new LinkedHashMap<String, StageManager.PermissionObservation>();
            for (var row : options.inbound()) rows.put(row.id(), PermissionEligibility.observe(row, snapshot));
            observations.put(definition.getId(), rows);
        }
        return new OnlinePermissionInput(snapshot, observations);
    }

    private static OnlinePermissionInput unavailable() {
        return new OnlinePermissionInput(LuckPermsAdapter.SubjectSnapshot.unavailable(), Map.of());
    }
}
