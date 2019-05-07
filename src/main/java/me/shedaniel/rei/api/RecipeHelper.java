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
    
    static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
    int getRecipeCount();
    
    List<Recipe> getVanillaSortedRecipes();
    
    List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems);
    
    void registerCategory(RecipeCategory category);
    
    void registerDisplay(Identifier categoryIdentifier, RecipeDisplay display);
    
    Map<RecipeCategory, List<RecipeDisplay>> getRecipesFor(ItemStack stack);
    
    RecipeManager getRecipeManager();
    
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
