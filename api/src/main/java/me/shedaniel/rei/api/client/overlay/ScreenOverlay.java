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

package me.shedaniel.rei.api.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.search.SearchFilter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public abstract class ScreenOverlay extends WidgetWithBounds {
    public static Optional<ScreenOverlay> getInstance() {
        return REIRuntime.getInstance().getOverlay();
    }
    
    /**
     * Queues reload of the overlay.
     */
    public abstract void queueReloadOverlay();
    
    /**
     * Queues reload of the search result.
     */
    public abstract void queueReloadSearch();
    
    /**
     * Returns whether the overlay is queued to be reloaded.
     *
     * @return whether the overlay is queued to be reloaded
     */
    public abstract boolean isOverlayReloadQueued();
    
    /**
     * Returns whether the search result is queued to be reloaded.
     *
     * @return whether the search result is queued to be reloaded
     */
    public abstract boolean isSearchReloadQueued();
    
    /**
     * Returns the current dragging context.
     *
     * @return the current dragging context
     */
    public abstract DraggingContext<?> getDraggingContext();
    
    /**
     * Returns whether a specified point is within the bounds of the overlay.
     *
     * @param mouseX the x coordinate of the mouse
     * @param mouseY the y coordinate of the mouse
     * @return whether the point is within the bounds of the overlay
     */
    public abstract boolean isNotInExclusionZones(double mouseX, double mouseY);
    
    /**
     * Returns whether a specified bounds is within the bounds of the overlay.
     *
     * @param bounds the bounds to test
     * @return whether the bounds is within the bounds of the overlay
     */
    public boolean isNotInExclusionZones(Rectangle bounds) {
        return isNotInExclusionZones(bounds.x, bounds.y) &&
               isNotInExclusionZones(bounds.getMaxX(), bounds.y) &&
               isNotInExclusionZones(bounds.x, bounds.getMaxY()) &&
               isNotInExclusionZones(bounds.getMaxX(), bounds.getMaxY());
    }
    
    /**
     * Returns the entry list of the overlay.
     *
     * @return the entry list of the overlay
     */
    public abstract OverlayListWidget getEntryList();
    
    /**
     * Returns the favorites list of the overlay.
     *
     * @return the favorites list of the overlay, or {@code null} if favorites are not enabled
     */
    public abstract Optional<OverlayListWidget> getFavoritesList();
    
    /**
     * Returns the search field of the overlay.
     *
     * @return the search field of the overlay
     */
    public abstract TextField getSearchField();
    
    /**
     * Returns the current search filter.
     *
     * @return the current search filter
     */
    @ApiStatus.Experimental
    @Nullable
    public abstract SearchFilter getCurrentSearchFilter();
    
    /**
     * Renders a tooltip.
     *
     * @param matrices the matrices transform
     * @param tooltip  the tooltip
     */
    public abstract void renderTooltip(PoseStack matrices, Tooltip tooltip);
}
