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

package me.shedaniel.rei.api.client;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipQueue;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The runtime of REI.
 */
@Environment(EnvType.CLIENT)
public interface REIRuntime extends Reloadable<REIClientPlugin> {
    /**
     * @return the instance of {@link REIRuntime}
     */
    static REIRuntime getInstance() {
        return PluginManager.getClientInstance().get(REIRuntime.class);
    }
    
    /**
     * Returns whether the overlay is visible, this is usually toggled by
     * the user with a keybind.
     *
     * @return whether the overlay is visible
     */
    default boolean isOverlayVisible() {
        return ConfigObject.getInstance().isOverlayVisible();
    }
    
    /**
     * Toggles the visibility of the overlay.
     */
    default void toggleOverlayVisible() {
        ConfigObject.getInstance().setOverlayVisible(!ConfigObject.getInstance().isOverlayVisible());
        ConfigManager.getInstance().saveConfig();
    }
    
    /**
     * Returns the screen overlay of REI, if available and constructed.
     *
     * @return the screen overlay
     */
    default Optional<ScreenOverlay> getOverlay() {
        return getOverlay(false, false);
    }
    
    /**
     * Returns the screen overlay of REI.
     * <p>
     * if {@param reset} is {@code true}, the overlay will be reset,
     * and the returned value <b>must</b> not be {@code null}.
     *
     * @param reset whether to reset the overlay
     * @return the screen overlay
     */
    default Optional<ScreenOverlay> getOverlay(boolean reset) {
        return getOverlay(reset, true);
    }
    
    /**
     * Returns the screen overlay of REI.
     * <p>
     * If {@param reset} is {@code true}, the overlay will be reset,
     * and the returned value <b>must</b> not be {@code null}.
     * <p>
     * If the overlay has not been constructed yet, and {@param init} is {@code true},
     * the overlay will be constructed, and the returned value <b>must</b> not be {@code null}.
     *
     * @param reset whether to reset the overlay
     * @param init  whether to init the overlay if it has not been constructed yet
     * @return the screen overlay
     */
    Optional<ScreenOverlay> getOverlay(boolean reset, boolean init);
    
    /**
     * Returns the previous opened container screen, if available.
     *
     * @return the previous opened container screen, or {@code null} if none
     */
    @Nullable
    AbstractContainerScreen<?> getPreviousContainerScreen();
    
    /**
     * Returns the previous opened screen, if available.
     *
     * @return the previous opened screen, or {@code null} if none
     */
    @Nullable
    Screen getPreviousScreen();
    
    /**
     * Returns whether dark mode is enabled.
     *
     * @return whether dark mode is enabled
     * @see ConfigObject#isUsingDarkTheme()
     */
    default boolean isDarkThemeEnabled() {
        return ConfigObject.getInstance().isUsingDarkTheme();
    }
    
    /**
     * Returns the text field used for searching, if constructed.
     *
     * @return the text field used for searching, or {@code null} if none
     */
    @Nullable
    default TextField getSearchTextField() {
        return getOverlay().map(ScreenOverlay::getSearchField).orElse(null);
    }
    
    /**
     * Queues a tooltip to be displayed.
     *
     * @param tooltip the tooltip to display, or {@code null}
     * @see Tooltip#queue()
     */
    default void queueTooltip(@Nullable Tooltip tooltip) {
        if (getOverlay(false, false).isEmpty()) {
            TooltipQueue.getInstance().queue(tooltip);
        }
    }
    
    /**
     * Clear all queued tooltips.
     *
     * @see Tooltip#queue()
     * @since 8.3
     */
    @ApiStatus.Experimental
    default void clearTooltips() {
        if (getOverlay(false, false).isEmpty()) {
            TooltipQueue.getInstance().clear();
        }
    }
    
    /**
     * Returns the texture location of the default display background.
     * <p>
     * This is different depending on whether dark mode is enabled.
     *
     * @return the texture location of the default display background
     */
    default ResourceLocation getDefaultDisplayTexture() {
        return getDefaultDisplayTexture(isDarkThemeEnabled());
    }
    
    /**
     * Returns the texture location of the default display background.
     *
     * @param darkTheme whether dark mode is enabled
     * @return the texture location of the default display background
     */
    ResourceLocation getDefaultDisplayTexture(boolean darkTheme);
    
    /**
     * Returns the location of the search field, according to the current screen.
     * <p>
     * If the config location is center, and the current screen is too small to display
     * the search field at the bottom center, the location returned will be the side.
     *
     * @return the location of the search field
     */
    SearchFieldLocation getContextualSearchFieldLocation();
    
    /**
     * Calculates the area of the entry list, given the bounds of the overlay.
     *
     * @param bounds the bounds of the overlay
     * @return the area of the entry list
     */
    Rectangle calculateEntryListArea(Rectangle bounds);
    
    /**
     * Calculates the area of the favorites list.
     *
     * @return the area of the favorites list
     */
    Rectangle calculateFavoritesListArea();
}
