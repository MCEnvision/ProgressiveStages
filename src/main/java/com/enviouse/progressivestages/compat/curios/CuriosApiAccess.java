package com.enviouse.progressivestages.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * Reflective access to the supported Curios public API.
 *
 * <p>This class intentionally has no Curios imports. Loading ProgressiveStages without Curios
 * must not resolve any optional API class.
 */
final class CuriosApiAccess {

    static final String API_CLASS = "top.theillusivec4.curios.api.CuriosApi";
    static final String INVENTORY_CLASS = "top.theillusivec4.curios.api.type.capability.ICuriosItemHandler";
    static final String STACKS_CLASS = "top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler";
    static final String DYNAMIC_STACKS_CLASS = "top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler";

    private final Method getCuriosInventory;
    private final Method getCurios;
    private final Method getSlots;
    private final Method getStacks;
    private final Method getStackInSlot;
    private final Method setStackInSlot;

    private CuriosApiAccess(Method getCuriosInventory, Method getCurios, Method getSlots,
                            Method getStacks, Method getStackInSlot, Method setStackInSlot) {
        this.getCuriosInventory = getCuriosInventory;
        this.getCurios = getCurios;
        this.getSlots = getSlots;
        this.getStacks = getStacks;
        this.getStackInSlot = getStackInSlot;
        this.setStackInSlot = setStackInSlot;
    }

    static CuriosApiAccess resolve(ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> api = Class.forName(API_CLASS, false, classLoader);
        Class<?> inventory = Class.forName(INVENTORY_CLASS, false, classLoader);
        Class<?> stacks = Class.forName(STACKS_CLASS, false, classLoader);
        Class<?> dynamicStacks = Class.forName(DYNAMIC_STACKS_CLASS, false, classLoader);

        return new CuriosApiAccess(
            api.getMethod("getCuriosInventory", LivingEntity.class),
            inventory.getMethod("getCurios"),
            stacks.getMethod("getSlots"),
            stacks.getMethod("getStacks"),
            dynamicStacks.getMethod("getStackInSlot", int.class),
            dynamicStacks.getMethod("setStackInSlot", int.class, ItemStack.class)
        );
    }

    Object getCuriosInventory(LivingEntity entity) throws ReflectiveOperationException {
        return getCuriosInventory.invoke(null, entity);
    }

    Object getCurios(Object inventory) throws ReflectiveOperationException {
        return getCurios.invoke(inventory);
    }

    int getSlots(Object stacksHandler) throws ReflectiveOperationException {
        return (int) getSlots.invoke(stacksHandler);
    }

    Object getStacks(Object stacksHandler) throws ReflectiveOperationException {
        return getStacks.invoke(stacksHandler);
    }

    ItemStack getStackInSlot(Object stacks, int slot) throws ReflectiveOperationException {
        return (ItemStack) getStackInSlot.invoke(stacks, slot);
    }

    void setStackInSlot(Object stacks, int slot, ItemStack stack) throws ReflectiveOperationException {
        setStackInSlot.invoke(stacks, slot, stack);
    }
}
