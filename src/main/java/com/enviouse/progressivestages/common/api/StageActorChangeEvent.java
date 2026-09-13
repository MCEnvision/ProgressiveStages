package com.enviouse.progressivestages.common.api;

import com.enviouse.progressivestages.common.stage.StageActorContext;
import net.neoforged.bus.api.Event;

import java.util.Objects;

/** A committed offline stage change with an explicit actor and concrete storage owner. */
public final class StageActorChangeEvent extends Event {
    private final StageActorContext context;
    private final StageId stageId;
    private final StageChangeType changeType;
    private final StageCause cause;

    public StageActorChangeEvent(StageActorContext context, StageId stageId,
                                 StageChangeType changeType, StageCause cause) {
        this.context = Objects.requireNonNull(context, "context");
        this.stageId = Objects.requireNonNull(stageId, "stageId");
        this.changeType = Objects.requireNonNull(changeType, "changeType");
        this.cause = cause;
    }

    public StageActorContext getContext() { return context; }
    public StageId getStageId() { return stageId; }
    public StageChangeType getChangeType() { return changeType; }
    public StageCause getCause() { return cause; }
}
