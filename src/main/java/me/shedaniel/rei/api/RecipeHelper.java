/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.annotations.ToBeRemoved;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface RecipeHelper {
    
    /**
     * @return the api instance of {@link me.shedaniel.rei.impl.RecipeHelperImpl}
     */
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
     * @return the list of craftable items
     */
    default List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (EntryStack item : findCraftableEntriesByItems(inventoryItems)) {
            if (item.getItemStack() != null)
                itemStacks.add(item.getItemStack());
        }
        return itemStacks;
    }
    
    /**
     * Gets all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    List<EntryStack> findCraftableEntriesByItems(List<ItemStack> inventoryItems);
    
    /**
     * Registers a category
     *
     * @param category the category to register
     */
    void registerCategory(RecipeCategory category);
    
    /**
     * Registers the working stations of a category
     *
     * @param category        the category
     * @param workingStations the working stations
     */
    void registerWorkingStations(Identifier category, List<ItemStack>... workingStations);
    
    /**
     * Registers the working stations of a category
     *
     * @param category        the category
     * @param workingStations the working stations
     */
    void registerWorkingStations(Identifier category, ItemStack... workingStations);
    
    List<List<ItemStack>> getWorkingStations(Identifier category);
    
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
    Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(EntryStack stack);
    
    @ToBeRemoved
    @Deprecated
    default Map<RecipeCategory<?>, List<RecipeDisplay>> getRecipesFor(ItemStack stack) {
        return getRecipesFor(EntryStack.create(stack));
    }
    
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
    List<RecipeCategory> getAllCategories();
    
    /**
     * Gets a map of usages for an itemstack
     *
     * @param stack the stack to be used
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getUsagesFor(EntryStack stack);
    
    @ToBeRemoved
    @Deprecated
    default Map<RecipeCategory<?>, List<RecipeDisplay>> getUsagesFor(ItemStack stack) {
        return getUsagesFor(EntryStack.create(stack));
    }
    
    /**
     * Gets the optional of the speed crafting button area from a category
     *
     * @param category the category of the display
     * @return the optional of speed crafting button area
     */
    Optional<ButtonAreaSupplier> getAutoCraftButtonArea(RecipeCategory category);
    
    /**
     * Registers a speed crafting button area
     *
     * @param category  the category of the button area
     * @param rectangle the button area
     */
    void registerAutoCraftButtonArea(Identifier category, ButtonAreaSupplier rectangle);
    
    /**
     * Removes the speed crafting button
     *
     * @param category the category of the button
     */
    default void removeSpeedCraftButton(Identifier category) {
        registerAutoCraftButtonArea(category, bounds -> null);
    }
    
    /**
     * @param category the category of the button area
     * @deprecated Not required anymore
     */
    @Deprecated
    void registerDefaultSpeedCraftButtonArea(Identifier category);
    
    /**
     * Gets the map of all recipes visible to the player
     *
     * @return the map of recipes
     */
    Map<RecipeCategory<?>, List<RecipeDisplay>> getAllRecipes();
    
    List<RecipeDisplay> getAllRecipesFromCategory(RecipeCategory category);
    
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
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Predicate<Recipe> recipeFilter, Function<T, RecipeDisplay> mappingFunction);
    
    /**
     * Registers a live recipe generator.
     *
     * @param liveRecipeGenerator the generator to register
     * @apiNote Still work in progress
     */
    void registerLiveRecipeGenerator(LiveRecipeGenerator<?> liveRecipeGenerator);
    
    void registerScreenClickArea(Rectangle rectangle, Class<? extends AbstractContainerScreen> screenClass, Identifier... categories);
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Class<T> recipeClass, Function<T, RecipeDisplay> mappingFunction);
    
    <T extends Recipe<?>> void registerRecipes(Identifier category, Function<Recipe, Boolean> recipeFilter, Function<T, RecipeDisplay> mappingFunction);
    
    List<RecipeHelper.ScreenClickArea> getScreenClickAreas();
    
    interface ScreenClickArea {
        Class<? extends AbstractContainerScreen> getScreenClass();
        
        Rectangle getRectangle();
        
        Identifier[] getCategories();
    }
    
}
