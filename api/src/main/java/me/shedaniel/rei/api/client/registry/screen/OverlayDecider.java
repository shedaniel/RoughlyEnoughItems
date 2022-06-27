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
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.world.InteractionResult.PASS;

/**
 * @see DisplayBoundsProvider
 */
@Environment(EnvType.CLIENT)
public interface OverlayDecider extends Comparable<OverlayDecider> {
    /**
     * Returns whether this decider should be used to handle the specified screen.
     *
     * @param screen the screen
     * @param <R>    the type of the screen
     * @return whether this decider should be used to handle the specified screen
     */
    <R extends Screen> boolean isHandingScreen(Class<R> screen);
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default InteractionResult shouldScreenBeOverlaid(Class<?> screen) {
        return InteractionResult.PASS;
    }
    
    default <R extends Screen> InteractionResult shouldScreenBeOverlaid(R screen) {
        return shouldScreenBeOverlaid(screen.getClass());
    }
    
    /**
     * Gets the priority of the handler, the higher it is, the earlier it is called.
     *
     * @return the priority
     */
    default double getPriority() {
        return 0.0;
    }
    
    /**
     * Checks if REI should recalculate the overlay bounds
     *
     * @param location  the location of the display panel
     * @param rectangle the current overlay bounds
     * @return whether REI should recalculate the overlay bounds
     */
    default boolean shouldRecalculateArea(DisplayPanelLocation location, Rectangle rectangle) {
        return false;
    }
    
    /**
     * Checks if a point is inside the overlay, return false for indicating that REI should not display anything here.
     *
     * @param mouseX mouse's x coordinates
     * @param mouseY mouse's y coordinates
     * @return whether a point is inside the overlay
     */
    default InteractionResult isInZone(double mouseX, double mouseY) {
        return PASS;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    default int compareTo(OverlayDecider o) {
        return Double.compare(getPriority(), o.getPriority());
    }
}
