/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api.plugins;

import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.ItemRegistry;
import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.api.RecipeHelper;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;

public interface REIPluginV0 extends REIPluginEntry {
    
    SemanticVersion getMinimumVersion() throws VersionParsingException;
    
    /**
     * On register of the plugin
     */
    default void onFirstLoad() {
    }
    
    /**
     * Registers items on the item panel
     *
     * @param itemRegistry the helper class
     */
    default void registerItems(ItemRegistry itemRegistry) {
    }
    
    /**
     * Registers categories
     *
     * @param recipeHelper the helper class
     */
    default void registerPluginCategories(RecipeHelper recipeHelper) {
    }
    
    /**
     * Registers displays for categories
     *
     * @param recipeHelper the helper class
     */
    default void registerRecipeDisplays(RecipeHelper recipeHelper) {
    }
    
    /**
     * Registers bounds handlers
     *
     * @param displayHelper the helper class
     */
    default void registerBounds(DisplayHelper displayHelper) {
    }
    
    /**
     * Register other stuff
     *
     * @param recipeHelper the helper class
     */
    default void registerOthers(RecipeHelper recipeHelper) {
    }
    
}
