package me.shedaniel.rei.listeners;

import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

public interface IMixinContainerScreen {
    
    public int rei_getContainerLeft();
    
    public int rei_getContainerTop();
    
    public int rei_getContainerWidth();
    
    public int rei_getContainerHeight();
    
    public ItemStack rei_getDraggedStack();
    
    public Slot rei_getHoveredSlot();
    
}
