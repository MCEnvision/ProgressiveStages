package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.server.rehaul.RehaulRuntime;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin {

    @Inject(method = "onScoreChanged", at = @At("TAIL"))
    private void progressivestages$invalidateScore(ScoreHolder holder, Objective objective, Score score,
                                                    CallbackInfo ci) {
        RehaulRuntime.get().invalidateEntityPresenceScore(holder.getScoreboardName());
    }

    @Inject(method = "onPlayerRemoved", at = @At("TAIL"))
    private void progressivestages$invalidateRemovedPlayer(ScoreHolder holder, CallbackInfo ci) {
        RehaulRuntime.get().invalidateEntityPresenceScore(holder.getScoreboardName());
    }

    @Inject(method = "onPlayerScoreRemoved", at = @At("TAIL"))
    private void progressivestages$invalidateRemovedScore(ScoreHolder holder, Objective objective,
                                                           CallbackInfo ci) {
        RehaulRuntime.get().invalidateEntityPresenceScore(holder.getScoreboardName());
    }

    @Inject(method = "onObjectiveAdded", at = @At("TAIL"))
    private void progressivestages$invalidateAddedObjective(Objective objective, CallbackInfo ci) {
        RehaulRuntime.get().invalidateAllEntityPresenceContexts();
    }

    @Inject(method = "onObjectiveChanged", at = @At("TAIL"))
    private void progressivestages$invalidateChangedObjective(Objective objective, CallbackInfo ci) {
        RehaulRuntime.get().invalidateAllEntityPresenceContexts();
    }

    @Inject(method = "onObjectiveRemoved", at = @At("TAIL"))
    private void progressivestages$invalidateRemovedObjective(Objective objective, CallbackInfo ci) {
        RehaulRuntime.get().invalidateAllEntityPresenceContexts();
    }
}
