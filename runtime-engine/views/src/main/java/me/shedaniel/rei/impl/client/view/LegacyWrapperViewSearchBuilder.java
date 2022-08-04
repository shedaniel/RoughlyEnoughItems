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

package me.shedaniel.rei.impl.client.view;

import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.display.DisplaySpec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class LegacyWrapperViewSearchBuilder extends AbstractViewSearchBuilder {
    private final Map<DisplayCategory<?>, List<DisplaySpec>> map;
    @Nullable
    private EntryStack<?> inputNotice;
    @Nullable
    private EntryStack<?> outputNotice;
    @Nullable
    private CategoryIdentifier<?> preferredOpenedCategory = null;
    
    public LegacyWrapperViewSearchBuilder(Map<DisplayCategory<?>, List<DisplaySpec>> map) {
        this.map = map;
    }
    
    @Override
    public ViewSearchBuilder addCategory(CategoryIdentifier<?> category) {
        return this;
    }
    
    @Override
    public ViewSearchBuilder addCategories(Collection<CategoryIdentifier<?>> categories) {
        return this;
    }
    
    @Override
    public Set<CategoryIdentifier<?>> getCategories() {
        return Collections.emptySet();
    }
    
    @Override
    public ViewSearchBuilder filterCategory(CategoryIdentifier<?> category) {
        return this;
    }
    
    @Override
    public ViewSearchBuilder filterCategories(Collection<CategoryIdentifier<?>> categories) {
        return this;
    }
    
    @Override
    public Set<CategoryIdentifier<?>> getFilteringCategories() {
        return Collections.emptySet();
    }
    
    @Override
    public <T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack) {
        return this;
    }
    
    @Override
    public List<EntryStack<?>> getRecipesFor() {
        return inputNotice == null ? Collections.emptyList() : Collections.singletonList(outputNotice);
    }
    
    @Override
    public <T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack) {
        return this;
    }
    
    @Override
    public List<EntryStack<?>> getUsagesFor() {
        return inputNotice == null ? Collections.emptyList() : Collections.singletonList(inputNotice);
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
    
    public <T> LegacyWrapperViewSearchBuilder addInputNotice(@Nullable EntryStack<T> stack) {
        this.inputNotice = stack;
        return this;
    }
    
    public <T> LegacyWrapperViewSearchBuilder addOutputNotice(@Nullable EntryStack<T> stack) {
        this.outputNotice = stack;
        return this;
    }
    
    @Override
    public Map<DisplayCategory<?>, List<DisplaySpec>> buildMapInternal() {
        fillPreferredOpenedCategory();
        return this.map;
    }
    
    @Override
    public boolean isMergingDisplays() {
        return true;
    }
    
    @Override
    public ViewSearchBuilder mergingDisplays(boolean mergingDisplays) {
        return this;
    }
    
    @Override
    public boolean isProcessingVisibilityHandlers() {
        return false;
    }
    
    @Override
    public ViewSearchBuilder processingVisibilityHandlers(boolean processingVisibilityHandlers) {
        return this;
    }
}
