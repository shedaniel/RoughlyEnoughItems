package me.shedaniel.rei.api;

import net.minecraft.client.gui.GuiScreen;

public interface SpeedCraftFunctional<T extends RecipeDisplay> {
    
    Class[] getFunctioningFor();
    
    boolean performAutoCraft(GuiScreen screen, T recipe);
    
    boolean acceptRecipe(GuiScreen screen, T recipe);
    
}
