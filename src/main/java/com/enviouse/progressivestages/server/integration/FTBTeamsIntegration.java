package com.enviouse.progressivestages.server.integration;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.stage.StageManager;
import com.enviouse.progressivestages.common.team.TeamProvider;
import com.enviouse.progressivestages.common.team.TeamStageSync;
import com.enviouse.progressivestages.server.loader.StageFileLoader;
import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.*;

/**
 * Integration with FTB Teams mod.
 * Native team events invalidate captured contexts while polling synchronizes team state.
 *
 * Imports legacy helper grants once into their original team namespace.
 * Native quest storage hooks handle new defined stage rewards through StageManager.
 *
 * <p><b>IMPORTANT:</b> This class is NOT annotated with {@code @EventBusSubscriber}
 * because it directly imports FTB Teams API classes. If FTB Teams is not installed,
 * loading this class would cause a {@code NoClassDefFoundError}. Instead, it is
 * manually registered via {@link #registerIfAvailable()} only after confirming
 * FTB Teams is present and the config toggle is enabled.
 */
public class FTBTeamsIntegration {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;
    private static boolean initChecked = false;
    private static boolean registered = false;
    private static final java.util.function.Consumer<PlayerChangedTeamEvent> MEMBERSHIP_CHANGED =
        event -> TeamProvider.getInstance().invalidateMembership();

    // Track each player's current team to detect changes
    private static final Map<UUID, UUID> lastKnownTeams = new HashMap<>();

    /**
     * Safely register this class as an event listener if FTB Teams is available and config is enabled.
     * Called from ServerEventHandler.onServerStarting() AFTER confirming FTB Teams is present.
     * This method must only be called when FTB Teams classes are on the classpath.
     */
    public static void registerIfAvailable() {
        if (registered) return;

        if (!StageConfig.isFtbTeamsIntegrationEnabled()) {
            LOGGER.info("[ProgressiveStages] FTB Teams integration disabled by config");
            return;
        }

        if (!ModList.get().isLoaded("ftbteams")) {
            LOGGER.debug("[ProgressiveStages] FTB Teams not installed, skipping integration registration");
            return;
        }

        try {
            Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            NeoForge.EVENT_BUS.register(FTBTeamsIntegration.class);
            TeamEvent.PLAYER_CHANGED.register(MEMBERSHIP_CHANGED);
            registered = true;
            LOGGER.info("[ProgressiveStages] FTB Teams integration registered successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("[ProgressiveStages] FTB Teams mod found but API not accessible, skipping: {}", e.getMessage());
        }
    }

    /**
     * Lazy initialization — called on first server tick.
     * Detects FTB Teams availability without requiring an explicit call from startup code.
     */
    private static void ensureInitialized() {
        if (initChecked) return;
        initChecked = true;

        if (!StageConfig.isFtbTeamsIntegrationEnabled()) {
            LOGGER.info("[ProgressiveStages] FTB Teams integration disabled by config");
            initialized = false;
            return;
        }

        if (ModList.get().isLoaded("ftbteams")) {
            try {
                Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
                initialized = true;
                LOGGER.info("[ProgressiveStages] FTB Teams detected, membership monitoring enabled");
            } catch (ClassNotFoundException e) {
                LOGGER.warn("[ProgressiveStages] FTB Teams found but API not accessible: {}", e.getMessage());
                initialized = false;
            }
        } else {
            initialized = false;
        }
    }

    public static boolean isInitialized() {
        ensureInitialized();
        return initialized;
    }

    /**
     * Check for team changes every second (20 ticks).
     * This is needed because FTB Teams events use their own event system,
     * not NeoForge's event bus.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ensureInitialized();
        if (!initialized) return;

        // Only check once per second to reduce overhead
        if (event.getServer().getTickCount() % 20 != 0) return;

        importLegacyStages(event.getServer());
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            checkTeamChange(player);
        }
    }

    /**
     * Track player's initial team on login
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ensureInitialized();
        if (!initialized) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        importLegacyStages(player.getServer());

        try {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            UUID teamId = team.map(Team::getId).orElse(null);

            if (teamId != null) {
                lastKnownTeams.put(player.getUUID(), teamId);
                LOGGER.debug("Player {} logged in, team: {}", player.getName().getString(), teamId);
            } else {
                LOGGER.debug("Player {} logged in with no team", player.getName().getString());
            }
        } catch (Exception e) {
            LOGGER.error("Error tracking player {} team on login: {}", player.getName().getString(), e.getMessage());
        }
    }

    /**
     * Clean up tracking when player logs out
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ensureInitialized();
        if (!initialized) return;

        UUID playerId = event.getEntity().getUUID();
        lastKnownTeams.remove(playerId);
        LOGGER.debug("Player {} logged out, tracking removed", event.getEntity().getName().getString());
    }

    /** Detach listeners and clear runtime snapshots before another server starts. */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TeamEvent.PLAYER_CHANGED.unregister(MEMBERSHIP_CHANGED);
        if (registered) NeoForge.EVENT_BUS.unregister(FTBTeamsIntegration.class);
        registered = false;
        initialized = false;
        initChecked = false;
        lastKnownTeams.clear();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        importLegacyStages(event.getServer());
    }

