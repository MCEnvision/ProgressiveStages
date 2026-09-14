let offlineOwnership = null;
NativeEvents.onEvent('com.enviouse.progressivestages.common.api.StageActorChangeEvent', event => {
    if (offlineOwnership !== null) offlineOwnership.events.push(event);
});
ProgressiveStages.onGranted(() => {
    if (offlineOwnership !== null) offlineOwnership.legacy++;
});
ProgressiveStages.onRevoked(() => {
    if (offlineOwnership !== null) offlineOwnership.legacy++;
});
ProgressiveStages.onChanged(() => {
    if (offlineOwnership !== null) offlineOwnership.legacy++;
});

ProgressiveStages.onEvent((event, data) => {
    const name = String(event);
    if (name !== 'verification:offline_ownership' && name !== 'verification:offline_rewards') return;
    const require = (condition, message) => {
        if (!condition) throw new Error(message);
    };
    const first = data.get('first');
    const second = data.get('second');
    const API = Java.loadClass('com.enviouse.progressivestages.common.api.ProgressiveStagesAPI');
    const StageId = Java.loadClass('com.enviouse.progressivestages.common.api.StageId');
    const Operation = Java.loadClass('com.enviouse.progressivestages.common.stage.StageOperation');
    const Cause = Java.loadClass('com.enviouse.progressivestages.common.api.StageCause');
    const Manager = Java.loadClass('com.enviouse.progressivestages.common.stage.StageManager');
    const manager = Manager.getInstance();
    if (name === 'verification:offline_rewards') {
        try {
            require(offlineOwnership !== null && offlineOwnership.events.length === 6 && offlineOwnership.legacy === 0,
                'Returning actors must not replay offline change events or connected player callbacks.');
            require(first.experienceLevel === 10 && second.experienceLevel === 0,
                'Only the initiating actor must receive all three original rewards once.');
            data.get('completed').incrementAndGet();
        } finally {
            offlineOwnership = null;
        }
        return;
    }
    require(manager.getServer().getPlayerList().getPlayer(first.uuid) === null,
        'The actor must actually be absent from the connected player lookup.');
    offlineOwnership = { events: [], legacy: 0 };
    const committed = [];
    const subscription = API.subscribeCommittedStageChanges(result => {
        require(manager.getServer().isSameThread(), 'Committed script listeners must execute on the server thread.');
        committed.push(result);
    });
    try {
        ['personal', 'shared', 'global'].forEach((key, index) => {
            const id = StageId.parse(String(data.get(key)));
            const context = API.resolveActorOwner(first.uuid, id);
            const before = API.getActorSnapshot(first.uuid);
            require(!before.contains(id), 'The offline fixture must start without the target entitlement.');
            require(String(context.owner().kind()) === ['PERSONAL', 'TEAM', 'SERVER'][index],
                'Offline scripts must resolve the native provider and the declared owner namespace.');
            const grant = API.mutateStage(context, id, Operation.GRANT, Cause.SCRIPT);
            require(grant.changed() && grant.affectedOwners().contains(context.owner())
                && grant.affectedPlayers().contains(first.uuid), 'The offline grant must publish its actual actor and owner.');
            require(API.getActorSnapshot(first.uuid).contains(id) && !before.contains(id),
                'Fresh snapshots must reflect offline grants while older snapshots remain immutable.');
            require(API.getActorSnapshot(second.uuid).contains(id) === (key !== 'personal'),
                'Native team and server access must remain shared while personal grants stay private.');
            require(!API.mutateStage(context, id, Operation.GRANT, Cause.SCRIPT).changed(),
                'An offline script retry must not acquire the same stage again.');
            const revoke = API.mutateStage(API.resolveActorOwner(first.uuid, id), id, Operation.REVOKE, Cause.SCRIPT);
            require(revoke.changed() && !API.getActorSnapshot(first.uuid).contains(id)
                && !API.getActorSnapshot(second.uuid).contains(id), 'Offline revocation must remove only the addressed entitlement.');
            require(!API.mutateStage(API.resolveActorOwner(first.uuid, id), id, Operation.REVOKE, Cause.SCRIPT).changed(),
                'An offline revocation retry must not publish another change.');
            const events = offlineOwnership.events.slice(index * 2);
            require(events.length === 2 && String(events[0].getChangeType()) === 'GRANTED'
                && String(events[1].getChangeType()) === 'REVOKED', 'Native script events must report one grant and one revoke.');
            events.forEach(change => {
                require(change.getContext().equals(context) && change.getStageId().equals(id)
                    && String(change.getCause()) === 'SCRIPT', 'Native events must preserve actor context, stage and cause.');
            });
        });
        require(committed.length === 6 && offlineOwnership.legacy === 0,
            'Only changed operations may publish committed results, without impersonating connected player callbacks.');
        committed.forEach((result, index) => {
            require(index === 0 || result.revision() > committed[index - 1].revision(),
                'Committed offline script revisions must increase monotonically.');
        });
        let offlinePersonalId = StageId.parse(String(data.get('personal')));
        let captured = API.resolveActorOwner(first.uuid, offlinePersonalId);
        let Teams = Java.loadClass('dev.ftb.mods.ftbteams.api.FTBTeamsAPI');
        let party = Teams.api().getManager().getTeamForPlayerID(second.uuid).orElseThrow();
        party.leave(second.uuid);
        try {
            require(API.resolveActorOwner(first.uuid, offlinePersonalId).owner().equals(captured.owner()),
                'The stale context fixture must retain the same personal owner.');
            [Operation.GRANT, Operation.REVOKE].forEach(operation => {
                const stale = API.mutateStage(captured, offlinePersonalId, operation, Cause.SCRIPT);
                require(!stale.changed() && String(stale.reason()) === 'stale_membership',
                    'A native membership change must reject captured script contexts before mutation.');
            });
            require(committed.length === 6 && offlineOwnership.events.length === 6,
                'Rejected stale script contexts must not publish changes or acquisition events.');
        } finally {
            party.join(null, second.getGameProfile());
        }
        require(first.experienceLevel === 0 && second.experienceLevel === 0,
            'Offline acquisitions must reserve rewards until the actual actor returns.');
        data.get('completed').incrementAndGet();
    } catch (error) {
        offlineOwnership = null;
        throw error;
    } finally {
        subscription.close();
    }
});

