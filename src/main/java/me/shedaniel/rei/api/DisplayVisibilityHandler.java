/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.config.DisplayVisibility;

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
     * {@link DisplayVisibility#PASS} to pass the handling to another handler
     * {@link DisplayVisibility#ALWAYS_VISIBLE} to always display it
     * {@link DisplayVisibility#NEVER_VISIBLE} to never display it
     *
     * @param category the category of the display
     * @param display  the display of the recipe
     * @return the visibility
     */
    DisplayVisibility handleDisplay(RecipeCategory<?> category, RecipeDisplay display);
    
}
