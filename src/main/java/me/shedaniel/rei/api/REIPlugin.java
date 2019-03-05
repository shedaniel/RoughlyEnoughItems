package me.shedaniel.rei.api;

public interface REIPlugin {
    
    default void onFirstLoad(PluginDisabler pluginDisabler) {}
    
    void registerItems(ItemRegistry itemRegistry);
    
    void registerPluginCategories(RecipeHelper recipeHelper);
    
    void registerRecipeDisplays(RecipeHelper recipeHelper);
    
    void registerSpeedCraft(RecipeHelper recipeHelper);
    
    default int getPriority() {
        return 0;
    }
    
}
