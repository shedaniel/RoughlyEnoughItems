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

package me.shedaniel.rei.api.client.registry.screen;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.ingredient.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public interface ScreenRegistry extends Reloadable<REIClientPlugin> {
    /**
     * @return the instance of {@link ScreenRegistry}
     */
    static ScreenRegistry getInstance() {
        return PluginManager.getClientInstance().get(ScreenRegistry.class);
    }
    
    /**
     * Gets all sorted registered overlay deciders for a screen
     *
     * @return the list of registered overlay deciders
     */
    <R extends Screen> List<OverlayDecider> getDeciders(R screen);
    
    /**
     * Gets all registered overlay deciders
     *
     * @return the list of registered overlay deciders
     */
    List<OverlayDecider> getDeciders();
    
    /**
     * Registers an overlay decider, may be an instance of {@link DisplayBoundsProvider} for providing
     * the boundaries of a screen.
     *
     * @param decider the decider to register
     */
    void registerDecider(OverlayDecider decider);
    
    /**
     * Registers a provider for getting the focused stack by the mouse.
     *
     * @param provider the provider to register
     */
    void registerFocusedStack(FocusedStackProvider provider);
    
    /**
     * Returns the main center screen bounds returned, provided by deciders.
     *
     * @param screen the screen to check
     * @param <T>    the type of screen
     * @return the main center screen bounds, may be an empty {@link Rectangle} if there are no providers
     */
    <T extends Screen> Rectangle getScreenBounds(T screen);
    
    /**
     * Returns the bounds of the overlay, provided by deciders.
     *
     * @param location the side of the overlay
     * @param screen   the screen to check
     * @param <T>      the type of screen
     * @return the overlay bounds decided by the {@code location}
     */
    <T extends Screen> Rectangle getOverlayBounds(DisplayPanelLocation location, T screen);
    
    @Nullable <T extends Screen> EntryStack<?> getFocusedStack(T screen, Point mouse);
    
    ExclusionZones exclusionZones();
    
    /**
     * Registers a click area for a container screen.
     *
     * @param area        The click area that is offset to the container screen's top left corner.
     * @param screenClass The class of the screen.
     * @param categories  The categories of result.
     * @param <T>         The screen type to be registered to.
     */
    default <C extends AbstractContainerMenu, T extends AbstractContainerScreen<C>> void registerContainerClickArea(Rectangle area, Class<? extends T> screenClass, CategoryIdentifier<?>... categories) {
        registerContainerClickArea(screen -> area, screenClass, categories);
    }
    
    /**
     * Registers a click area for a container screen.
     *
     * @param area        The click area that is offset to the container screen's top left corner.
     * @param screenClass The class of the screen.
     * @param categories  The categories of result.
     * @param <T>         The screen type to be registered to.
     */
    <C extends AbstractContainerMenu, T extends AbstractContainerScreen<C>> void registerContainerClickArea(SimpleClickArea<T> area, Class<? extends T> screenClass, CategoryIdentifier<?>... categories);
    
    /**
     * Registers a click area for a screen.
     *
     * @param area        The click area that is offset to the window's top left corner.
     * @param screenClass The class of the screen.
     * @param categories  The categories of result.
     * @param <T>         The screen type to be registered to.
     */
    default <T extends Screen> void registerClickArea(SimpleClickArea<T> area, Class<? extends T> screenClass, CategoryIdentifier<?>... categories) {
        registerClickArea(screenClass, area.toClickArea(() -> categories));
    }
    
    /**
     * Registers a click area handler for a screen. A handler allows more specific implementation of click areas.
     *
     * @param screenClass The class of the screen.
     * @param area        The click area that is offset to the window's top left corner.
     * @param <T>         The screen type to be registered to.
     * @see #registerClickArea(SimpleClickArea, Class, ResourceLocation...) for a simpler way to handle areas without custom categories.
     */
    <T extends Screen> void registerClickArea(Class<? extends T> screenClass, ClickArea<T> area);
    
    /**
     * Handles the click area, returns an optional collection of category identifiers.
     *
     * @param screenClass the class of the screen
     * @param context     the click area context
     * @param <T>         the type of screen
     * @return the collection of category identifiers, may be null if there are no click area handlers.
     */
    @Nullable <T extends Screen> Set<CategoryIdentifier<?>> handleClickArea(Class<T> screenClass, ClickArea.ClickAreaContext<T> context);
}
