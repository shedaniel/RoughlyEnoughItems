package me.shedaniel.rei.api;

import net.minecraft.util.Identifier;

/**
 * Get base class of a REI plugin.
 */
public interface REIPluginEntry {
    
    /**
     * On register of the plugin
     *
     * @param pluginDisabler the helper class to disable other plugins
     */
    default void onFirstLoad(PluginDisabler pluginDisabler) {}
    
    /**
     * Registers items on the item panel
     *
     * @param itemRegistry the helper class
     */
    default void registerItems(ItemRegistry itemRegistry) {}
    
    /**
     * Registers categories
     *
     * @param recipeHelper the helper class
     */
    default void registerPluginCategories(RecipeHelper recipeHelper) {}
    
    /**
     * Registers displays for categories
     *
     * @param recipeHelper the helper class
     */
    default void registerRecipeDisplays(RecipeHelper recipeHelper) {}
    
    /**
     * Not called anymore!
     *
     * @param recipeHelper
     * @see REIPluginEntry#registerOthers(RecipeHelper)
     */
    @Deprecated
    default void registerSpeedCraft(RecipeHelper recipeHelper) {}
    
    /**
     * Registers bounds handlers
     *
     * @param displayHelper the helper class
     */
    default void registerBounds(DisplayHelper displayHelper) {}
    
    /**
     * Register other stuff
     *
     * @param recipeHelper the helper class
     */
    default void registerOthers(RecipeHelper recipeHelper) {}
    
    /**
     * Gets the priority of the plugin.
     *
     * @return the priority
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Get the identifier of the plugin
     *
     * @return the identifier
     */
    Identifier getPluginIdentifier();
    
}
