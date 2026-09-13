package com.enviouse.progressivestages.common.team;

import com.enviouse.progressivestages.common.config.StageConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract team provider interface.
 * Supports FTB Teams integration or solo mode.
 */
public class TeamProvider {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static TeamProvider INSTANCE;
    private ITeamIntegration integration;
    private ITeamIntegration ftbIntegration;
    private boolean ftbTeamsAvailable = false;

    public static TeamProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TeamProvider();
        }
        return INSTANCE;
    }

    private TeamProvider() {}

    /**
     * Initialize the team provider
     */
    public void initialize() {
        // Check if FTB Teams integration is enabled in config
        if (!StageConfig.isFtbTeamsIntegrationEnabled()) {
            ftbTeamsAvailable = false;
            ftbIntegration = null;
            LOGGER.info("[ProgressiveStages] FTB Teams integration disabled by config, using solo mode");
            integration = new SoloIntegration();
            return;
        }

        // Check if FTB Teams is available via reflection (no direct class references)
        try {
            Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            ftbTeamsAvailable = true;
            ftbIntegration = new ReflectiveFTBTeamsIntegration();
            LOGGER.info("FTB Teams detected, enabling team integration");
        } catch (ClassNotFoundException e) {
            ftbTeamsAvailable = false;
            ftbIntegration = null;
            LOGGER.info("FTB Teams not found, using solo mode");
        }

        // Create integration based on config and availability
        if (StageConfig.isFtbTeamsMode() && ftbTeamsAvailable) {
            integration = ftbIntegration;
        } else {
            integration = new SoloIntegration();
        }
    }

    /**
     * Get the team ID for a player
     * In solo mode, this is the player's UUID
     * In team mode, this is the team's UUID
     */
    public UUID getTeamId(ServerPlayer player) {
        if (integration == null) {
            // Fallback to solo mode
            return player.getUUID();
        }
        return integration.getTeamId(player);
    }

    /**
     * Get all online players in a team
     */
    public Set<ServerPlayer> getTeamMembers(UUID teamId, ServerPlayer requester) {
        if (integration == null) {
            return Collections.singleton(requester);
        }
        return integration.getTeamMembers(teamId, requester);
    }

    /**
     * Check if FTB Teams is available and enabled
     */
    public boolean isFtbTeamsActive() {
        return ftbTeamsAvailable && StageConfig.isFtbTeamsMode();
    }

    /** Whether the FTB Teams classes were found during provider initialization. */
    public boolean isFtbTeamsAvailable() {
        return ftbTeamsAvailable;
    }

    /** Whether the player is actually attached to an FTB team. */
    public boolean hasTeam(ServerPlayer player) {
        return integration != null && integration.hasTeam(player);
    }

    /** Resolve a team through FTB Teams even when the global sharing mode is disabled. */
    public UUID getFtbTeamId(ServerPlayer player) {
        if (ftbIntegration == null) return player.getUUID();
        return ftbIntegration.getTeamId(player);
    }

    public Optional<UUID> getOfflineTeamId(UUID subject, boolean forceTeam) {
        if (subject == null) return Optional.empty();
        ITeamIntegration selected = forceTeam ? ftbIntegration : integration;
        return selected == null ? Optional.of(subject) : selected.getOfflineTeamId(subject);
    }

    /** Return members for an explicitly team-owned stage. */
    public Set<ServerPlayer> getTeamMembersForOwner(UUID teamId, ServerPlayer requester) {
        if (ftbIntegration != null) return ftbIntegration.getTeamMembers(teamId, requester);
        if (requester == null || !requester.getUUID().equals(teamId)) return Set.of();
        return getTeamMembers(teamId, requester);
    }

    /** Whether the player belongs to an available FTB Teams provider. */
    public boolean hasFtbTeam(ServerPlayer player) {
        return ftbIntegration != null && ftbIntegration.hasTeam(player);
    }

    /**
     * Interface for team integrations
     */
    public interface ITeamIntegration {
        UUID getTeamId(ServerPlayer player);
        Set<ServerPlayer> getTeamMembers(UUID teamId, ServerPlayer requester);
        default boolean hasTeam(ServerPlayer player) { return false; }
        default Optional<UUID> getOfflineTeamId(UUID subject) { return Optional.empty(); }
    }

    /**
     * Solo mode implementation - each player is their own team
     */
    private static class SoloIntegration implements ITeamIntegration {
        @Override
        public Optional<UUID> getOfflineTeamId(UUID subject) { return Optional.of(subject); }

        @Override
        public UUID getTeamId(ServerPlayer player) {
            return player.getUUID();
        }

        @Override
        public Set<ServerPlayer> getTeamMembers(UUID teamId, ServerPlayer requester) {
            return Collections.singleton(requester);
        }
    }

    /**
     * FTB Teams implementation using reflection to avoid any direct class references
     * to FTB Teams API. This prevents NoClassDefFoundError when FTB Teams is not installed.
     */
    private static class ReflectiveFTBTeamsIntegration implements ITeamIntegration {

        private Object cachedApi;
        private java.lang.reflect.Method getManagerMethod;
        private java.lang.reflect.Method getTeamForPlayerMethod;
        private java.lang.reflect.Method getTeamByIDMethod;
        private java.lang.reflect.Method getIdMethod;
        private java.lang.reflect.Method getMembersMethod;
        private boolean reflectionFailed = false;

        @Override
        public Optional<UUID> getOfflineTeamId(UUID subject) {
            ensureReflection();
            if (reflectionFailed) return Optional.empty();
            try {
                Object manager = getManagerMethod.invoke(cachedApi);
                Class<?> managerType = Class.forName("dev.ftb.mods.ftbteams.api.TeamManager");
                Object value = managerType.getMethod("getTeamForPlayerID", UUID.class).invoke(manager, subject);
                if (!(value instanceof Optional<?> team)) return Optional.empty();
                if (team.isEmpty()) return Optional.of(subject);
                Class<?> teamType = Class.forName("dev.ftb.mods.ftbteams.api.Team");
                Object id = teamType.getMethod("getId").invoke(team.get());
                return id instanceof UUID uuid ? Optional.of(uuid) : Optional.empty();
            } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
                LOGGER.debug("Unable to resolve offline stage ownership", failure);
                return Optional.empty();
            }
        }

        private void ensureReflection() {
            if (cachedApi != null || reflectionFailed) return;
            try {
                Class<?> apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
                java.lang.reflect.Method apiMethod = apiClass.getMethod("api");
                cachedApi = apiMethod.invoke(null);
                getManagerMethod = cachedApi.getClass().getMethod("getManager");
            } catch (Exception e) {
                LOGGER.warn("[ProgressiveStages] FTB Teams reflection init failed: {}", e.getMessage());
                reflectionFailed = true;
            }
        }

        @Override
        public UUID getTeamId(ServerPlayer player) {
            ensureReflection();
            if (reflectionFailed) return player.getUUID();

            try {
                Object manager = getManagerMethod.invoke(cachedApi);

                if (getTeamForPlayerMethod == null) {
                    getTeamForPlayerMethod = manager.getClass().getMethod("getTeamForPlayer", ServerPlayer.class);
                }

                @SuppressWarnings("unchecked")
                Optional<Object> teamOpt = (Optional<Object>) getTeamForPlayerMethod.invoke(manager, player);

                if (teamOpt.isPresent()) {
                    Object team = teamOpt.get();
                    if (getIdMethod == null) {
                        getIdMethod = team.getClass().getMethod("getId");
                    }
                    return (UUID) getIdMethod.invoke(team);
                }
            } catch (Exception e) {
                LOGGER.debug("Failed to get FTB Team for player {}: {}", player.getName().getString(), e.getMessage());
            }

            return player.getUUID();
        }

        @Override
        public Set<ServerPlayer> getTeamMembers(UUID teamId, ServerPlayer requester) {
            ensureReflection();
            if (reflectionFailed) return Collections.singleton(requester);

            try {
                Object manager = getManagerMethod.invoke(cachedApi);

                if (getTeamByIDMethod == null) {
                    getTeamByIDMethod = manager.getClass().getMethod("getTeamByID", UUID.class);
                }

                @SuppressWarnings("unchecked")
                Optional<Object> teamOpt = (Optional<Object>) getTeamByIDMethod.invoke(manager, teamId);

                if (teamOpt.isPresent()) {
                    Object team = teamOpt.get();
                    if (getMembersMethod == null) {
                        getMembersMethod = team.getClass().getMethod("getMembers");
                    }

                    @SuppressWarnings("unchecked")
                    Collection<UUID> memberIds = (Collection<UUID>) getMembersMethod.invoke(team);
                    Set<ServerPlayer> members = new java.util.HashSet<>();

                    for (UUID memberId : memberIds) {
                        ServerPlayer member = requester.getServer().getPlayerList().getPlayer(memberId);
                        if (member != null) {
                            members.add(member);
                        }
                    }

                    return members;
                }
            } catch (Exception e) {
                LOGGER.debug("Failed to get FTB Team members for team {}: {}", teamId, e.getMessage());
            }

            return Collections.singleton(requester);
        }

        @Override
        public boolean hasTeam(ServerPlayer player) {
            ensureReflection();
            if (reflectionFailed) return false;
            try {
                Object manager = getManagerMethod.invoke(cachedApi);
                if (getTeamForPlayerMethod == null) {
                    getTeamForPlayerMethod = manager.getClass().getMethod("getTeamForPlayer", ServerPlayer.class);
                }
                @SuppressWarnings("unchecked")
                Optional<Object> team = (Optional<Object>) getTeamForPlayerMethod.invoke(manager, player);
                return team.isPresent();
            } catch (Exception e) {
                return false;
            }
        }
    }
}
