package me.shedaniel.rei.api;

import net.minecraft.client.gui.Screen;

public interface SpeedCraftFunctional<T extends RecipeDisplay> {
    
    Class[] getFunctioningFor();
    
    boolean performAutoCraft(Screen screen, T recipe);
    
    boolean acceptRecipe(Screen screen, T recipe);
    
}
