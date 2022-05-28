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
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
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
    @Nullable
    public IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden) {
        try {
            DisplayCategory<Display> category = CategoryRegistry.getInstance().get(recipeCategoryUid.categoryId()).getCategory();
            if (CategoryRegistry.getInstance().isCategoryVisible(category)) {
                return new JEIUnwrappedCategory<>(category);
            }
        } catch (NullPointerException ignored) {
        }
        return null;
    }
    
    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, List<? extends IFocus<?>> focuses, boolean includeHidden) {
        if (focuses != null && !focuses.isEmpty()) throw TODO();
        return JEIPluginDetector.wrapRecipes(recipeCategory.getRecipeType().categoryId(), !includeHidden);
    }
    
    @Override
    public List<ITypedIngredient<?>> getRecipeCatalystsTyped(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
        List<ITypedIngredient<?>> objects = new ArrayList<>();
        for (EntryIngredient stacks : CategoryRegistry.getInstance().get(recipeCategory.getRecipeType().categoryId()).getWorkstations()) {
            objects.addAll(CollectionUtils.map(stacks, JEIPluginDetector::typedJeiValue));
        }
        return objects;
    }
    
    @Override
    public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
        return JEIFocusFactory.INSTANCE.createFocus(mode, ingredient);
    }
    
    @Override
    public <V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden) {
        if (focus != null) throw TODO();
        return CollectionUtils.filterAndMap(CategoryRegistry.getInstance(), config -> includeHidden || CategoryRegistry.getInstance().isCategoryVisible(config.getCategory()),
                config -> new JEIUnwrappedCategory<>(config.getCategory()));
    }
    
    @Override
    public List<IRecipeCategory<?>> getRecipeCategories(Collection<? extends IFocus<?>> focuses, boolean includeHidden) {
        if (focuses != null && !focuses.isEmpty()) throw TODO();
        return CollectionUtils.filterAndMap(CategoryRegistry.getInstance(), config -> includeHidden || CategoryRegistry.getInstance().isCategoryVisible(config.getCategory()),
                config -> new JEIUnwrappedCategory<>(config.getCategory()));
    }
    
    @Override
    public <V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden) {
        if (focus != null) throw TODO();
        return CollectionUtils.map(recipeCategoryUids, id -> getRecipeCategory(id, includeHidden));
    }
    
    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden) {
        if (focus != null) throw TODO();
        return JEIPluginDetector.wrapRecipes(recipeCategory.getRecipeType().categoryId(), !includeHidden);
    }
    
    @Override
    public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden) {
        List<Object> objects = new ArrayList<>();
        for (EntryIngredient stacks : CategoryRegistry.getInstance().get(recipeCategory.getRecipeType().categoryId()).getWorkstations()) {
            objects.addAll(CollectionUtils.map(stacks, JEIPluginDetector::jeiValue));
        }
        return objects;
    }
    
    @Override
    public <R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType) {
        return new JEIRecipeLookup<>(recipeType.categoryId());
    }
    
    @Override
    public IRecipeCategoriesLookup createRecipeCategoryLookup() {
        return null;
    }
    
    @Override
    public IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType) {
        return null;
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
    
    @Override
    public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        Set<Object> recipes = hiddenRecipes.computeIfAbsent(recipeCategoryUid.categoryId(), $ -> new HashSet<>());
        recipes.add(recipe);
    }
    
    @Override
    public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        CategoryIdentifier<Display> categoryIdentifier = recipeCategoryUid.categoryId();
        Set<Object> recipes = hiddenRecipes.computeIfAbsent(categoryIdentifier, $ -> new HashSet<>());
        recipes.remove(recipe);
        if (recipes.isEmpty()) {
            hiddenRecipes.remove(categoryIdentifier);
        }
    }
    
    @Override
    public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
        this.hiddenCategories.add(recipeCategoryUid.categoryId());
    }
    
    @Override
    public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
        this.hiddenCategories.remove(recipeCategoryUid.categoryId());
    }
    
    @Override
    public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        JEIRecipeRegistration.addRecipes0(Collections.singletonList(recipe), recipeCategoryUid);
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
