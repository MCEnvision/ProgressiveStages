package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.api.StageId;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;
import com.enviouse.progressivestages.common.stage.PermissionStageSource;

import static org.junit.jupiter.api.Assertions.*;

class TeamStageDataTest {
    private static final StageId STAGE = StageId.parse("progressivestages:chef");

    @Test
    void personalAndTeamNamespacesRemainDistinctThroughCodec() {
        UUID team = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        TeamStageData data = new TeamStageData();
        assertTrue(data.grantStage(team, STAGE));
        assertFalse(data.grantStageFromSource(team, STAGE, "luckperms:chef"));
        assertTrue(data.grantPersonalStage(player, STAGE));
        assertTrue(data.getSources(team, STAGE).contains("luckperms:chef"));
        assertTrue(data.hasStage(team, STAGE));
        assertTrue(data.hasPersonalStage(player, STAGE));
        assertFalse(data.hasPersonalStage(team, STAGE));

        var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        TeamStageData decoded = TeamStageData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(Set.of(STAGE), decoded.getStages(team));
        assertEquals(Set.of(STAGE), decoded.getPersonalStages(player));
        assertTrue(decoded.getSources(team, STAGE).contains("luckperms:chef"));
    }

    @Test
    void legacyTeamOnlyPayloadStillDecodes() {
        UUID team = UUID.randomUUID();
        var payload = com.google.gson.JsonParser.parseString("{\"team_stages\":{\"" + team + "\":[\"progressivestages:chef\"]}}");
        TeamStageData decoded = TeamStageData.CODEC.parse(JsonOps.INSTANCE, payload).getOrThrow();
        assertTrue(decoded.hasStage(team, STAGE));
        assertTrue(decoded.getPersonalStages(UUID.randomUUID()).isEmpty());
        assertEquals(0, decoded.getOwnershipSchema());
    }

    @Test
    void equalPersonalAndTeamIdsDoNotMergeSourceAttribution() {
        UUID collision = UUID.randomUUID();
        TeamStageData data = new TeamStageData();
        data.grantStageFromSource(collision, STAGE, "team-source");
        data.grantPersonalStageFromSource(collision, STAGE, "personal-source");
        assertEquals(Set.of("team-source"), data.getSources(new OwnerRef(OwnerKind.TEAM, collision), STAGE));
        assertEquals(Set.of("personal-source"), data.getSources(new OwnerRef(OwnerKind.PERSONAL, collision), STAGE));

        var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        TeamStageData decoded = TeamStageData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(Set.of("team-source"), decoded.getSources(new OwnerRef(OwnerKind.TEAM, collision), STAGE));
        assertEquals(Set.of("personal-source"), decoded.getSources(new OwnerRef(OwnerKind.PERSONAL, collision), STAGE));
    }

    @Test
    void removingOneSourcePreservesIndependentAccess() {
        UUID player = UUID.randomUUID();
        TeamStageData data = new TeamStageData();
        data.grantPersonalStage(player, STAGE);
        data.grantPersonalStageFromSource(player, STAGE, "luckperms_synchronized");
        assertTrue(data.revokeStageFromSource(player, STAGE, "luckperms_synchronized", true));
        assertTrue(data.hasPersonalStage(player, STAGE));
        assertEquals(Set.of("independent"), data.getSources(new OwnerRef(OwnerKind.PERSONAL, player), STAGE));
    }

    @Test
    void serverSourceUsesTheServerNamespace() {
        UUID serverOwner = new UUID(0L, 0L);
        TeamStageData data = new TeamStageData();
        data.grantStageFromSource(serverOwner, STAGE, "luckperms_synchronized");
        assertEquals(Set.of("luckperms_synchronized"),
            data.getSources(new OwnerRef(OwnerKind.SERVER, serverOwner), STAGE));
        assertTrue(data.revokeStageFromSource(new OwnerRef(OwnerKind.SERVER, serverOwner), STAGE,
            "luckperms_synchronized"));
        assertFalse(data.hasStage(serverOwner, STAGE));
    }

    @Test
    void independentLegacyGrantsSurviveAddingAndRemovingTheirFirstDerivedSource() {
        for (OwnerKind kind : OwnerKind.values()) {
            UUID id = kind == OwnerKind.SERVER ? new UUID(0L, 0L) : UUID.randomUUID();
            var owner = new OwnerRef(kind, id);
            var data = new TeamStageData();
            if (kind == OwnerKind.PERSONAL) data.setPersonalStages(id, Set.of(STAGE));
            else data.setStages(id, Set.of(STAGE));
            String source = new PermissionStageSource(UUID.randomUUID(), "chef", false).label();
            if (kind == OwnerKind.PERSONAL) data.grantPersonalStageFromSource(id, STAGE, source);
            else data.grantStageFromSource(id, STAGE, source);
            var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
            var loaded = TeamStageData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
            assertEquals(Set.of("independent", source), loaded.getSources(owner, STAGE));
            assertTrue(loaded.revokeStageFromSource(owner, STAGE, source));
            assertEquals(Set.of("independent"), loaded.getSources(owner, STAGE));
            assertTrue(kind == OwnerKind.PERSONAL ? loaded.hasPersonalStage(id, STAGE) : loaded.hasStage(id, STAGE));
        }
    }

    @Test
    void subjectsSharingOneOwnerRetainDistinctContributionsThroughPersistence() {
        UUID id = UUID.randomUUID();
        var owner = new OwnerRef(OwnerKind.TEAM, id);
        var first = new PermissionStageSource(UUID.randomUUID(), "chef", false).label();
        var second = new PermissionStageSource(UUID.randomUUID(), "chef", false).label();
        var data = new TeamStageData();
        data.grantStageFromSource(id, STAGE, first);
        data.grantStageFromSource(id, STAGE, second);
        var encoded = TeamStageData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var loaded = TeamStageData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(Set.of(first, second), loaded.getSources(owner, STAGE));
        assertTrue(loaded.revokeStageFromSource(owner, STAGE, first));
        assertTrue(loaded.hasStage(id, STAGE));
        assertEquals(Set.of(second), loaded.getSources(owner, STAGE));
        assertTrue(loaded.revokeStageFromSource(owner, STAGE, second));
        assertFalse(loaded.hasStage(id, STAGE));
    }

    @Test
    void aNewDerivedStageDoesNotManufactureIndependentOwnership() {
        UUID id = UUID.randomUUID();
        var data = new TeamStageData();
        String source = new PermissionStageSource(id, "chef", false).label();
        data.grantPersonalStageFromSource(id, STAGE, source);
        assertEquals(Set.of(source), data.getSources(new OwnerRef(OwnerKind.PERSONAL, id), STAGE));
        assertTrue(data.revokeStageFromSource(id, STAGE, source, true));
        assertFalse(data.hasPersonalStage(id, STAGE));
    }

    @Test
    void invalidEmptySourcesCannotCreateAnUnattributedStage() {
        var data = new TeamStageData();
        UUID id = UUID.randomUUID();
        assertFalse(data.grantStageFromSource(id, STAGE, " "));
        assertFalse(data.grantPersonalStageFromSource(id, STAGE, null));
        assertFalse(data.hasStage(id, STAGE));
        assertFalse(data.hasPersonalStage(id, STAGE));
    }
}
