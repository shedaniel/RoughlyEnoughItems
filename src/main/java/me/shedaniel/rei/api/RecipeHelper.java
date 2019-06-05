/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface RecipeHelper {
    
    /**
     * @return the api instance of {@link me.shedaniel.rei.client.RecipeHelperImpl}
     */
    static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
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
     * @return the list of craftable items
     */
    List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems);
    
    /**
     * Registers a category
     *
     * @param category the category to register
     */
    void registerCategory(RecipeCategory category);
    
    /**
     * Registers a recipe display
     *
     * @param categoryIdentifier the category to display in
     * @param display            the recipe display
     */
    void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display);
    
    /**
     * Gets a map of recipes for an itemstack
     *
     * @param stack the stack to be crafted
     * @return the map of recipes
     */
    Map<RecipeCategory, List<RecipeDisplay>> getRecipesFor(ItemStack stack);
    
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
    List<RecipeCategory> getAllCategories();
    
    /**
     * Gets a map of usages for an itemstack
     *
     * @param stack the stack to be used
     * @return the map of recipes
     */
    Map<RecipeCategory, List<RecipeDisplay>> getUsagesFor(ItemStack stack);
    
    /**
     * Gets the optional of the speed crafting button area from a category
     *
     * @param category the category of the display
     * @return the optional of speed crafting button area
     */
    Optional<ButtonAreaSupplier> getSpeedCraftButtonArea(RecipeCategory category);
    
    /**
     * Registers a speed crafting button area
     *
     * @param category  the category of the button area
     * @param rectangle the button area
     */
    void registerSpeedCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle);
    
    /**
     * @param category the category of the button area
     * @deprecated Not required anymore
     */
    @Deprecated
    void registerDefaultSpeedCraftButtonArea(Identifier category);
    
    /**
     * Gets the speed crafting functional from a category
     *
     * @param category the category of the speed crafting functional
     * @return the list of speed crafting functionals
     */
    List<SpeedCraftFunctional> getSpeedCraftFunctional(RecipeCategory category);
    
    /**
     * Registers a speed crafting functional
     *
     * @param category   the category of the speed crafting functional
     * @param functional the functional to be registered
     */
    void registerSpeedCraftFunctional(Identifier category, SpeedCraftFunctional functional);
    
    /**
     * Gets the map of all recipes visible to the player
     *
     * @return the map of recipes
     */
    Map<RecipeCategory, List<RecipeDisplay>> getAllRecipes();
    
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
    boolean isDisplayVisible(RecipeDisplay display, boolean respectConfig);
    
    /**
     * Checks if the display is visible by asking recipe visibility handlers
     *
     * @param display the display to be checked
     * @return whether the display should be visible
     */
    boolean isDisplayVisible(RecipeDisplay display);
    
    /**
     * Gets the cached category setting by the category identifier
     *
     * @param category the identifier of the category
     * @return the optional of the category settings
     */
    Optional<DisplaySettings> getCachedCategorySettings(Identifier category);
    
    /**
     * Registers a live recipe generator.
     *
     * @param liveRecipeGenerator the generator to register
     * @apiNote Still work in progress
     */
    void registerLiveRecipeGenerator(LiveRecipeGenerator liveRecipeGenerator);
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction);
}
