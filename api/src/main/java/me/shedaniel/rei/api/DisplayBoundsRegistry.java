/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface DisplayBoundsRegistry {
    
    /**
     * @return the instance of {@link DisplayBoundsRegistry}
     */
    @NotNull
    static DisplayBoundsRegistry getInstance() {
        return Internals.getDisplayHelper();
    }
    
    List<OverlayDecider> getSortedOverlayDeciders(Class<?> screenClass);
    
    /**
     * Gets all registered overlay deciders
     *
     * @return the list of registered overlay deciders
     */
    List<OverlayDecider> getAllOverlayDeciders();
    
    /**
     * Registers a bounds decider
     *
     * @param decider the decider to register
     */
    void registerHandler(OverlayDecider decider);
    
    default <T> void registerProvider(DisplayBoundsProvider<T> provider) {
        registerHandler(provider);
    }
    
    /**
     * Gets the left bounds of the overlay
     *
     * @param screen the current screen
     * @return the left bounds
     */
    <T> Rectangle getOverlayBounds(DisplayPanelLocation location, T screen);
    
    @ApiStatus.Experimental
    void resetCache();
    
    ExclusionZones exclusionZones();
    
    interface DisplayBoundsProvider<T> extends OverlayDecider {
        /**
         * @param screen the screen
         * @return the boundary of the base container panel.
         */
        Rectangle getScreenBounds(T screen);
        
        /**
         * Gets the base supported class for the bounds handler
         *
         * @return the base class
         */
        Class<?> getBaseSupportedClass();
        
        @Override
        default boolean isHandingScreen(Class<?> screen) {
            return getBaseSupportedClass().isAssignableFrom(screen);
        }
    }
}
