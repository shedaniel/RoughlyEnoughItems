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

package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.LiveDisplayGenerator;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.registry.display.DisplayRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class DisplayRegistryImpl extends RecipeManagerContextImpl implements DisplayRegistry {
    private final Map<ResourceLocation, List<Display>> displays = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, List<LiveDisplayGenerator<?>>> displayGenerators = new ConcurrentHashMap<>();
    private final List<DisplayVisibilityPredicate> visibilityPredicates = new ArrayList<>();
    private final List<RecipeFiller<?, ?, ?>> fillers = new ArrayList<>();
    private final MutableInt displayCount = new MutableInt(0);
    
    @Override
    public void acceptPlugin(REIPlugin plugin) {
        plugin.registerDisplays(this);
    }
    
    @Override
    public int getDisplayCount() {
        return displayCount.getValue();
    }
    
    @Override
    public void registerDisplay(Display display) {
        displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new ArrayList<>())
                .add(display);
        displayCount.increment();
    }
    
    public void registerDisplay(int index, Display display) {
        displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new ArrayList<>())
                .add(index, display);
        displayCount.increment();
    }
    
    @Override
    public Map<ResourceLocation, List<Display>> getAllDisplays() {
        return Collections.unmodifiableMap(displays);
    }
    
    @Override
    public <A extends Display> void registerDisplayGenerator(ResourceLocation categoryId, LiveDisplayGenerator<A> generator) {
        displayGenerators.computeIfAbsent(categoryId, location -> new ArrayList<>())
                .add(generator);
    }
    
    @Override
    public Map<ResourceLocation, List<LiveDisplayGenerator<?>>> getAllDisplayGenerators() {
        return Collections.unmodifiableMap(displayGenerators);
    }
    
    @Override
    public void registerVisibilityPredicate(DisplayVisibilityPredicate predicate) {
        visibilityPredicates.add(predicate);
        visibilityPredicates.sort(Comparator.reverseOrder());
    }
    
    @Override
    public boolean isDisplayVisible(Display display) {
        DisplayCategory<Display> category = CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory();
        for (DisplayVisibilityPredicate predicate : visibilityPredicates) {
            try {
                InteractionResult result = predicate.handleDisplay(category, display);
                if (result != InteractionResult.PASS) {
                    return result == InteractionResult.SUCCESS;
                }
            } catch (Throwable throwable) {
                RoughlyEnoughItemsCore.LOGGER.error("Failed to check if the recipe is visible!", throwable);
            }
        }
        
        return true;
    }
    
    @Override
    public List<DisplayVisibilityPredicate> getVisibilityPredicates() {
        return Collections.unmodifiableList(visibilityPredicates);
    }
    
    @Override
    public <A extends Container, T extends Recipe<A>, D extends Display> void registerRecipes(Predicate<? extends T> recipeFilter, Function<T, D> mappingFunction) {
        fillers.add(new RecipeFiller<>((Predicate<T>) recipeFilter, mappingFunction));
    }
    
    @Override
    public void startReload() {
        super.startReload();
        this.displays.clear();
        this.displayGenerators.clear();
        this.visibilityPredicates.clear();
        this.fillers.clear();
        this.displayCount.setValue(0);
    }
    
    @Override
    public void endReload() {
        if (!fillers.isEmpty()) {
            List<Recipe<?>> allSortedRecipes = getAllSortedRecipes();
            for (int i = allSortedRecipes.size() - 1; i >= 0; i--) {
                Recipe<?> recipe = allSortedRecipes.get(i);
                for (RecipeFiller<?, ?, ?> filler : fillers) {
                    try {
                        handleRecipe(filler, recipe);
                    } catch (Throwable e) {
                        RoughlyEnoughItemsCore.LOGGER.error("Failed to fill recipes!", e);
                    }
                }
            }
        }
        this.fillers.clear();
    }
    
    private <A extends Container, T extends Recipe<A>, D extends Display> void handleRecipe(RecipeFiller<A, T, D> filler, Recipe<?> recipe) {
        if (filler.recipeFilter.test((T) recipe)) {
            registerDisplay(0, filler.mappingFunction.apply((T) recipe));
        }
    }
    
    private static class RecipeFiller<A extends Container, T extends Recipe<A>, D extends Display> {
        private final Predicate<T> recipeFilter;
        
        private final Function<T, D> mappingFunction;
        
        public RecipeFiller(Predicate<T> recipeFilter, Function<T, D> mappingFunction) {
            this.recipeFilter = recipeFilter;
            this.mappingFunction = mappingFunction;
        }
        
    }
}
