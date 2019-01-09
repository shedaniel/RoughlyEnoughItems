package me.shedaniel.api;

import me.shedaniel.gui.RecipeGui;
import me.shedaniel.gui.widget.Control;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public interface IDisplayCategoryCraftable<T extends IRecipe> extends IDisplayCategory<T> {
    
    public boolean canAutoCraftHere(Class<? extends GuiScreen> guiClass, T recipe);
    
    public boolean performAutoCraft(GuiScreen gui, T recipe);
    
    public void registerAutoCraftButton(List<Control> control, RecipeGui recipeGui, GuiScreen parentGui, T recipe, int number);
    
}
