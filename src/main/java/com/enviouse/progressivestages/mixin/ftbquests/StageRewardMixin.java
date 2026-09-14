package com.enviouse.progressivestages.mixin.ftbquests;

import com.enviouse.progressivestages.compat.ftbquests.FtbQuestsHooks;
import dev.ftb.mods.ftbquests.quest.reward.StageReward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StageReward.class, remap = false)
public abstract class StageRewardMixin {
    @Shadow private String stage;

    // Reward distribution stays native while stage storage follows its definition.
    @Redirect(method = "claim", at = @At(value = "INVOKE",
        target = "Ldev/ftb/mods/ftbquests/quest/reward/StageReward;isTeamReward()Z"), require = 3)
    private boolean progressivestages$useNativeTeamStorage(StageReward reward) {
        return reward.isTeamReward() && FtbQuestsHooks.usesNativeTeamStorage(stage);
    }
}