ProgressiveStages.onEvent((event, data) => {
    if (String(event) !== 'verification:ownership') return;

    const first = data.get('first');
    const second = data.get('second');
    const personal = String(data.get('personal'));
    const shared = String(data.get('shared'));
    const globalStage = String(data.get('global'));
    const StageId = Java.loadClass('com.enviouse.progressivestages.common.api.StageId');
    const StageManager = Java.loadClass('com.enviouse.progressivestages.common.stage.StageManager');
    const API = Java.loadClass('com.enviouse.progressivestages.common.api.ProgressiveStagesAPI');
    const Operation = Java.loadClass('com.enviouse.progressivestages.common.stage.StageOperation');
    const Cause = Java.loadClass('com.enviouse.progressivestages.common.api.StageCause');
    const manager = StageManager.getInstance();
    const require = (condition, message) => {
        if (!condition) throw new Error(message);
    };

    require(ProgressiveStages.has(first, personal) && !ProgressiveStages.has(second, personal),
        'Personal ownership must remain separate between actual teammates.');
    require(String(ProgressiveStages.owner(first, personal).get('kind')) === 'personal',
        'The script owner query must explain personal storage.');
    require(String(ProgressiveStages.owner(first, shared).get('kind')) === 'team',
        'The script owner query must explain team storage.');
    require(String(ProgressiveStages.owner(second, globalStage).get('kind')) === 'server',
        'The script owner query must explain server storage.');
    require(ProgressiveStages.revoke(first, personal), 'The personal script revoke must succeed.');
    require(!ProgressiveStages.has(first, personal), 'The personal revoke must remove access.');
    require(ProgressiveStages.grant(second, personal), 'The second actor must receive their own profession.');
    require(!ProgressiveStages.has(first, personal) && ProgressiveStages.has(second, personal),
        'A script grant must not grant a profession to a teammate.');
    require(!ProgressiveStages.grant(second, personal), 'A duplicate script grant must be a no op.');

    const teamId = API.getStageOwner(first, StageId.parse(shared)).id();
    const personalId = StageId.parse(personal);
    manager.grantStageToTeam(teamId, personalId);
    require(manager.hasStage(teamId, personalId) && !ProgressiveStages.has(first, personal),
        'Legacy UUID grants must retain team storage without inferring a personal actor.');
    manager.revokeStageFromTeam(teamId, personalId);
    require(!manager.hasStage(teamId, personalId) && ProgressiveStages.has(second, personal),
        'Legacy UUID revocation must preserve independent personal ownership.');
    manager.grantStageToTeam(second.uuid, personalId);
    require(manager.hasStage(second.uuid, personalId), 'The legacy UUID record must exist.');
    require(ProgressiveStages.revoke(second, personal) && manager.hasStage(second.uuid, personalId),
        'A personal revoke must preserve a team record with the same UUID.');
    manager.revokeStageFromTeam(second.uuid, personalId);

    require(first.stages.add(personal), 'The native KubeJS stage API must grant a profession.');
    require(first.stages.has(personal) && !second.stages.has(personal),
        'Native KubeJS queries must respect personal ownership.');
    require(first.stages.remove(personal) && !first.stages.has(personal),
        'The native KubeJS stage API must revoke a profession.');

    require(ProgressiveStages.revoke(first, shared), 'The team script revoke must succeed.');
    require(!ProgressiveStages.has(first, shared) && !ProgressiveStages.has(second, shared),
        'Team revocation must reach both teammates.');
    require(ProgressiveStages.grant(second, shared), 'The team script grant must succeed.');
    require(ProgressiveStages.has(first, shared) && ProgressiveStages.has(second, shared),
        'Team grants must remain shared.');
    require(ProgressiveStages.grant(first, globalStage), 'The server script grant must succeed.');
    require(ProgressiveStages.has(second, globalStage), 'Server grants must reach the second actor.');
    require(ProgressiveStages.revoke(second, globalStage) && !ProgressiveStages.has(first, globalStage),
        'Server revocation must remove global access.');

    ProgressiveStages.revoke(first, shared);
    [personal, shared, globalStage].forEach(stage => {
        const id = StageId.parse(stage);
        const context = API.resolveStageOwner(first, id);
        require(API.resolveActorOwner(first.uuid, id).equals(context),
            'Explicit UUID actor resolution must remain unambiguous through Rhino.');
        const grant = API.mutateStage(context, id, Operation.GRANT, Cause.SCRIPT);
        require(grant.changed() && grant.affectedOwners().contains(context.owner()),
            'Explicit actor mutation must report the resolved owner.');
        require(ProgressiveStages.has(first, stage), 'Explicit actor grants must be visible to scripts.');
        require(API.getActorSnapshot(first.uuid).contains(id),
            'Explicit UUID actor snapshots must reflect committed grants through Rhino.');
        require(ProgressiveStages.has(second, stage) === (stage !== personal),
            'Explicit actor grants must respect the selected sharing policy.');
        require(API.mutateStage(API.resolveStageOwner(first, id), id, Operation.REVOKE, Cause.SCRIPT).changed(),
            'Explicit actor revocation must succeed.');
        require(!ProgressiveStages.has(first, stage) && !ProgressiveStages.has(second, stage),
            'Explicit actor revocation must remove only the addressed ownership.');
    });
    data.get('completed').incrementAndGet();
});
