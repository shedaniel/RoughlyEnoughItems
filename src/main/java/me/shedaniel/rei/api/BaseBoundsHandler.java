/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.List;

public interface BaseBoundsHandler extends DisplayHelper.DisplayBoundsHandler<Screen> {
    /**
     * Gets the exclusion zones by the screen class
     *
     * @param currentScreenClass the current screen class
     * @param isOnRightSide      whether the user has set the overlay to the right
     * @return the list of exclusion zones
     */
    List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide);
    
    /**
     * Register an exclusion zone
     *
     * @param screenClass the screen
     * @param supplier    the exclusion zone supplier
     */
    void registerExclusionZones(Class<?> screenClass, ExclusionZoneSupplier supplier);
    
    public static interface ExclusionZoneSupplier {
        /**
         * Gets the current exclusion zones
         *
         * @param isOnRightSide whether the user has set the overlay to the right
         * @return the list of exclusion zones
         */
        List<Rectangle> apply(boolean isOnRightSide);
    }
}
