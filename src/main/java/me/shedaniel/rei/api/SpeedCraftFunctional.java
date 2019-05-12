/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.Screen;

public interface SpeedCraftFunctional<T extends RecipeDisplay> {
    
    /**
     * Gets the classes that it is functioning for
     *
     * @return the array of classes
     */
    Class[] getFunctioningFor();
    
    /**
     * Performs the auto crafting
     *
     * @param screen the current screen
     * @param recipe the current recipe
     * @return whether it worked
     */
    boolean performAutoCraft(Screen screen, T recipe);
    
    /**
     * Gets if this functional accepts the auto crafting
     *
     * @param screen the current screen
     * @param recipe the current recipe
     * @return whether it is accepted
     */
    boolean acceptRecipe(Screen screen, T recipe);
    
}
