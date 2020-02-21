/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import net.minecraft.util.ActionResult;

public interface OverlayDecider {
    boolean isHandingScreen(Class<?> screen);
    
    default ActionResult shouldScreenBeOverlayed(Class<?> screen) {
        return ActionResult.PASS;
    }
    
    /**
     * Gets the priority of the handler, the higher it is, the earlier it is called.
     *
     * @return the priority in float
     */
    default float getPriority() {
        return 0f;
    }
}
