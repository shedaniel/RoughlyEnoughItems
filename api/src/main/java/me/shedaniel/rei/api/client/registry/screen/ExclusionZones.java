/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.api.client.registry.screen;

import me.shedaniel.math.Rectangle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

/**
 * The registry for querying and registering exclusion zones.
 */
@Environment(EnvType.CLIENT)
public interface ExclusionZones extends OverlayDecider {
    /**
     * Returns the exclusion zones by the screen class.
     *
     * @param currentScreenClass the current screen class
     * @return the list of exclusion zones
     * @deprecated use the screen instance instead
     */
    @Deprecated
    default List<Rectangle> getExclusionZones(Class<?> currentScreenClass) {
        return getExclusionZones(currentScreenClass, false);
    }
    
    /**
     * Returns the exclusion zones by the screen class.
     *
     * @param currentScreenClass the current screen class
     * @return the list of exclusion zones
     * @deprecated use the screen instance instead
     */
    @Deprecated
    List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort);
    
    /**
     * Returns the exclusion zones by the screen.
     *
     * @param screen the screen
     * @return the list of exclusion zones
     */
    default List<Rectangle> getExclusionZones(Screen screen) {
        return getExclusionZones(screen, false);
    }
    
    /**
     * Returns the exclusion zones by the screen.
     *
     * @param screen the screen
     * @return the list of exclusion zones
     */
    List<Rectangle> getExclusionZones(Screen screen, boolean sort);
    
    /**
     * Returns the number of exclusion zone providers registered.
     *
     * @return the number of exclusion zone providers registered
     */
    int getZonesCount();
    
    /**
     * Register an exclusion zone.
     *
     * @param screenClass the screen class
     * @param provider    the exclusion zone provider, returns a collection of exclusion zones
     */
    <T> void register(Class<? extends T> screenClass, ExclusionZonesProvider<? extends T> provider);
}