    static boolean importLegacyStages(MinecraftServer server) {
        if (server == null || !server.isSameThread() || !StageConfig.isFtbTeamsIntegrationEnabled()
            || !StageConfig.isFtbQuestsIntegrationEnabled()
            || StageFileLoader.getInstance().getCompiledSnapshot().revision() <= 0
            || StageManager.getInstance().hasImportedFtbHelperStages()) return false;
        try {
            var teams = FTBTeamsAPI.api().getManager();
            if (teams.getServer() != server) return false;
            Map<UUID, Set<StageId>> legacy = new LinkedHashMap<>();
            for (Team team : teams.getTeams()) {
                Set<StageId> stages = new LinkedHashSet<>();
                for (String raw : TeamStagesHelper.getStages(team)) {
                    StageId stage = StageId.tryParse(raw);
                    if (stage != null) stages.add(stage);
                }
                legacy.put(team.getId(), Set.copyOf(stages));
            }
            return StageManager.getInstance().importFtbHelperStages(legacy);
        } catch (RuntimeException exception) {
            LOGGER.error("Could not import legacy FTB stages. Existing progression data was preserved.", exception);
            return false;
        }
    }

    /**
     * Check if a player's team has changed and handle it
     */
    private static void checkTeamChange(ServerPlayer player) {
        try {
            UUID playerId = player.getUUID();

            // Get current team from FTB Teams
            Optional<Team> currentTeamOpt = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            UUID currentTeamId = currentTeamOpt.map(Team::getId).orElse(null);

            // Get last known team
            UUID lastTeamId = lastKnownTeams.get(playerId);

            // Check if team changed
            if (!java.util.Objects.equals(currentTeamId, lastTeamId)) {
                com.enviouse.progressivestages.server.integration.luckperms.LuckPermsBridge.reconcile(player);
                LOGGER.debug("Team change detected for {}: {} -> {}",
                        player.getName().getString(),
                        lastTeamId != null ? lastTeamId : "none",
                        currentTeamId != null ? currentTeamId : "none");

                // Handle team leave
                if (lastTeamId != null && currentTeamId == null) {
                    LOGGER.info("Player {} left team {}", player.getName().getString(), lastTeamId);
                    TeamStageSync.onPlayerLeaveTeam(player, lastTeamId);
                }
                // Handle team join
                else if (currentTeamId != null && lastTeamId == null) {
                    LOGGER.info("Player {} joined team {}", player.getName().getString(), currentTeamId);
                    TeamStageSync.onPlayerJoinTeam(player, currentTeamId, playerId);
                }
                // Handle team switch (left one team, joined another)
                else if (currentTeamId != null && lastTeamId != null && !currentTeamId.equals(lastTeamId)) {
                    LOGGER.info("Player {} switched from team {} to team {}",
                            player.getName().getString(), lastTeamId, currentTeamId);
                    TeamStageSync.onPlayerLeaveTeam(player, lastTeamId);
                    TeamStageSync.onPlayerJoinTeam(player, currentTeamId, lastTeamId);
                }

                // Update tracking
                if (currentTeamId != null) {
                    lastKnownTeams.put(playerId, currentTeamId);
                } else {
                    lastKnownTeams.remove(playerId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error checking team change for {}: {}", player.getName().getString(), e.getMessage());
        }
    }

    /**
     * Get the current team ID for a player (if any)
     */
    public static UUID getTeamId(ServerPlayer player) {
        if (!initialized) return player.getUUID(); // Solo mode: player is their own team

        try {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            return team.map(Team::getId).orElse(player.getUUID());
        } catch (Exception e) {
            LOGGER.error("Error getting team for {}: {}", player.getName().getString(), e.getMessage());
            return player.getUUID();
        }
    }

    /**
     * Check if a player is in a team (not solo)
     */
    public static boolean isInTeam(ServerPlayer player) {
        if (!initialized) return false;

        try {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            return team.isPresent() && !team.get().getMembers().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
