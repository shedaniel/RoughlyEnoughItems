package me.shedaniel.rei.api;

import net.minecraft.client.gui.Gui;

public interface SpeedCraftFunctional<T extends IRecipeDisplay> {
    
    public Class[] getFunctioningFor();
    
    public boolean performAutoCraft(Gui gui, T recipe);
    
    public boolean acceptRecipe(Gui gui, T recipe);
    
}
