/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api.plugins;

import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.api.RecipeHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.OverrideOnly
public interface REIPluginV0 extends REIPluginEntry {
    
    /**
     * Registers entries on the item panel
     *
     * @param entryRegistry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerEntries(EntryRegistry entryRegistry) {
    }
    
    /**
     * Registers categories
     *
     * @param recipeHelper the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerPluginCategories(RecipeHelper recipeHelper) {
    }
    
    /**
     * Registers displays for categories
     *
     * @param recipeHelper the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerRecipeDisplays(RecipeHelper recipeHelper) {
    }
    
    /**
     * Registers bounds handlers
     *
     * @param displayHelper the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerBounds(DisplayHelper displayHelper) {
    }
    
    /**
     * Register other stuff
     *
     * @param recipeHelper the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerOthers(RecipeHelper recipeHelper) {
    }
    
    @ApiStatus.OverrideOnly
    default void preRegister() {
    }
    
    @ApiStatus.OverrideOnly
    default void postRegister() {
    }
    
}
