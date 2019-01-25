package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.List;

public interface IRecipeCategoryCraftable<T extends IRecipeDisplay> {
    
    public boolean canAutoCraftHere(Class<? extends Gui> guiClass, T recipe);
    
    public boolean performAutoCraft(Gui gui, T recipe);
    
    public void registerAutoCraftButton(List<IWidget> widgets, Rectangle rectangle, IMixinContainerGui parentGui, T recipe);
    
}
