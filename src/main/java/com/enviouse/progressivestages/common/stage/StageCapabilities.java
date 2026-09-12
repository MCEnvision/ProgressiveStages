package com.enviouse.progressivestages.common.stage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable provider and authoring capabilities used by validation and the editor. */
public record StageCapabilities(TeamProviderStatus teamProvider, LuckPermsStatus luckPerms,
                                List<String> supportedOwnership, List<String> supportedInboundModes,
                                Map<String, GroupStatus> configuredGroupStatus,
                                Map<String, CommandStatus> configuredCommandStatus,
                                long definitionRevision) {
    public enum TeamProviderStatus { ABSENT, DISABLED, READY }
    public enum LuckPermsStatus { ABSENT, DISABLED, STARTING, READY, FAILED }
    public enum GroupStatus { PRESENT, MISSING, UNKNOWN }
    public enum CommandStatus { RESOLVED, MISSING, AMBIGUOUS }

    public StageCapabilities {
        teamProvider = Objects.requireNonNull(teamProvider, "teamProvider");
        luckPerms = Objects.requireNonNull(luckPerms, "luckPerms");
        supportedOwnership = supportedOwnership == null ? List.of() : List.copyOf(supportedOwnership);
        supportedInboundModes = supportedInboundModes == null ? List.of() : List.copyOf(supportedInboundModes);
        configuredGroupStatus = configuredGroupStatus == null ? Map.of() : Map.copyOf(configuredGroupStatus);
        configuredCommandStatus = configuredCommandStatus == null ? Map.of() : Map.copyOf(configuredCommandStatus);
        if (definitionRevision < 0) throw new IllegalArgumentException("definitionRevision must not be negative");
    }
}
