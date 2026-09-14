package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.server.enforcement.FluidEnforcer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FlowingFluid.class, net.minecraft.world.level.material.LavaFluid.class})
public abstract class FlowingFluidMixin {
    @Inject(method = "spreadTo", at = @At("HEAD"), cancellable = true, require = 1)
    private void progressivestages$gateFlow(LevelAccessor level, BlockPos pos, BlockState state,
            Direction direction, FluidState fluid, CallbackInfo callback) {
        if (FluidEnforcer.shouldCancelFlow(level, pos, fluid.getType())) callback.cancel();
    }
}
