package me.shedaniel.listenerdefinitions;

import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

public interface IMixinContainerGui {
    public ItemStack getDraggedStack();
    
    public int getGuiLeft();
    
    public int getXSize();
    
    public Slot getHoveredSlot();
}
