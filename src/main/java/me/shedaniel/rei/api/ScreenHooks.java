package me.shedaniel.rei.api;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.List;

public interface ScreenHooks {
    
    List<GuiButton> cloth_getButtonWidgets();
    
    List<IGuiEventListener> cloth_getChildren();
    
}
