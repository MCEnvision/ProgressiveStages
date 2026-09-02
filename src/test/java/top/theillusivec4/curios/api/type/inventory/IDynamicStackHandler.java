package top.theillusivec4.curios.api.type.inventory;

import net.minecraft.world.item.ItemStack;

public interface IDynamicStackHandler {
    ItemStack getStackInSlot(int slot);

    void setStackInSlot(int slot, ItemStack stack);
}
