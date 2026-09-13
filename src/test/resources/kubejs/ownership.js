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
        const grant = API.mutateStage(context, id, Operation.GRANT, Cause.SCRIPT);
        require(grant.changed() && grant.affectedOwners().contains(context.owner()),
            'Explicit actor mutation must report the resolved owner.');
        require(ProgressiveStages.has(first, stage), 'Explicit actor grants must be visible to scripts.');
        require(ProgressiveStages.has(second, stage) === (stage !== personal),
            'Explicit actor grants must respect the selected sharing policy.');
        require(API.mutateStage(API.resolveStageOwner(first, id), id, Operation.REVOKE, Cause.SCRIPT).changed(),
            'Explicit actor revocation must succeed.');
        require(!ProgressiveStages.has(first, stage) && !ProgressiveStages.has(second, stage),
            'Explicit actor revocation must remove only the addressed ownership.');
    });
    data.get('completed').incrementAndGet();
});
