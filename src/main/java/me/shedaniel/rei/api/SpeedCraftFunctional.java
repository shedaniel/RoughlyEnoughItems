package me.shedaniel.rei.api;

import net.minecraft.client.gui.Screen;

public interface SpeedCraftFunctional<T extends IRecipeDisplay> {
    
    public Class[] getFunctioningFor();
    
    public boolean performAutoCraft(Screen screen, T recipe);
    
    public boolean acceptRecipe(Screen screen, T recipe);
    
}
