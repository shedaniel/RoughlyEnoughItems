/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.util.EnumActionResult;

public interface DisplayVisibilityHandler {
    
    /**
     * Gets the priority of the handler
     *
     * @return the priority
     */
    default float getPriority() {
        return 0f;
    }
    
    /**
     * Handles the visibility of the display.
     * {@link EnumActionResult#PASS} to pass the handling to another handler
     * {@link EnumActionResult#SUCCESS} to always display it
     * {@link EnumActionResult#FAIL} to never display it
     *
     * @param category the category of the display
     * @param display  the display of the recipe
     * @return the visibility
     */
    EnumActionResult handleDisplay(RecipeCategory<?> category, RecipeDisplay display);
    
}
