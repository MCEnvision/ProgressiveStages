package com.enviouse.progressivestages.mixin;

import com.enviouse.progressivestages.server.enforcement.DimensionEnforcer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityPortalTravelMixin {
    @Redirect(method = "handlePortal", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;"), require = 1)
    private Entity progressivestages$markPortalTravel(Entity entity, DimensionTransition destination) {
        return DimensionEnforcer.travelThroughPortal(entity, destination);
    }
}
