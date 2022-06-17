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

import com.google.common.base.MoreObjects;
import dev.architectury.event.EventResult;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEIRecipeManager implements IRecipeManager {
    INSTANCE;
    
    Set<CategoryIdentifier<?>> hiddenCategories = new HashSet<>();
    Map<CategoryIdentifier<?>, Set<Object>> hiddenRecipes = new HashMap<>();
    public DisplayPredicate displayPredicate = new DisplayPredicate();
    public CategoryPredicate categoryPredicate = new CategoryPredicate();
    
    @Override
    public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
        return new JEIRecipeLookup<>(recipeType.categoryId());
    }
    
    @Override
    public IRecipeCategoriesLookup createRecipeCategoryLookup() {
        return new JEIRecipeCategoriesLookup();
    }
    
    @Override
    public IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType) {
        return new JEICatalystLookup(recipeType.categoryId());
    }
    
    @Override
    public <T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
        hiddenRecipes.computeIfAbsent(recipeType.categoryId(), $ -> new HashSet<>()).addAll(recipes);
    }
    
    @Override
    public <T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes) {
        CategoryIdentifier<Display> categoryIdentifier = recipeType.categoryId();
        hiddenRecipes.computeIfAbsent(categoryIdentifier, $ -> new HashSet<>()).removeAll(recipes);
        if (hiddenRecipes.get(categoryIdentifier).isEmpty()) {
            hiddenRecipes.remove(categoryIdentifier);
        }
    }
    
    @Override
    public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
        JEIRecipeRegistration.addRecipes0(recipes, recipeType.getUid());
    }
    
    @Override
    public void hideRecipeCategory(RecipeType<?> recipeType) {
        this.hiddenCategories.add(recipeType.categoryId());
    }
    
    @Override
    public void unhideRecipeCategory(RecipeType<?> recipeType) {
        this.hiddenCategories.remove(recipeType.categoryId());
    }
    
    @Override
    @Nullable
    public <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, @Nullable IFocus<?> focus) {
        throw TODO();
    }
    
    public class DisplayPredicate implements DisplayVisibilityPredicate {
        @Override
        public EventResult handleDisplay(DisplayCategory<?> category, Display display) {
            Set<Object> hidden = hiddenRecipes.get(category.getCategoryIdentifier());
            if (hidden != null && hidden.contains(MoreObjects.firstNonNull(display.jeiValue(), display))) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        }
    }
    
    public class CategoryPredicate implements CategoryVisibilityPredicate {
        @Override
        public EventResult handleCategory(DisplayCategory<?> category) {
            if (hiddenCategories.contains(category.getCategoryIdentifier())) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        }
    }
}
