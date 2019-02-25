package me.shedaniel.rei.api;

import me.shedaniel.rei.client.RecipeHelper;

public interface IRecipePlugin {
    
    default public void onFirstLoad(IPluginDisabler pluginDisabler) {}
    
    public void registerItems(IItemRegisterer itemRegisterer);
    
    public void registerPluginCategories(IRecipeHelper recipeHelper);
    
    public void registerRecipeDisplays(IRecipeHelper recipeHelper);
    
    public void registerSpeedCraft(IRecipeHelper recipeHelper);
    
    default public int getPriority() {
        return 0;
    }
    
}
