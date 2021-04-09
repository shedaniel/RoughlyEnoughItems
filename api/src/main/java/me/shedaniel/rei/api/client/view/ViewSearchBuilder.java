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

package me.shedaniel.rei.api.client.view;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ViewSearchBuilder {
    static ViewSearchBuilder builder() {
        return ClientInternals.createViewSearchBuilder();
    }
    
    ViewSearchBuilder addCategory(CategoryIdentifier<?> category);
    
    ViewSearchBuilder addCategories(Collection<CategoryIdentifier<?>> categories);
    
    default ViewSearchBuilder addAllCategories() {
        return addCategories(CollectionUtils.map(CategoryRegistry.getInstance(), CategoryRegistry.CategoryConfiguration::getCategoryIdentifier));
    }
    
    Set<CategoryIdentifier<?>> getCategories();
    
    <T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack);
    
    List<EntryStack<?>> getRecipesFor();
    
    <T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack);
    
    List<EntryStack<?>> getUsagesFor();
    
    ViewSearchBuilder setPreferredOpenedCategory(@Nullable CategoryIdentifier<?> category);
    
    @Nullable
    CategoryIdentifier<?> getPreferredOpenedCategory();
    
    ViewSearchBuilder fillPreferredOpenedCategory();
    
    <T> ViewSearchBuilder setInputNotice(@Nullable EntryStack<T> stack);
    
    @Nullable
    EntryStack<?> getInputNotice();
    
    <T> ViewSearchBuilder setOutputNotice(@Nullable EntryStack<T> stack);
    
    @Nullable
    EntryStack<?> getOutputNotice();
    
    Map<DisplayCategory<?>, List<Display>> buildMap();
    
    
}