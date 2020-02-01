/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import net.minecraft.util.ActionResult;

public interface DisplayVisibilityHandler {
    
    /**
     * Gets the priority of the handler, the higher the priority, the earlier this is called.
     *
     * @return the priority
     */
    default float getPriority() {
        return 0f;
    }
    
    /**
     * Handles the visibility of the display.
     * {@link ActionResult#PASS} to pass the handling to another handler
     * {@link ActionResult#SUCCESS} to always display it
     * {@link ActionResult#FAIL} to never display it
     *
     * @param category the category of the display
     * @param display  the display of the recipe
     * @return the visibility
     */
    ActionResult handleDisplay(RecipeCategory<?> category, RecipeDisplay display);
    
}
