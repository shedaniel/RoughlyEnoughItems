/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client;

import com.google.common.base.Suppliers;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import me.shedaniel.rei.impl.display.DisplaySpec;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public final class ViewSearchBuilderImpl extends AbstractViewSearchBuilder {
    private final Set<CategoryIdentifier<?>> filteringCategories = new HashSet<>();
    private final Set<CategoryIdentifier<?>> categories = new HashSet<>();
    private final List<EntryStack<?>> recipesFor = new ArrayList<>();
    private final List<EntryStack<?>> usagesFor = new ArrayList<>();
    @Nullable
    private CategoryIdentifier<?> preferredOpenedCategory = null;
    private boolean mergeDisplays = true;
    private boolean processVisibilityHandlers = true;
    private final Supplier<Map<DisplayCategory<?>, List<DisplaySpec>>> map = Suppliers.memoize(() -> ViewsImpl.buildMapFor(this));
    
    @Override
    public ViewSearchBuilder addCategory(CategoryIdentifier<?> category) {
        this.categories.add(category);
        return this;
    }
    
    @Override
    public ViewSearchBuilder addCategories(Collection<CategoryIdentifier<?>> categories) {
        this.categories.addAll(categories);
        return this;
    }
    
    @Override
    public Set<CategoryIdentifier<?>> getCategories() {
        return categories;
    }
    
    @Override
    public <T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack) {
        this.recipesFor.add(stack);
        return this;
    }
    
    @Override
    public List<EntryStack<?>> getRecipesFor() {
        return recipesFor;
    }
    
    @Override
    public <T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack) {
        this.usagesFor.add(stack);
        return this;
    }
    
    @Override
    public List<EntryStack<?>> getUsagesFor() {
        return usagesFor;
    }
    
    @Override
    public ViewSearchBuilder setPreferredOpenedCategory(@Nullable CategoryIdentifier<?> category) {
        this.preferredOpenedCategory = category;
        return this;
    }
    
    @Override
    @Nullable
    public CategoryIdentifier<?> getPreferredOpenedCategory() {
        return this.preferredOpenedCategory;
    }
    
    @Override
    public ViewSearchBuilder filterCategory(CategoryIdentifier<?> category) {
        this.filteringCategories.add(category);
        return this;
    }
    
    @Override
    public ViewSearchBuilder filterCategories(Collection<CategoryIdentifier<?>> categories) {
        this.filteringCategories.addAll(categories);
        return this;
    }
    
    @Override
    public Set<CategoryIdentifier<?>> getFilteringCategories() {
        return filteringCategories;
    }
    
    @Override
    public Map<DisplayCategory<?>, List<DisplaySpec>> buildMapInternal() {
        fillPreferredOpenedCategory();
        return this.map.get();
    }
    
    @Override
    public boolean isMergingDisplays() {
        return mergeDisplays;
    }
    
    @Override
    public ViewSearchBuilder mergingDisplays(boolean mergingDisplays) {
        this.mergeDisplays = mergingDisplays;
        return this;
    }
    
    @Override
    public boolean isProcessingVisibilityHandlers() {
        return processVisibilityHandlers;
    }
    
    @Override
    public ViewSearchBuilder processingVisibilityHandlers(boolean processingVisibilityHandlers) {
        this.processVisibilityHandlers = processingVisibilityHandlers;
        return this;
    }
}
