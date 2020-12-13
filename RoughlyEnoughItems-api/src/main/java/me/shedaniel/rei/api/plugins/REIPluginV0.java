/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api.plugins;

import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.entry.EntryTypeRegistry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.OverrideOnly
public interface REIPluginV0 extends REIPluginEntry {
    /**
     * Registers the types of entries
     */
    @ApiStatus.OverrideOnly
    default void registerEntryTypes(EntryTypeRegistry registry) {
        
    }
    
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
