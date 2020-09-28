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
import me.shedaniel.rei.impl.Internals;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public interface RecipeHelper {
    
    /**
     * @return the instance of {@link me.shedaniel.rei.api.RecipeHelper}
     */
    @NotNull
    static RecipeHelper getInstance() {
        return Internals.getRecipeHelper();
    }
    
    AutoTransferHandler registerAutoCraftingHandler(AutoTransferHandler handler);
    
    void registerFocusedStackProvider(FocusedStackProvider provider);
    
    @Nullable
    @ApiStatus.Internal
    EntryStack getScreenFocusedStack(Screen screen);
    
    List<AutoTransferHandler> getSortedAutoCraftingHandler();
    
    /**
     * Gets the total recipe count registered
     *
     * @return the recipe count
     */
    int getRecipeCount();
    
    /**
     * @return a list of sorted recipes
     */
    List<IRecipe> getAllSortedRecipes();
    
    /**
     * Gets all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    List<EntryStack> findCraftableEntriesByItems(Iterable<EntryStack> inventoryItems);
    
    /**
     * Gets all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    default List<EntryStack> findCraftableEntriesByItems(List<EntryStack> inventoryItems) {
        return findCraftableEntriesByItems((Iterable<EntryStack>) inventoryItems);
    }
    
    /**
     * Registers a category
     *
     * @param category the category to register
     */
    void registerCategory(RecipeCategory<?> category);
    
    default void registerCategories(Iterable<RecipeCategory<?>> categories) {
        for (RecipeCategory<?> category : categories) {
            registerCategory(category);
        }
    }
    
    default void registerCategories(RecipeCategory<?>... categories) {
        for (RecipeCategory<?> category : categories) {
            registerCategory(category);
        }
    }
    
    /**
     * Registers the working stations of a category
     *
     * @param category        the category
     * @param workingStations the working stations
     */
    void registerWorkingStations(ResourceLocation category, List<EntryStack>... workingStations);
    
    /**
     * Registers the working stations of a category
     *
     * @param category        the category
     * @param workingStations the working stations
     */
    void registerWorkingStations(ResourceLocation category, EntryStack... workingStations);
    
    List<List<EntryStack>> getWorkingStations(ResourceLocation category);
    
    /**
     * Registers a recipe display.
     *
     * @param display the recipe display
     */
    void registerDisplay(RecipeDisplay display);
    
    /**
     * Registers a recipe display.
     *
     * @param categoryIdentifier the category to display in
     * @param display            the recipe display
     * @deprecated Use {@link RecipeHelper#registerDisplay(RecipeDisplay)}
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default void registerDisplay(ResourceLocation categoryIdentifier, RecipeDisplay display) {
        registerDisplay(display);
    }
    
    Map<RecipeCategory<?>, List<RecipeDisplay>> buildMapFor(ClientHelper.ViewSearchBuilder builder);
    
    /**
     * Gets a map of recipes for an entry
     *
     * @param stack the stack to be crafted
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(EntryStack stack);
    
    RecipeCategory<?> getCategory(ResourceLocation identifier);
    
    /**
     * Gets the vanilla recipe manager
     *
     * @return the recipe manager
     */
    RecipeManager getRecipeManager();
    
    /**
     * Gets all registered categories
     *
     * @return the list of categories
     */
    List<RecipeCategory<?>> getAllCategories();
    
    /**
     * Gets a map of usages for an entry
     *
     * @param stack the stack to be used
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getUsagesFor(EntryStack stack);
    
    /**
     * Gets the optional of the auto crafting button area from a category
     *
     * @param category the category of the display
     * @return the optional of auto crafting button area
     */
    Optional<ButtonAreaSupplier> getAutoCraftButtonArea(RecipeCategory<?> category);
    
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
    
    /**
     * Gets the map of all recipes visible to the player
     *
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes();
    
    Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipesNoHandlers();
    
    List<RecipeDisplay> getAllRecipesFromCategory(RecipeCategory<?> category);
    
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
    
    boolean isDisplayNotVisible(RecipeDisplay display);
    
    /**
     * Checks if the display is visible by asking recipe visibility handlers
     *
     * @param display the display to be checked
     * @return whether the display should be visible
     */
    boolean isDisplayVisible(RecipeDisplay display);
    
    <T extends IRecipe<?>> void registerRecipes(ResourceLocation category, Predicate<IRecipe> recipeFilter, Function<T, RecipeDisplay> mappingFunction);
    
    /**
     * Registers a live recipe generator.
     *
     * @param liveRecipeGenerator the generator to register
     * @apiNote Still work in progress
     */
    void registerLiveRecipeGenerator(LiveRecipeGenerator<?> liveRecipeGenerator);
    
    /**
     * @deprecated Use {@link #registerContainerClickArea(Rectangle, Class, ResourceLocation...)} for the same result.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default void registerScreenClickArea(Rectangle rectangle, Class<? extends ContainerScreen<?>> screenClass, ResourceLocation... categories) {
        registerContainerClickArea(rectangle, screenClass, categories);
    }
    
    /**
     * Registers a click area for a container screen.
     *
     * @param rectangle   The click area that is offset to the container screen's top left corner.
     * @param screenClass The class of the screen.
     * @param categories  The categories of result.
     * @param <T>         The screen type to be registered to.
     */
    default <T extends ContainerScreen<?>> void registerContainerClickArea(Rectangle rectangle, Class<T> screenClass, ResourceLocation... categories) {
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
    <T extends ContainerScreen<?>> void registerContainerClickArea(ScreenClickAreaProvider<T> rectangleSupplier, Class<T> screenClass, ResourceLocation... categories);
    
    /**
     * Registers a click area for a screen.
     *
     * @param rectangleSupplier The click area supplier that is offset to the window's top left corner.
     * @param screenClass       The class of the screen.
     * @param categories        The categories of result.
     * @param <T>               The screen type to be registered to.
     */
    <T extends Screen> void registerClickArea(ScreenClickAreaProvider<T> rectangleSupplier, Class<T> screenClass, ResourceLocation... categories);
    
    <T extends IRecipe<?>> void registerRecipes(ResourceLocation category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction);
    
    <T extends IRecipe<?>> void registerRecipes(ResourceLocation category, Function<IRecipe, Boolean> recipeFilter, Function<T, RecipeDisplay> mappingFunction);
    
    @ApiStatus.Internal
    List<RecipeHelper.ScreenClickArea> getScreenClickAreas();
    
    @ApiStatus.Internal
    boolean arePluginsLoading();
    
    @ApiStatus.Internal
    interface ScreenClickArea {
        Class<? extends Screen> getScreenClass();
        
        Rectangle getRectangle();
        
        ResourceLocation[] getCategories();
    }
    
}

