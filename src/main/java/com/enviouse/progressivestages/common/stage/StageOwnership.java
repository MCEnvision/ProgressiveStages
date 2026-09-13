package com.enviouse.progressivestages.common.stage;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.config.StageDefinition;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

/** Central owner resolution for stage definitions. */
public final class StageOwnership {
    private StageOwnership() {}

    public static StageActorContext context(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return context(player, null);
    }

    public static StageActorContext context(ServerPlayer player, StageId stageId) {
        Objects.requireNonNull(player, "player");
        OwnerRef resolved = stageId == null
            ? new OwnerRef(OwnerKind.PERSONAL, player.getUUID()) : resolveOwner(player, stageId);
        long definitionRevision = StageFileLoader.getInstance().getCompiledSnapshot().revision();
        return new StageActorContext(player.getUUID(), resolved, definitionRevision,
            TeamProvider.getInstance().membershipRevision());
    }

    public static OwnerRef owner(ServerPlayer player, StageId stageId) {
        Objects.requireNonNull(player, "player");
        return resolveOwner(player, stageId);
    }

    /** Resolve an explicitly identified actor on the server thread without requiring a connected player. */
    public static StageActorContext contextForActor(UUID actorId, StageId stageId) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(stageId, "stageId");
        var server = StageManager.getInstance().getServer();
        if (server == null || !server.isSameThread()) {
            throw new IllegalStateException("Stage actor queries require the running server thread.");
        }
        if (!StageOrder.getInstance().stageExists(stageId)) {
            throw new IllegalArgumentException("Unknown stage. " + stageId);
        }
        ServerPlayer player = server.getPlayerList().getPlayer(actorId);
        if (player != null) return context(player, stageId);
        OwnerRef resolved = offlineOwner(actorId, stageId).orElseThrow(() ->
            new IllegalStateException("The offline actor owner is unavailable for stage " + stageId));
        return new StageActorContext(actorId, resolved, StageFileLoader.getInstance().getCompiledSnapshot().revision(),
            TeamProvider.getInstance().membershipRevision());
    }

    public static java.util.Optional<OwnerRef> offlineOwner(java.util.UUID subject, StageId stageId) {
        Objects.requireNonNull(subject, "subject");
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        if (definition == null) return java.util.Optional.empty();
        if (definition.isServerScope()) return java.util.Optional.of(new OwnerRef(OwnerKind.SERVER, StageManager.SERVER_TEAM));
        boolean team = definition.getTeamStage().orElse(StageConfig.isFtbTeamsMode());
        if (!team) return java.util.Optional.of(new OwnerRef(OwnerKind.PERSONAL, subject));
        return TeamProvider.getInstance().getOfflineTeamId(subject, definition.getTeamStage().orElse(false))
            .map(id -> new OwnerRef(OwnerKind.TEAM, id));
    }

    private static OwnerRef resolveOwner(ServerPlayer player, StageId stageId) {
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        if (definition != null && definition.isServerScope()) {
            return new OwnerRef(OwnerKind.SERVER, StageManager.SERVER_TEAM);
        }

        TeamProvider provider = TeamProvider.getInstance();
        boolean explicitTeam = definition != null && definition.getTeamStage().orElse(false);
        boolean team = definition == null
            ? StageConfig.isFtbTeamsMode()
            : definition.getTeamStage().orElse(StageConfig.isFtbTeamsMode());
        return team
            ? new OwnerRef(OwnerKind.TEAM, explicitTeam
                ? provider.getFtbTeamId(player) : provider.getTeamId(player))
            : new OwnerRef(OwnerKind.PERSONAL, player.getUUID());
    }

    public static boolean isTeamOwned(ServerPlayer player, StageId stageId) {
        return owner(player, stageId).kind() == OwnerKind.TEAM;
    }

    /** Explain the precedence and provider state used for a stage owner. */
    public static String resolutionReason(ServerPlayer player, StageId stageId) {
        Objects.requireNonNull(player, "player");
        StageDefinition definition = StageOrder.getInstance().getStageDefinition(stageId).orElse(null);
        if (definition != null && definition.isServerScope()) return "server";
        TeamProvider provider = TeamProvider.getInstance();
        if (definition != null && definition.getTeamStage().isPresent()) {
            if (!definition.getTeamStage().orElse(false)) return "personal";
            return provider.hasFtbTeam(player) ? "team" : "provider_fallback";
        }
        if (!StageConfig.isFtbTeamsMode()) return "personal";
        return provider.isFtbTeamsActive() && provider.hasTeam(player) ? "team" : "provider_fallback";
    }
}
