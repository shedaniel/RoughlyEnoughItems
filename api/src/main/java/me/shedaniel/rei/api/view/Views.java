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

package me.shedaniel.rei.api.view;

import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.impl.Internals;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Views {
    static Views getInstance() {
        return Internals.getViews();
    }
    
    Map<DisplayCategory<?>, List<Display>> buildMapFor(ViewSearchBuilder builder);
    
    /**
     * Returns all craftable items from materials.
     *
     * @param inventoryItems the materials
     * @return the list of craftable entries
     */
    Collection<EntryStack<?>> findCraftableEntriesByItems(Iterable<? extends EntryStack<?>> inventoryItems);
    
    /**
     * Returns a map of recipes for an entry
     *
     * @param stack the stack to be crafted
     * @return the map of recipes
     */
    default <T> Map<DisplayCategory<?>, List<Display>> getRecipesFor(EntryStack<T> stack) {
        return buildMapFor(ViewSearchBuilder.builder().addRecipesFor(stack).setInputNotice(stack));
    }
    
    /**
     * Returns a map of usages for an entry
     *
     * @param stack the stack to be used
     * @return the map of recipes
     */
    default <T> Map<DisplayCategory<?>, List<Display>> getUsagesFor(EntryStack<T> stack) {
        return buildMapFor(ViewSearchBuilder.builder().addUsagesFor(stack).setInputNotice(stack));
    }
    
    default Map<DisplayCategory<?>, List<Display>> getAllRecipes() {
        return buildMapFor(ViewSearchBuilder.builder().addAllCategories());
    }
}
