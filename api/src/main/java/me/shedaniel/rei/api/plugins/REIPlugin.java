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

import me.shedaniel.rei.api.registry.screens.ScreenRegistry;
import me.shedaniel.rei.api.DisplayRegistry;
import me.shedaniel.rei.api.registry.EntryRegistry;
import me.shedaniel.rei.api.ingredient.entry.EntryTypeRegistry;
import me.shedaniel.rei.api.registry.CategoryRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.OverrideOnly
public interface REIPlugin extends Comparable<REIPlugin> {
    /**
     * @return the priority of the plugin, the smaller the number, the earlier it is called.
     */
    default int getPriority() {
        return 0;
    }
    
    default String getPluginName() {
        Class<? extends REIPlugin> self = getClass();
        String simpleName = self.getSimpleName();
        return simpleName == null ? self.getName() : simpleName;
    }
    
    @Override
    default int compareTo(@NotNull REIPlugin o) {
        return Double.compare(getPriority(), o.getPriority());
    }
    
    /**
     * Registers the types of entries
     */
    @ApiStatus.OverrideOnly
    default void registerEntryTypes(EntryTypeRegistry registry) {
        
    }
    
    /**
     * Registers entries on the entry panel.
     *
     * @param registry the entry registry
     */
    @ApiStatus.OverrideOnly
    default void registerEntries(EntryRegistry registry) {
    }
    
    /**
     * Registers categories
     *
     * @param registry the category registry
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
    default void registerDisplays(DisplayRegistry registry) {
    }
    
    /**
     * Registers bounds handlers
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerScreens(ScreenRegistry registry) {
    }
    
    /**
     * Register other stuff
     *
     * @param registry the helper class
     */
    @ApiStatus.OverrideOnly
    default void registerOthers(DisplayRegistry registry) {
    }
    
    @ApiStatus.OverrideOnly
    default void preRegister() {
    }
    
    @ApiStatus.OverrideOnly
    default void postRegister() {
    }
}
