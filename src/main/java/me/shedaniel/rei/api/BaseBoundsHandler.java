/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.function.Supplier;

public interface BaseBoundsHandler extends DisplayHelper.DisplayBoundsHandler<Screen> {
    /**
     * Gets the exclusion zones by the screen class
     *
     * @param currentScreenClass the current screen class
     * @return the list of exclusion zones
     */
    default List<Rectangle> getExclusionZones(Class<?> currentScreenClass) {
        return getExclusionZones(currentScreenClass, false);
    }
    
    List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort);
    
    int supplierSize();
    
    /**
     * Register an exclusion zone
     *
     * @param screenClass the screen
     * @param supplier    the exclusion zone supplier, returns the list of exclusion zones
     */
    void registerExclusionZones(Class<?> screenClass, Supplier<List<Rectangle>> supplier);
    
}
