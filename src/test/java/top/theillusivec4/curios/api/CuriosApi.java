package top.theillusivec4.curios.api;

import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public final class CuriosApi {

    private CuriosApi() {}

    public static Optional<ICuriosItemHandler> getCuriosInventory(LivingEntity entity) {
        return Optional.empty();
    }
}
