package me.shedaniel.listenerdefinitions;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public interface IMixinGuiContainer {
    public ItemStack getDraggedStack();
    
    public int getGuiLeft();
    
    public int getXSize();
    
    public Slot getHoveredSlot();
}
