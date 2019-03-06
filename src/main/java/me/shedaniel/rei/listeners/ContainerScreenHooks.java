package me.shedaniel.rei.listeners;

import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

public interface ContainerScreenHooks {
    
    public int rei_getContainerLeft();
    
    public int rei_getContainerTop();
    
    public int rei_getContainerWidth();
    
    public int rei_getContainerHeight();
    
    public Slot rei_getHoveredSlot();
    
}
