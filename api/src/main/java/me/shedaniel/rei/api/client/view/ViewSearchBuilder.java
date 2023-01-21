/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.display.DisplaySpec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Builder for querying displays.
 */
public interface ViewSearchBuilder {
    /**
     * Creates a new {@link ViewSearchBuilder} for looking up displays.
     */
    static ViewSearchBuilder builder() {
        return ClientInternals.createViewSearchBuilder();
    }
    
    /**
     * Adds all displays from the given {@link DisplayCategory} to the search.
     *
     * @param category the category to add
     * @return the {@link ViewSearchBuilder} for chaining
     */
    ViewSearchBuilder addCategory(CategoryIdentifier<?> category);
    
    /**
     * Adds all displays from the given {@link DisplayCategory}s to the search.
     *
     * @param categories the categories to add
     * @return the {@link ViewSearchBuilder} for chaining
     */
    ViewSearchBuilder addCategories(Collection<CategoryIdentifier<?>> categories);
    
    /**
     * Adds all displays from all the {@link CategoryRegistry}s to the search.
     *
     * @return the {@link ViewSearchBuilder} for chaining
     */
    default ViewSearchBuilder addAllCategories() {
        return addCategories(CollectionUtils.map(CategoryRegistry.getInstance(), CategoryRegistry.CategoryConfiguration::getCategoryIdentifier));
    }
    
    /**
     * Returns the set of {@link CategoryIdentifier}s that will be used to add all the {@link Display}s to the search.
     *
     * @return the set of {@link CategoryIdentifier}s
     */
    Set<CategoryIdentifier<?>> getCategories();
    
    /**
     * Filters the search to only include {@link Display}s that are in the given {@link CategoryIdentifier}.
     *
     * @param category the category to filter by
     * @return the {@link ViewSearchBuilder} for chaining
     */
    ViewSearchBuilder filterCategory(CategoryIdentifier<?> category);
    
    /**
     * Filters the search to only include {@link Display}s that are in the given {@link CategoryIdentifier}s.
     *
     * @param categories the categories to filter by
     * @return the {@link ViewSearchBuilder} for chaining
     */
    ViewSearchBuilder filterCategories(Collection<CategoryIdentifier<?>> categories);
    
    /**
     * Returns the set of {@link CategoryIdentifier}s that will be used to filter the search.
     *
     * @return the set of {@link CategoryIdentifier}s
     */
    Set<CategoryIdentifier<?>> getFilteringCategories();
    
    /**
     * Filters the search to only include {@link Display}s that are recipes for the given {@link EntryStack}.
     * <p>
     * This will include any {@link Display}s containing {@link Display#getOutputEntries()} that match for the given {@link EntryStack}.
     *
     * @param stack the stack to filter by
     * @param <T>   the type of the stack
     * @return the {@link ViewSearchBuilder} for chaining
     */
    <T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack);
    
    /**
     * Returns the list of {@link EntryStack}s that will be used to filter the search for the recipes of the given {@link EntryStack}.
     *
     * @return the list of {@link EntryStack}s
     * @see ViewSearchBuilder#addRecipesFor(EntryStack)
     */
    List<EntryStack<?>> getRecipesFor();
    
    /**
     * Filters the search to only include {@link Display}s that are usages for the given {@link EntryStack}.
     * <p>
     * This will include any {@link Display}s containing {@link Display#getInputEntries()} that match for the given {@link EntryStack}.
     *
     * @param stack the stack to filter by
     * @param <T>   the type of the stack
     * @return the {@link ViewSearchBuilder} for chaining
     */
    <T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack);
    
    /**
     * Returns the list of {@link EntryStack}s that will be used to filter the search for the usages of the given {@link EntryStack}.
     *
     * @return the list of {@link EntryStack}s
     * @see ViewSearchBuilder#addUsagesFor(EntryStack)
     */
    List<EntryStack<?>> getUsagesFor();
    
    /**
     * Sets the preferred opened {@link DisplayCategory} for the view.
     *
     * @param category the preferred opened {@link DisplayCategory}, or {@code null} to use the first category
     * @return the {@link ViewSearchBuilder} for chaining
     */
    ViewSearchBuilder setPreferredOpenedCategory(@Nullable CategoryIdentifier<?> category);
    
    /**
     * Returns the preferred opened {@link DisplayCategory} for the view, or {@code null} if none is set.
     *
     * @return the preferred opened {@link DisplayCategory}
     */
    @Nullable
    CategoryIdentifier<?> getPreferredOpenedCategory();
    
    /**
     * Returns the built map of {@link DisplaySpec}s to {@link DisplayCategory}s.
     * <p>
     * This should not be used by plugins, plugins should instead opt-for the {@link #open()} method
     * to open the view.
     *
     * @return the built map
     */
    @ApiStatus.Internal
    Map<DisplayCategory<?>, List<DisplaySpec>> buildMapInternal();
    
    /**
     * Returns the stream of {@link DisplaySpec}s that match the search.
     *
     * @return the stream of {@link DisplaySpec}s
     */
    @ApiStatus.Experimental
    Stream<DisplaySpec> streamDisplays();
    
    /**
     * Returns whether the search is merging equal {@link DisplaySpec}s.
     *
     * @return whether the search is merging equal {@link DisplaySpec}s
     * @see ConfigObject#doMergeDisplayUnderOne() for the config option
     * @see DisplayCategory#getDisplayMerger() for the merging strategy
     */
    boolean isMergingDisplays();
    
    /**
     * Sets whether the search is merging equal {@link DisplaySpec}s.
     *
     * @param mergingDisplays whether the search is merging equal {@link DisplaySpec}s
     * @return the {@link ViewSearchBuilder} for chaining
     * @see ConfigObject#doMergeDisplayUnderOne() for the default config option
     * @see DisplayCategory#getDisplayMerger() for the merging strategy
     */
    ViewSearchBuilder mergingDisplays(boolean mergingDisplays);
    
    /**
     * Returns whether the search is processing visibility handlers.
     *
     * @return whether the search is processing visibility handlers
     * @see me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate
     * @see me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate
     */
    boolean isProcessingVisibilityHandlers();
    
    /**
     * Sets whether the search is processing visibility handlers.
     *
     * @param processingVisibilityHandlers whether the search is processing visibility handlers
     * @return the {@link ViewSearchBuilder} for chaining
     * @see me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate
     * @see me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate
     */
    ViewSearchBuilder processingVisibilityHandlers(boolean processingVisibilityHandlers);
    
    /**
     * Opens the view after the search is complete.
     *
     * @return whether the view was opened
     */
    default boolean open() {
        return ClientHelper.getInstance().openView(this);
    }
}