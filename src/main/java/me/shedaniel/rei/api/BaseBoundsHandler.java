/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.function.Function;

public interface BaseBoundsHandler extends DisplayHelper.DisplayBoundsHandler<Screen> {
    /**
     * Gets the exclusion zones by the screen class
     *
     * @param currentScreenClass the current screen class
     * @param isOnRightSide      whether the user has set the overlay to the right
     * @return the list of exclusion zones
     */
    default List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide) {
        return getCurrentExclusionZones(currentScreenClass, isOnRightSide, false);
    }
    
    List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide, boolean sort);
    
    /**
     * Register an exclusion zone
     *
     * @param screenClass the screen
     * @param supplier    the exclusion zone supplier, isOnRightSide -> the list of exclusion zones
     */
    void registerExclusionZones(Class<?> screenClass, Function<Boolean, List<Rectangle>> supplier);
    
}
