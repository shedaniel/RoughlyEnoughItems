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

package me.shedaniel.rei.jeicompat.unwrap;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.LiveDisplayGenerator;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.wrap.JEIFocus;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.unwrap;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.unwrapCategory;

public class JEILiveDisplayGenerator implements LiveDisplayGenerator<Display> {
    private final IRecipeManagerPlugin plugin;
    
    public JEILiveDisplayGenerator(IRecipeManagerPlugin plugin) {
        this.plugin = plugin;
    }
    
    private Optional<List<Display>> getDisplays(EntryStack<?> entry, IFocus.Mode mode) {
        JEIFocus<?> focus = new JEIFocus<>(mode, unwrap(entry));
        List<ResourceLocation> categoryIds = plugin.getRecipeCategoryUids(focus);
        if (categoryIds.isEmpty()) {
            return Optional.empty();
        }
        List<Display> displays = null;
        for (ResourceLocation categoryId : categoryIds) {
            IRecipeCategory<Object> category = (IRecipeCategory<Object>) unwrapCategory(CategoryRegistry.getInstance().get(CategoryIdentifier.of(categoryId)).getCategory());
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
        return Optional.ofNullable(CollectionUtils.filterToList(displays, Objects::nonNull));
    }
    
    @Override
    public Optional<List<Display>> getRecipeFor(EntryStack<?> entry) {
        return getDisplays(entry, IFocus.Mode.OUTPUT);
    }
    
    @Override
    public Optional<List<Display>> getUsageFor(EntryStack<?> entry) {
        return getDisplays(entry, IFocus.Mode.INPUT);
    }
}
