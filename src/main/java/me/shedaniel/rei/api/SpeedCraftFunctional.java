package me.shedaniel.rei.api;

import net.minecraft.client.gui.Gui;

public interface SpeedCraftFunctional<T extends RecipeDisplay> {
    
    Class[] getFunctioningFor();
    
    boolean performAutoCraft(Gui gui, T recipe);
    
    boolean acceptRecipe(Gui gui, T recipe);
    
}
