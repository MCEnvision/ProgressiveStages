package com.enviouse.progressivestages.common.data;

import com.enviouse.progressivestages.common.api.StageId;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import com.enviouse.progressivestages.common.stage.OwnerKind;
import com.enviouse.progressivestages.common.stage.OwnerRef;

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
}
