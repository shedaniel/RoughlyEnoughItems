package me.shedaniel.rei.listeners;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public interface IMixinGuiContainer {
    
    public int rei_getContainerLeft();
    
    public int rei_getContainerTop();
    
    public int rei_etContainerWidth();
    
    public int rei_getContainerHeight();
    
    public ItemStack rei_getDraggedStack();
    
    public Slot rei_getHoveredSlot();
    
}
