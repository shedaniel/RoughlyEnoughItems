package me.shedaniel.rei.api;

import net.minecraft.inventory.Slot;

public interface IGuiContainer {
    
    public int getContainerLeft();
    
    public int getContainerTop();
    
    public int getContainerWidth();
    
    public int getContainerHeight();
    
    public Slot getHoveredSlot();
    
}
