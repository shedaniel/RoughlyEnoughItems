/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api.plugins;

import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.annotations.ToBeRemoved;

public interface REIPluginV0 extends REIPluginEntry {
    
    /**
     * Registers entries on the item panel
     *
     * @param entryRegistry the helper class
     */
    default void registerEntries(EntryRegistry entryRegistry) {
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
    
    default void preRegister() {
    }
    
    default void postRegister() {
    }
    
}
