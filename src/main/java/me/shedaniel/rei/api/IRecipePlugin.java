package me.shedaniel.rei.api;

import me.shedaniel.rei.client.RecipeHelper;

public interface IRecipePlugin {
    
    default public void onFirstLoad(IPluginDisabler pluginDisabler) {}
    
    public void registerItems(IItemRegisterer itemRegisterer);
    
    public void registerPluginCategories(RecipeHelper recipeHelper);
    
    public void registerRecipeDisplays(RecipeHelper recipeHelper);
    
    public void registerSpeedCraft(RecipeHelper recipeHelper);
    
    default public int getPriority() {
        return 0;
    }
    
}
