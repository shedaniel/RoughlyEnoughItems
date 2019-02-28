package me.shedaniel.rei.api;

public interface IRecipePlugin {
    
    default void onFirstLoad(PluginDisabler pluginDisabler) {}
    
    void registerItems(ItemRegisterer itemRegisterer);
    
    void registerPluginCategories(RecipeHelper recipeHelper);
    
    void registerRecipeDisplays(RecipeHelper recipeHelper);
    
    void registerSpeedCraft(RecipeHelper recipeHelper);
    
    default int getPriority() {
        return 0;
    }
    
}
