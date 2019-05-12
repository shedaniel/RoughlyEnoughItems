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

public interface RecipeHelper {
    
    /**
     * @return the api instance of {@link me.shedaniel.rei.client.RecipeHelperImpl}
     */
    static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
    int getRecipeCount();
    
    List<Recipe> getVanillaSortedRecipes();
    
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
    
    Map<RecipeCategory, List<RecipeDisplay>> getRecipesFor(ItemStack stack);
    
    /**
     * Gets the vanilla recipe manager
     *
     * @return the recipe manager
     */
    RecipeManager getRecipeManager();
    
    /**
     * Gets all registered categories
     * @return the list of categories
     */
    List<RecipeCategory> getAllCategories();
    
    Map<RecipeCategory, List<RecipeDisplay>> getUsagesFor(ItemStack stack);
    
    Optional<ButtonAreaSupplier> getSpeedCraftButtonArea(RecipeCategory category);
    
    void registerSpeedCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle);
    
    void registerDefaultSpeedCraftButtonArea(Identifier category);
    
    List<SpeedCraftFunctional> getSpeedCraftFunctional(RecipeCategory category);
    
    void registerSpeedCraftFunctional(Identifier category, SpeedCraftFunctional functional);
    
    Map<RecipeCategory, List<RecipeDisplay>> getAllRecipes();
    
    void registerRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler);
    
    void unregisterRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler);
    
    List<DisplayVisibilityHandler> getDisplayVisibilityHandlers();
    
    boolean isDisplayVisible(RecipeDisplay display, boolean respectConfig);
    
    Optional<DisplaySettings> getCachedCategorySettings(Identifier category);
    
}
