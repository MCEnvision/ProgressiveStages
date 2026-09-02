package top.theillusivec4.curios.api.type.capability;

import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

public interface ICuriosItemHandler {
    Map<String, ICurioStacksHandler> getCurios();
}
