/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface RecipeHelper {
    
    @SuppressWarnings("deprecation")
    static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
    AutoTransferHandler registerAutoCraftingHandler(AutoTransferHandler handler);
    
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
    List<Recipe> getAllSortedRecipes();
    
    /**
     * Gets all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    List<EntryStack> findCraftableEntriesByItems(List<EntryStack> inventoryItems);
    
    /**
     * Registers a category
     *
     * @param category the category to register
     */
    void registerCategory(RecipeCategory<?> category);
    
    /**
     * Registers the working stations of a category
     *
     * @param category        the category
     * @param workingStations the working stations
     */
    void registerWorkingStations(Identifier category, List<EntryStack>... workingStations);
    
    /**
     * Registers the working stations of a category
     *
     * @param category        the category
     * @param workingStations the working stations
     */
    void registerWorkingStations(Identifier category, EntryStack... workingStations);
    
    List<List<EntryStack>> getWorkingStations(Identifier category);
    
    /**
     * Registers a recipe display
     *
     * @param categoryIdentifier the category to display in
     * @param display            the recipe display
     */
    void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display);
    
    /**
     * Gets a map of recipes for an entry
     *
     * @param stack the stack to be crafted
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(EntryStack stack);
    
    RecipeCategory getCategory(Identifier identifier);
    
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
    void registerAutoCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle);
    
    /**
     * Removes the auto crafting button
     *
     * @param category the category of the button
     */
    default void removeAutoCraftButton(Identifier category) {
        registerAutoCraftButtonArea(category, bounds -> null);
    }
    
    /**
     * Gets the map of all recipes visible to the player
     *
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes();
    
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
    
    /**
     * Checks if the display is visible by asking recipe visibility handlers
     *
     * @param display       the display to be checked
     * @param respectConfig whether it should respect the user's config
     * @return whether the display should be visible
     * @deprecated {@link RecipeHelper#isDisplayVisible(RecipeDisplay)} )}
     */
    @Deprecated
    default boolean isDisplayVisible(RecipeDisplay display, boolean respectConfig) {
        return isDisplayVisible(display);
    }
    
    boolean isDisplayNotVisible(RecipeDisplay display);
    
    /**
     * Checks if the display is visible by asking recipe visibility handlers
     *
     * @param display the display to be checked
     * @return whether the display should be visible
     */
    boolean isDisplayVisible(RecipeDisplay display);
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Predicate<Recipe> recipeFilter, Function<T, RecipeDisplay> mappingFunction);
    
    /**
     * Registers a live recipe generator.
     *
     * @param liveRecipeGenerator the generator to register
     * @apiNote Still work in progress
     */
    void registerLiveRecipeGenerator(LiveRecipeGenerator<?> liveRecipeGenerator);
    
    void registerScreenClickArea(Rectangle rectangle, Class<? extends AbstractContainerScreen<?>> screenClass, Identifier... categories);
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction);
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Function<Recipe, Boolean> recipeFilter, Function<T, RecipeDisplay> mappingFunction);
    
    List<RecipeHelper.ScreenClickArea> getScreenClickAreas();
    
    boolean arePluginsLoading();
    
    interface ScreenClickArea {
        Class<? extends AbstractContainerScreen> getScreenClass();
        
        Rectangle getRectangle();
        
        Identifier[] getCategories();
    }
    
}

