package me.shedaniel.rei.mixin;

import me.shedaniel.rei.gui.ContainerGuiOverlay;

public interface IMixinContainerGui {
    
    public int getContainerLeft();
    
    public int getContainerTop();
    
    public int getContainerWidth();
    
    public int getContainerHeight();
    
    public ContainerGuiOverlay getOverlay();
    
}
