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

package me.shedaniel.rei.impl.client.registry.display;

import dev.architectury.event.EventResult;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.common.registry.RecipeManagerContextImpl;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class DisplayRegistryImpl extends RecipeManagerContextImpl<REIClientPlugin> implements DisplayRegistry {
    private final Map<CategoryIdentifier<?>, List<Display>> displays = new ConcurrentHashMap<>();
    private final Map<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> displayGenerators = new ConcurrentHashMap<>();
    private final List<DynamicDisplayGenerator<?>> globalDisplayGenerators = new ArrayList<>();
    private final List<DisplayVisibilityPredicate> visibilityPredicates = new ArrayList<>();
    private final List<DisplayFiller<?>> fillers = new ArrayList<>();
    private final MutableInt displayCount = new MutableInt(0);
    
    public DisplayRegistryImpl() {
        super(RecipeManagerContextImpl.supplier());
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerDisplays(this);
    }
    
    @Override
    public int displaySize() {
        return displayCount.getValue();
    }
    
    @Override
    public void add(Display display) {
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
    public Map<CategoryIdentifier<?>, List<Display>> getAll() {
        return Collections.unmodifiableMap(displays);
    }
    
    @Override
    public <A extends Display> void registerGlobalDisplayGenerator(DynamicDisplayGenerator<A> generator) {
        globalDisplayGenerators.add(generator);
    }
    
    @Override
    public <A extends Display> void registerDisplayGenerator(CategoryIdentifier<A> categoryId, DynamicDisplayGenerator<A> generator) {
        displayGenerators.computeIfAbsent(categoryId, location -> new ArrayList<>())
                .add(generator);
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> getCategoryDisplayGenerators() {
        return Collections.unmodifiableMap(displayGenerators);
    }
    
    @Override
    public List<DynamicDisplayGenerator<?>> getGlobalDisplayGenerators() {
        return Collections.unmodifiableList(globalDisplayGenerators);
    }
    
    @Override
    public void registerVisibilityPredicate(DisplayVisibilityPredicate predicate) {
        visibilityPredicates.add(predicate);
        visibilityPredicates.sort(Comparator.reverseOrder());
    }
    
    @Override
    public boolean isDisplayVisible(Display display) {
        DisplayCategory<Display> category = (DisplayCategory<Display>) CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory();
        for (DisplayVisibilityPredicate predicate : visibilityPredicates) {
            try {
                EventResult result = predicate.handleDisplay(category, display);
                if (result.interruptsFurtherEvaluation()) {
                    return result.isEmpty() || result.isTrue();
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
    public <T, D extends Display> void registerFiller(Class<T> typeClass, Predicate<? extends T> predicate, Function<T, D> filler) {
        registerFiller(o -> typeClass.isInstance(o) && ((Predicate<T>) predicate).test((T) o), o -> filler.apply((T) o));
    }
    
    @Override
    public <D extends Display> void registerFiller(Predicate<?> predicate, Function<?, D> filler) {
        fillers.add(new DisplayFiller<>((Predicate<Object>) predicate, (Function<Object, D>) filler));
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
                Collection<Display> displays = tryFillDisplay(recipe);
                for (Display display : displays) {
                    registerDisplay(0, display);
                }
            }
        }
    }
    
    @Override
    public <T> Collection<Display> tryFillDisplay(T value) {
        if (value instanceof Display) return Collections.singleton((Display) value);
        List<Display> displays = null;
        for (DisplayFiller<?> filler : fillers) {
            Display display = tryFillDisplayGenerics(filler, value);
            if (display != null) {
                if (displays == null) displays = Collections.singletonList(display);
                else {
                    if (!(displays instanceof ArrayList)) displays = new ArrayList<>(displays);
                    displays.add(display);
                }
            }
        }
        if (displays != null) {
            return displays;
        }
        return Collections.emptyList();
    }
    
    private <D extends Display> D tryFillDisplayGenerics(DisplayFiller<D> filler, Object value) {
        try {
            if (filler.predicate.test(value)) {
                return filler.mappingFunction.apply(value);
            }
        } catch (Throwable e) {
            RoughlyEnoughItemsCore.LOGGER.error("Failed to fill displays!", e);
        }
        
        return null;
    }
    
    private static record DisplayFiller<D extends Display>(
            Predicate<Object> predicate,
            
            Function<Object, D> mappingFunction
    ) {}
}
