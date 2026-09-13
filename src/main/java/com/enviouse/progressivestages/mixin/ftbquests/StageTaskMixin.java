package com.enviouse.progressivestages.mixin.ftbquests;

import com.enviouse.progressivestages.compat.ftbquests.FtbQuestsHooks;
import dev.ftb.mods.ftbquests.quest.task.StageTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StageTask.class, remap = false)
public abstract class StageTaskMixin {
    @Shadow private String stage;
    @Shadow private boolean teamStage;

    @Redirect(method = "canSubmit", at = @At(value = "FIELD",
        target = "Ldev/ftb/mods/ftbquests/quest/task/StageTask;teamStage:Z"), require = 1)
    private boolean progressivestages$useNativeTeamStorage(StageTask task) {
        return teamStage && FtbQuestsHooks.usesNativeTeamStorage(stage);
    }
}
