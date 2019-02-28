package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.item.ItemStack;
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
    
    List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems);
    
    void registerCategory(IRecipeCategory category);
    
    void registerDisplay(Identifier categoryIdentifier, IRecipeDisplay display);
    
    Map<IRecipeCategory, List<IRecipeDisplay>> getRecipesFor(ItemStack stack);
    
    RecipeManager getRecipeManager();
    
    List<IRecipeCategory> getAllCategories();
    
    Map<IRecipeCategory, List<IRecipeDisplay>> getUsagesFor(ItemStack stack);
    
    Optional<SpeedCraftAreaSupplier> getSpeedCraftButtonArea(IRecipeCategory category);
    
    void registerSpeedCraftButtonArea(Identifier category, SpeedCraftAreaSupplier rectangle);
    
    List<SpeedCraftFunctional> getSpeedCraftFunctional(IRecipeCategory category);
    
    void registerSpeedCraftFunctional(Identifier category, SpeedCraftFunctional functional);
    
    Map<IRecipeCategory, List<IRecipeDisplay>> getAllRecipes();
    
}
