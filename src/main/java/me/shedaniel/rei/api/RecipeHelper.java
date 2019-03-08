package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecipeHelper {
    
    static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
    int getRecipeCount();
    
    List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems);
    
    void registerCategory(RecipeCategory category);
    
    void registerDisplay(ResourceLocation category, RecipeDisplay display);
    
    Map<RecipeCategory, List<RecipeDisplay>> getRecipesFor(ItemStack stack);
    
    RecipeManager getRecipeManager();
    
    List<RecipeCategory> getAllCategories();
    
    Map<RecipeCategory, List<RecipeDisplay>> getUsagesFor(ItemStack stack);
    
    Optional<ButtonAreaSupplier> getSpeedCraftButtonArea(RecipeCategory category);
    
    void registerSpeedCraftButtonArea(ResourceLocation category, ButtonAreaSupplier rectangle);
    
    List<SpeedCraftFunctional> getSpeedCraftFunctional(RecipeCategory category);
    
    void registerSpeedCraftFunctional(ResourceLocation category, SpeedCraftFunctional functional);
    
    Map<RecipeCategory, List<RecipeDisplay>> getAllRecipes();
    
}
