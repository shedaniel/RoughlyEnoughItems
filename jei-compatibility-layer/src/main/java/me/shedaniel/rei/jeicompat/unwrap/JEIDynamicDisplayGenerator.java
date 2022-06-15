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

package me.shedaniel.rei.jeicompat.unwrap;

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.wrap.JEIFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIDynamicDisplayGenerator implements DynamicDisplayGenerator<Display> {
    private final IRecipeManagerPlugin plugin;
    
    public JEIDynamicDisplayGenerator(IRecipeManagerPlugin plugin) {
        this.plugin = plugin;
    }
    
    private Optional<List<Display>> getDisplays(EntryStack<?> entry, RecipeIngredientRole role) {
        JEIFocus<?> focus = new JEIFocus<>(role, entry.typedJeiValue());
        List<RecipeType<?>> categoryIds = plugin.getRecipeTypes(focus);
        if (categoryIds.isEmpty()) {
            return Optional.empty();
        }
        List<Display> displays = null;
        for (RecipeType<?> categoryId : categoryIds) {
            IRecipeCategory<Object> category = (IRecipeCategory<Object>) (CategoryRegistry.getInstance().get(categoryId.categoryId()).getCategory().wrapCategory());
            List<Object> recipes = plugin.getRecipes(category, focus);
            if (recipes != null && !recipes.isEmpty()) {
                if (displays == null) displays = CollectionUtils.flatMap(recipes, JEIPluginDetector::createDisplayFrom);
                else displays.addAll(CollectionUtils.flatMap(recipes, JEIPluginDetector::createDisplayFrom));
            }
            recipes = plugin.getRecipes(category);
            if (recipes != null && !recipes.isEmpty()) {
                if (displays == null) displays = new ArrayList<>(CollectionUtils.flatMap(recipes, JEIPluginDetector::createDisplayFrom));
                else displays.addAll(CollectionUtils.flatMap(recipes, JEIPluginDetector::createDisplayFrom));
            }
        }
        if (displays == null) {
            return Optional.empty();
        }
        return Optional.of(CollectionUtils.filterToList(displays, Objects::nonNull));
    }
    
    @Override
    public Optional<List<Display>> getRecipeFor(EntryStack<?> entry) {
        return getDisplays(entry, RecipeIngredientRole.OUTPUT);
    }
    
    @Override
    public Optional<List<Display>> getUsageFor(EntryStack<?> entry) {
        return getDisplays(entry, RecipeIngredientRole.INPUT);
    }
}
