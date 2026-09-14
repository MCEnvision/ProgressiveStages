package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.common.config.StageConfig;
import com.enviouse.progressivestages.common.lock.LockRegistry;
import com.enviouse.progressivestages.server.enforcement.FluidEnforcer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item {
    @Shadow @Final public Fluid content;

    protected BucketItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, require = 1)
    private void progressivestages$checkFluidAction(Level level, Player actor, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> result) {
        if (!(actor instanceof ServerPlayer player) || player.isSpectator()
                || StageConfig.isAllowCreativeBypass() && player.isCreative()) return;
        var hit = getPlayerPOVHitResult(level, actor,
            content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        boolean blocked = content == Fluids.EMPTY
            ? !FluidEnforcer.canPickupFluid(player, level, hit.getBlockPos())
            : LockRegistry.getInstance().isFluidBlockedFor(player, BuiltInRegistries.FLUID.getKey(content), "place");
        if (!blocked) return;
        if (player.connection != null) {
            player.connection.send(new ClientboundBlockUpdatePacket(level, hit.getBlockPos()));
            player.connection.send(new ClientboundBlockUpdatePacket(level, hit.getBlockPos().relative(hit.getDirection())));
            player.inventoryMenu.sendAllDataToRemote();
        }
        result.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
    }
}
