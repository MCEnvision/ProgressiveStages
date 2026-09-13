package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.api.StageId;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.PermissionEpisode;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionEpisodeTest {
    private static final UUID SUBJECT = new UUID(0, 45);
    private static final OwnerRef OWNER = new OwnerRef(OwnerKind.PERSONAL, SUBJECT);
    private static final StageId STAGE = StageId.parse("progressivestages:episode");
    private static final PermissionStageSource SOURCE = new PermissionStageSource(SUBJECT, "chef", false);
    private static final String ORIGINAL = "a".repeat(64);
    private static final String OTHER = "b".repeat(64);

    private static PermissionEpisode episode() {
        return new PermissionEpisode(OWNER, STAGE, SUBJECT, "chef", ORIGINAL, true, false, 0, 0);
    }

    @Test
    void contextChangesCannotClearSuppressionOrRefreshAnExpiredEpisode() {
        var started = episode().acquire(1000, 0, 500);
        assertEquals(1500, started.expiresAt());
        assertEquals(started, started.observe(OTHER, false));
        assertEquals(started, started.observe(OTHER, true).acquire(2000, 0, 500));
        assertTrue(started.expired(1500));
        var suppressed = started.suppress();
        assertTrue(suppressed.observe(OTHER, false).observe(OTHER, true).suppressed());
        assertTrue(suppressed.observe(ORIGINAL, true).suppressed());
        var returned = suppressed.observe(ORIGINAL, false).observe(ORIGINAL, true).acquire(3000, 0, 500);
        assertFalse(returned.suppressed());
        assertEquals(3500, returned.expiresAt());
    }

    @Test
    void unqualifiedObservationsDoNotAnchorTheFirstSuccessfulGrantToAnEarlierContext() {
        var pending = episode().observe(OTHER, true);
        assertEquals(OTHER, pending.observation());
        var acquired = pending.acquire(1000, 0, 500).suppress();
        assertEquals(acquired, acquired.observe(ORIGINAL, false));
        assertFalse(acquired.observe(OTHER, false).observe(OTHER, true).suppressed());
        assertEquals(ORIGINAL, episode().suppress().observe(OTHER, true).observation());
    }

    @Test
    void firstAcquisitionUsesTheExistingOwnerClockAndDoesNotRefreshForAnotherSource() {
        var started = episode().acquire(1000, 500, 500);
        assertEquals(500, started.acquiredAt());
        assertEquals(1000, started.expiresAt());
        assertEquals(started, started.acquire(9000, 8000, 9999));
        assertEquals(0, started.suppress().administrativeGrant().acquiredAt());
        assertFalse(started.suppress().administrativeGrant().suppressed());
    }

    @Test
    void removalPersistsSuppressionWithoutAContributionAndKeepsOfflineDiscovery() {
        var data = new TeamStageData();
        data.putPermissionEpisode(episode().acquire(System.currentTimeMillis(), 0, -1));
        data.grantPersonalStageFromSource(SUBJECT, STAGE, SOURCE.label());
        assertTrue(data.revokePersonalStage(SUBJECT, STAGE));
        assertTrue(data.getPermissionContributions(SUBJECT).isEmpty());
        var loaded = TeamStageData.CODEC.parse(JsonOps.INSTANCE,
            TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow()).getOrThrow();
        assertEquals(SUBJECT, loaded.nextPermissionSubject(null));
        assertNull(loaded.nextPermissionSubject(SUBJECT));
        assertTrue(loaded.getPermissionEpisode(OWNER, STAGE, SOURCE).suppressed());
        assertFalse(loaded.grantPersonalStageFromSource(SUBJECT, STAGE, SOURCE.label()));
        var copy = loaded.copy();
        loaded.allowPermissionEpisodes(OWNER, STAGE);
        assertTrue(loaded.grantPersonalStageFromSource(SUBJECT, STAGE, SOURCE.label()));
        assertTrue(copy.getPermissionEpisode(OWNER, STAGE, SOURCE).suppressed());
    }

    @Test
    void sourceWithdrawalPreservesItsEpisodeWithoutSuppressingIt() {
        var data = new TeamStageData();
        var started = episode().acquire(System.currentTimeMillis(), 0, 60000);
        data.putPermissionEpisode(started);
        data.grantPersonalStageFromSource(SUBJECT, STAGE, SOURCE.label());
        data.revokeStageFromSource(OWNER, STAGE, SOURCE.label());
        assertEquals(started, data.getPermissionEpisode(OWNER, STAGE, SOURCE));
        assertTrue(data.grantPersonalStageFromSource(SUBJECT, STAGE, SOURCE.label()));
    }

    @Test
    void expiredPermanentSourcesCannotBecomeEffectiveAndLegacyRemovalGetsATombstone() {
        var data = new TeamStageData();
        var permanent = new PermissionStageSource(SUBJECT, "chef", true);
        data.grantPersonalStageFromSource(SUBJECT, STAGE, permanent.label());
        data.revokePersonalStage(SUBJECT, STAGE);
        assertTrue(data.getPermissionEpisode(OWNER, STAGE, permanent).suppressed());
        data.putPermissionEpisode(episode().acquire(1, 0, 1));
        assertFalse(data.grantPersonalStageFromSource(SUBJECT, STAGE, permanent.label()));
        data.grantPersonalStage(SUBJECT, STAGE);
        assertTrue(data.hasEffectiveStage(OWNER, STAGE));
    }

    @Test
    void rollbackAndOwnerNamespacesPreserveUnrelatedHistory() {
        var data = new TeamStageData();
        var first = episode();
        var team = new OwnerRef(OwnerKind.TEAM, SUBJECT);
        var other = new PermissionEpisode(team, STAGE, SUBJECT, "chef", ORIGINAL, true, false, 0, 0);
        data.putPermissionEpisode(first);
        data.putPermissionEpisode(other);
        var before = data.getPermissionEpisodes(SUBJECT);
        data.suppressPermissionEpisodes(OWNER, STAGE);
        assertFalse(data.getPermissionEpisode(team, STAGE, SOURCE).suppressed());
        data.restorePermissionEpisodes(SUBJECT, before);
        assertEquals(first, data.getPermissionEpisode(OWNER, STAGE, SOURCE));
        data.restorePermissionEpisodes(SUBJECT, List.of());
        assertNull(data.nextPermissionSubject(null));
    }

    @Test
    void unknownSchemaAndInvalidEpisodeClocksAreRejected() {
        var data = new TeamStageData();
        data.putPermissionEpisode(episode());
        var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow().getAsJsonObject();
        encoded.addProperty("permission_episode_schema", 2);
        assertThrows(IllegalArgumentException.class, () -> TeamStageData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertThrows(IllegalArgumentException.class, () -> new PermissionEpisode(OWNER, STAGE, SUBJECT,
            "chef", ORIGINAL, true, false, 100, 99));
    }
}
