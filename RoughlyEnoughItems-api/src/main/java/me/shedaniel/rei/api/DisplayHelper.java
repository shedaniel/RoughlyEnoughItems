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
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public interface DisplayHelper {
    
    /**
     * @return the instance of {@link me.shedaniel.rei.api.DisplayHelper}
     */
    @NotNull
    static DisplayHelper getInstance() {
        return Internals.getDisplayHelper();
    }
    
    /**
     * Gets the sorted version of all responsible bounds handlers
     *
     * @param screenClass the class for checking responsible bounds handlers
     * @return the sorted list of responsible bounds handlers
     * @see DisplayHelper#getResponsibleBoundsHandler(Class) for the unsorted version
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    List<DisplayBoundsHandler<?>> getSortedBoundsHandlers(Class<?> screenClass);
    
    List<OverlayDecider> getSortedOverlayDeciders(Class<?> screenClass);
    
    /**
     * Gets all registered overlay deciders
     *
     * @return the list of registered overlay deciders
     */
    List<OverlayDecider> getAllOverlayDeciders();
    
    /**
     * Gets the responsible bounds handlers
     *
     * @param screenClass the class for checking responsible bounds handlers
     * @return the the list of responsible bounds handlers
     * @see DisplayHelper#getSortedBoundsHandlers(Class) for the sorted version
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    DisplayBoundsHandler<?> getResponsibleBoundsHandler(Class<?> screenClass);
    
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
    
    @ApiStatus.Internal
    BaseBoundsHandler getBaseBoundsHandler();
    
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
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    interface DisplayBoundsHandler<T> extends OverlayDecider {
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
        
        /**
         * Gets the left bounds of the overlay
         *
         * @param screen the current screen
         * @return the left bounds
         */
        Rectangle getLeftBounds(T screen);
        
        /**
         * Gets the right bounds of the overlay
         *
         * @param screen the current screen
         * @return the right bounds
         */
        Rectangle getRightBounds(T screen);
        
        /**
         * Checks if item slot can fit the screen
         *
         * @param left       the left x coordinates of the stack
         * @param top        the top y coordinates for the stack
         * @param screen     the current screen
         * @param fullBounds the current bounds
         * @return whether the item slot can fit
         * @see BaseBoundsHandler#registerExclusionZones(Class, Supplier) for easier api
         */
        default ActionResultType canItemSlotWidgetFit(int left, int top, T screen, Rectangle fullBounds) {
            ActionResultType fit = isInZone(left, top);
            if (fit == ActionResultType.FAIL)
                return ActionResultType.FAIL;
            ActionResultType fit2 = isInZone(left + 18, top + 18);
            if (fit2 == ActionResultType.FAIL)
                return ActionResultType.FAIL;
            if (fit == ActionResultType.SUCCESS && fit2 == ActionResultType.SUCCESS)
                return ActionResultType.SUCCESS;
            return ActionResultType.PASS;
        }
        
        @Override
        default ActionResultType isInZone(double mouseX, double mouseY) {
            return OverlayDecider.super.isInZone(mouseX, mouseY);
        }
        
        /**
         * Gets the item list bounds by the overlay bounds
         *
         * @param rectangle the overlay bounds
         * @return the item list bounds
         */
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        default Rectangle getItemListArea(Rectangle rectangle) {
            return new Rectangle(rectangle.x + 1, rectangle.y + 2 + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + (ConfigObject.getInstance().isEntryListWidgetScrolled() ? 0 : 22), rectangle.width - 2, rectangle.height - (ConfigObject.getInstance().getSearchFieldLocation() != SearchFieldLocation.CENTER ? 27 + 22 : 27) + (!ConfigObject.getInstance().isEntryListWidgetScrolled() ? 0 : 22));
        }
        
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        default Rectangle getFavoritesListArea(Rectangle rectangle) {
            int offset = 31 + (ConfigObject.getInstance().doesShowUtilsButtons() ? 25 : 0);
            return new Rectangle(rectangle.x + 1, rectangle.y + 2 + offset, rectangle.width - 2, rectangle.height - 5 - offset);
        }
        
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        default boolean shouldRecalculateArea(boolean isOnRightSide, Rectangle rectangle) {
            return false;
        }
        
        @Override
        default boolean shouldRecalculateArea(DisplayPanelLocation location, Rectangle rectangle) {
            return shouldRecalculateArea(location == DisplayPanelLocation.RIGHT, rectangle);
        }
        
        /**
         * Gets the priority of the handler, the higher it is, the earlier it is called.
         *
         * @return the priority in float
         */
        @Override
        float getPriority();
    }
    
}
