package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.listeners.IMixinContainerScreen;
import net.minecraft.client.gui.Screen;

import java.awt.*;
import java.util.List;

public interface IRecipeCategoryCraftable<T extends IRecipeDisplay> {
    
    boolean canAutoCraftHere(Class<? extends Screen> screenClasses, T recipe);
    
    boolean performAutoCraft(Screen gui, T recipe);
    
    void registerAutoCraftButton(List<IWidget> widgets, Rectangle rectangle, IMixinContainerScreen parentScreen, T recipe);
    
}
