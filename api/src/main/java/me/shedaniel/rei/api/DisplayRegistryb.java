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
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public interface DisplayRegistryb {
    /**
     * @return the instance of {@link DisplayRegistry}
     */
    @NotNull
    static DisplayRegistry getInstance() {
        return Internals.getDisplayRegistry();
    }
    
    AutoTransferHandler registerAutoCraftingHandler(AutoTransferHandler handler);
    
    void registerFocusedStackProvider(FocusedStackProvider provider);
    
    @Nullable
    @ApiStatus.Internal
    EntryStack<?> getScreenFocusedStack(Screen screen);
    
    List<AutoTransferHandler> getSortedAutoCraftingHandler();
    
    /**
     * Gets all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    List<EntryStack<?>> findCraftableEntriesByItems(Iterable<? extends EntryStack<?>> inventoryItems);
    
    /**
     * Gets all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    default List<EntryStack<?>> findCraftableEntriesByItems(List<? extends EntryStack<?>> inventoryItems) {
        return findCraftableEntriesByItems((Iterable<? extends EntryStack<?>>) inventoryItems);
    }
    
    Map<DisplayCategory<?>, List<Display>> buildMapFor(ClientHelper.ViewSearchBuilder builder);
    
    /**
     * Gets a map of recipes for an entry
     *
     * @param stack the stack to be crafted
     * @return the map of recipes
     */
    Map<DisplayCategory<?>, List<Display>> getRecipesFor(EntryStack<?> stack);
    
    /**
     * Gets a map of usages for an entry
     *
     * @param stack the stack to be used
     * @return the map of recipes
     */
    Map<DisplayCategory<?>, List<Display>> getUsagesFor(EntryStack<?> stack);
    
    /**
     * Gets the optional of the auto crafting button area from a category
     *
     * @param category the category of the display
     * @return the optional of auto crafting button area
     */
    Optional<ButtonAreaSupplier> getAutoCraftButtonArea(DisplayCategory<?> category);
    
    /**
     * Registers a auto crafting button area
     *
     * @param category  the category of the button area
     * @param rectangle the button area
     */
    void registerAutoCraftButtonArea(ResourceLocation category, ButtonAreaSupplier rectangle);
    
    /**
     * Removes the auto crafting button
     *
     * @param category the category of the button
     */
    default void removeAutoCraftButton(ResourceLocation category) {
        registerAutoCraftButtonArea(category, bounds -> null);
    }
    
    
    List<Display> getAllRecipesFromCategory(DisplayCategory<?> category);
    
    /**
     * Registers a recipe visibility handler
     *
     * @param visibilityHandler the handler to be registered
     */
    void registerRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler);
    
    /**
     * Unregisters a recipe visibility handler
     *
     * @param visibilityHandler the handler to be unregistered
     */
    void unregisterRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler);
    
    /**
     * Gets an unmodifiable list of recipe visibility handlers
     *
     * @return the unmodifiable list of handlers
     */
    List<DisplayVisibilityHandler> getDisplayVisibilityHandlers();
    
    /**
     * Checks if the display is invisible by querying the recipe visibility handlers
     *
     * @param display the display to be checked
     * @return whether the display should be invisible
     */
    boolean isDisplayNotVisible(Display display);
    
    /**
     * Checks if the display is visible by querying the recipe visibility handlers
     *
     * @param display the display to be checked
     * @return whether the display should be visible
     */
    boolean isDisplayVisible(Display display);
    
    /**
     * Registers a click area for a container screen.
     *
     * @param rectangle   The click area that is offset to the container screen's top left corner.
     * @param screenClass The class of the screen.
     * @param categories  The categories of result.
     * @param <T>         The screen type to be registered to.
     */
    default <T extends AbstractContainerScreen<?>> void registerContainerClickArea(Rectangle rectangle, Class<T> screenClass, ResourceLocation... categories) {
        registerContainerClickArea(screen -> rectangle, screenClass, categories);
    }
    
    /**
     * Registers a click area for a container screen.
     *
     * @param rectangleSupplier The click area supplier that is offset to the container screen's top left corner.
     * @param screenClass       The class of the screen.
     * @param categories        The categories of result.
     * @param <T>               The screen type to be registered to.
     */
    <T extends AbstractContainerScreen<?>> void registerContainerClickArea(ScreenClickAreaProvider<T> rectangleSupplier, Class<T> screenClass, ResourceLocation... categories);
    
    /**
     * Registers a click area for a screen.
     *
     * @param rectangleSupplier The click area supplier that is offset to the window's top left corner.
     * @param screenClass       The class of the screen.
     * @param categories        The categories of result.
     * @param <T>               The screen type to be registered to.
     */
    <T extends Screen> void registerClickArea(ScreenClickAreaProvider<T> rectangleSupplier, Class<T> screenClass, ResourceLocation... categories);
    
    /**
     * Registers a click area handler for a screen. A handler allows more specific implementation of click areas.
     *
     * @param screenClass The class of the screen.
     * @param handler     The click area handler that is offset to the window's top left corner.
     * @param <T>         The screen type to be registered to.
     * @see #registerClickArea(ScreenClickAreaProvider, Class, ResourceLocation...) for a simpler way to handle areas without custom categories.
     */
    <T extends Screen> void registerClickArea(Class<T> screenClass, ClickAreaHandler<T> handler);
    
    @ApiStatus.Internal
    boolean arePluginsLoading();
}

