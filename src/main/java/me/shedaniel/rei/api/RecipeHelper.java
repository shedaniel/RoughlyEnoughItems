package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecipeHelper {
    
    public static RecipeHelper getInstance() {
        return RoughlyEnoughItemsCore.getRecipeHelper();
    }
    
    public int getRecipeCount();
    
    public List<ItemStack> findCraftableByItems(List<ItemStack> inventoryItems);
    
    public void registerCategory(IRecipeCategory category);
    
    public void registerDisplay(Identifier categoryIdentifier, IRecipeDisplay display);
    
    public Map<IRecipeCategory, List<IRecipeDisplay>> getRecipesFor(ItemStack stack);
    
    public RecipeManager getRecipeManager();
    
    public List<IRecipeCategory> getAllCategories();
    
    public Map<IRecipeCategory, List<IRecipeDisplay>> getUsagesFor(ItemStack stack);
    
    public Optional<SpeedCraftAreaSupplier> getSpeedCraftButtonArea(IRecipeCategory category);
    
    public void registerSpeedCraftButtonArea(Identifier category, SpeedCraftAreaSupplier rectangle);
    
    public List<SpeedCraftFunctional> getSpeedCraftFunctional(IRecipeCategory category);
    
    public void registerSpeedCraftFunctional(Identifier category, SpeedCraftFunctional functional);
    
    Map<IRecipeCategory, List<IRecipeDisplay>> getAllRecipes();
    
}
