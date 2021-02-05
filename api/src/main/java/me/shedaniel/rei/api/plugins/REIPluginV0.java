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

import me.shedaniel.rei.api.DisplayBoundsRegistry;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.REIPlugin;
import me.shedaniel.rei.api.RecipeRegistry;
import me.shedaniel.rei.api.ingredient.entry.EntryTypeRegistry;
import me.shedaniel.rei.api.registry.CategoryRegistry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.OverrideOnly
public interface REIPluginV0 extends REIPlugin {
    /**
     * Registers the types of entries
     */
    @ApiStatus.OverrideOnly
    default void registerEntryTypes(EntryTypeRegistry registry) {
        
    }
    
    /**
     * Registers entries on the item panel
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerEntries(EntryRegistry registry) {
    }
    
    /**
     * Registers categories
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerCategories(CategoryRegistry registry) {
    }
    
    /**
     * Registers displays for categories
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerDisplays(RecipeRegistry registry) {
    }
    
    /**
     * Registers bounds handlers
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerBounds(DisplayBoundsRegistry registry) {
    }
    
    /**
     * Register other stuff
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerOthers(RecipeRegistry registry) {
    }
    
    @ApiStatus.OverrideOnly
    default void preRegister() {
    }
    
    @ApiStatus.OverrideOnly
    default void postRegister() {
    }
    
}
