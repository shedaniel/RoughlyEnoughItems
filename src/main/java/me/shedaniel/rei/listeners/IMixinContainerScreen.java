package me.shedaniel.rei.listeners;

import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

public interface IMixinContainerScreen {
    
    public int getContainerLeft();
    
    public int getContainerTop();
    
    public int getContainerWidth();
    
    public int getContainerHeight();
    
    public ItemStack getDraggedStack();
    
    public Slot getHoveredSlot();
    
    public void setOverlay(ContainerScreenOverlay overlay);
    
}
