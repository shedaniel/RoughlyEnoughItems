package me.shedaniel.rei.listeners;

import net.minecraft.inventory.Slot;

public interface ContainerScreenHooks {
    
    int rei_getContainerLeft();
    
    int rei_getContainerTop();
    
    int rei_getContainerWidth();
    
    int rei_getContainerHeight();
    
    Slot rei_getHoveredSlot();
    
}
