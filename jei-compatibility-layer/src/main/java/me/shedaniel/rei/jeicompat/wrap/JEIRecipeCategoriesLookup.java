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

package me.shedaniel.rei.jeicompat.wrap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategoriesLookup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIRecipeCategoriesLookup implements IRecipeCategoriesLookup {

    private Set<CategoryIdentifier<?>> categoryFilter = Set.of();
    private Set<CategoryIdentifier<?>> focusFilter = Set.of();
    private boolean includeHidden = false;

    @Override
    public IRecipeCategoriesLookup limitTypes(Collection<RecipeType<?>> recipeTypes) {
        Preconditions.checkNotNull(recipeTypes, "recipeTypes");
        this.categoryFilter = CollectionUtils.mapToSet(recipeTypes, type -> type.categoryId());
        return this;
    }

    @Override
    public IRecipeCategoriesLookup limitFocus(Collection<? extends IFocus<?>> focuses) {
        Preconditions.checkNotNull(focuses, "focuses");
        if (!focuses.isEmpty()) {
            ViewSearchBuilder builder = ViewSearchBuilder.builder();
            for (IFocus<?> focus : focuses) {
                EntryStack<?> stack = focus.getTypedValue().unwrapStack();
                if (focus.getRole() == RecipeIngredientRole.INPUT || focus.getRole() == RecipeIngredientRole.CATALYST) {
                    builder.addUsagesFor(stack);
                } else {
                    builder.addRecipesFor(stack);
                }
            }
            this.focusFilter = CollectionUtils.mapToSet(builder.buildMapInternal().keySet(), DisplayCategory::getCategoryIdentifier);
        }
        return this;
    }

    @Override
    public IRecipeCategoriesLookup includeHidden() {
        this.includeHidden = true;
        return this;
    }

    @Override
    public Stream<IRecipeCategory<?>> get() {
        Stream<? extends IRecipeCategory<?>> stream = CollectionUtils.filterAndMap(CategoryRegistry.getInstance(), cat -> {
            if (!includeHidden && CategoryRegistry.getInstance().isCategoryInvisible(cat.getCategory())) {
                return false;
            }

            Set<CategoryIdentifier<?>> filter = Sets.intersection(categoryFilter, focusFilter);
            return filter.isEmpty() || filter.contains(cat.getCategoryIdentifier());
        }, cat -> cat.getCategory().wrapCategory()).stream();
        return (Stream<IRecipeCategory<?>>) stream;
    }
}
