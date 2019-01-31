package me.shedaniel.rei.listeners;

import me.shedaniel.rei.gui.ContainerGuiOverlay;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public interface IMixinGuiContainer {
    
    public int getContainerLeft();
    
    public int getContainerTop();
    
    public int getContainerWidth();
    
    public int getContainerHeight();
    
    public ItemStack getDraggedStack();
    
    public Slot getHoveredSlot();
    
    public void setOverlay(ContainerGuiOverlay overlay);
    
}
