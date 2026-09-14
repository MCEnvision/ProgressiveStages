package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.server.enforcement.ItemEnforcer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDropMixin {
    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true, require = 1)
    private void progressivestages$checkSelectedDrop(boolean entireStack, CallbackInfoReturnable<Boolean> result) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (ItemEnforcer.canDropItem(player, player.getMainHandItem())) return;
        player.inventoryMenu.sendAllDataToRemote();
        result.setReturnValue(false);
    }
}
