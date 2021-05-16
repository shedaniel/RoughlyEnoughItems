/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.unwrap.JEIUnwrappedCategory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public enum JEIRecipeManager implements IRecipeManager {
    INSTANCE;
    
    Set<CategoryIdentifier<?>> hiddenCategories = new HashSet<>();
    Map<CategoryIdentifier<?>, Set<Object>> hiddenRecipes = new HashMap<>();
    public Predicate predicate = new Predicate();
    
    @Override
    public List<IRecipeCategory<?>> getRecipeCategories() {
        return CollectionUtils.map(CategoryRegistry.getInstance(), config -> new JEIUnwrappedCategory<>(config.getCategory()));
    }
    
    @Override
    public List<IRecipeCategory<?>> getRecipeCategories(List<ResourceLocation> recipeCategoryUids) {
        return CollectionUtils.map(recipeCategoryUids, this::getRecipeCategory);
    }
    
    @Override
    public @Nullable IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid) {
        try {
            return new JEIUnwrappedCategory<>(CategoryRegistry.getInstance().get(CategoryIdentifier.of(recipeCategoryUid)).getCategory());
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    @Override
    public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
        return new JEIFocus<>(mode, ingredient);
    }
    
    @Override
    public <V> List<IRecipeCategory<?>> getRecipeCategories(IFocus<V> focus) {
        throw TODO();
    }
    
    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        throw TODO();
    }
    
    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        return wrapRecipes(CategoryIdentifier.of(recipeCategory.getUid()));
    }
    
    @Override
    public List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
        List<Object> objects = new ArrayList<>();
        for (EntryIngredient stacks : CategoryRegistry.getInstance().get(CategoryIdentifier.of(recipeCategory.getUid())).getWorkstations()) {
            objects.addAll(CollectionUtils.map(stacks, JEIPluginDetector::unwrap));
        }
        return objects;
    }
    
    @Override
    public @Nullable <T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus) {
        throw TODO();
    }
    
    @Override
    public <T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        Set<Object> recipes = hiddenRecipes.computeIfAbsent(CategoryIdentifier.of(recipeCategoryUid), $ -> new HashSet<>());
        recipes.add(recipe);
    }
    
    @Override
    public <T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        CategoryIdentifier<Display> categoryIdentifier = CategoryIdentifier.of(recipeCategoryUid);
        Set<Object> recipes = hiddenRecipes.computeIfAbsent(categoryIdentifier, $ -> new HashSet<>());
        recipes.remove(recipe);
        if (recipes.isEmpty()) {
            hiddenRecipes.remove(categoryIdentifier);
        }
    }
    
    @Override
    public void hideRecipeCategory(ResourceLocation recipeCategoryUid) {
        this.hiddenCategories.add(CategoryIdentifier.of(recipeCategoryUid));
    }
    
    @Override
    public void unhideRecipeCategory(ResourceLocation recipeCategoryUid) {
        this.hiddenCategories.remove(CategoryIdentifier.of(recipeCategoryUid));
    }
    
    @Override
    public <T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid) {
        Collection<Display> display = createDisplayFrom(recipe);
        for (Display d : display) {
            if (Objects.equals(d.getCategoryIdentifier().getIdentifier(), recipeCategoryUid)) {
                DisplayRegistry.getInstance().add(d);
            }
        }
    }
    
    public class Predicate implements DisplayVisibilityPredicate {
        @Override
        public InteractionResult handleDisplay(DisplayCategory<?> category, Display display) {
            if (hiddenCategories.contains(category.getCategoryIdentifier())) {
                return InteractionResult.FAIL;
            }
            Set<Object> hidden = hiddenRecipes.get(category.getCategoryIdentifier());
            if (hidden != null && hidden.contains(wrapRecipe(category, display))) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        }
    }
}
