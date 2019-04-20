package me.shedaniel.rei.api;

import net.minecraft.util.Identifier;

public interface REIPluginEntry {
    
    default void onFirstLoad(PluginDisabler pluginDisabler) {}
    
    default void registerItems(ItemRegistry itemRegistry) {}
    
    default void registerPluginCategories(RecipeHelper recipeHelper) {}
    
    default void registerRecipeDisplays(RecipeHelper recipeHelper) {}
    
    @Deprecated
    default void registerSpeedCraft(RecipeHelper recipeHelper) {}
    
    default void registerBounds(DisplayHelper displayHelper) {}
    
    default void registerOthers(RecipeHelper recipeHelper) {}
    
    default int getPriority() {
        return 0;
    }
    
    Identifier getPluginIdentifier();
    
}
