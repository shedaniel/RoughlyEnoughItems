package me.shedaniel.rei.listeners;

import me.shedaniel.rei.gui.ContainerGuiOverlay;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IMixinContainerGui {
    
    public int getContainerLeft();
    
    public int getContainerTop();
    
    public int getContainerWidth();
    
    public int getContainerHeight();
    
    public ItemStack getDraggedStack();
    
    public ContainerGuiOverlay getOverlay();
    
    public ContainerGui getContainerGui();
    
    public void setOverlay(ContainerGuiOverlay overlay);
    
}
