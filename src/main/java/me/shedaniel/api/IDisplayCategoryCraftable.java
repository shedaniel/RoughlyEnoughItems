package me.shedaniel.api;

import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.api.IRecipe;
import me.shedaniel.gui.RecipeGui;
import me.shedaniel.gui.widget.Control;
import net.minecraft.client.gui.Gui;

import java.util.List;

public interface IDisplayCategoryCraftable<T extends IRecipe> extends IDisplayCategory<T> {
    
    public boolean canAutoCraftHere(Class<? extends Gui> guiClass, T recipe);
    
    public boolean performAutoCraft(Gui gui, T recipe);
    
    public void registerAutoCraftButton(List<Control> control, RecipeGui recipeGui, Gui parentGui, T recipe, int number);
    
}
