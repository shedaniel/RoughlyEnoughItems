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

import me.shedaniel.rei.api.ButtonAreaSupplier;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.Reloadable;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CategoryRegistryImpl implements CategoryRegistry, Reloadable {
    private final Map<ResourceLocation, Configuration<?>> categories = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, List<Consumer<CategoryConfiguration<?>>>> listeners = new ConcurrentHashMap<>();
    
    @Override
    public void acceptPlugin(REIPlugin plugin) {
        plugin.registerCategories(this);
    }
    
    @Override
    public void startReload() {
        this.categories.clear();
    }
    
    @Override
    public <T extends Display> void register(DisplayCategory<T> category, Consumer<CategoryConfiguration<T>> configurator) {
        Configuration<T> configuration = new Configuration<>(category);
        this.categories.put(category.getIdentifier(), configuration);
        configurator.accept(configuration);
        
        List<Consumer<CategoryConfiguration<?>>> listeners = this.listeners.get(category.getIdentifier());
        if (listeners != null) {
            this.listeners.remove(category.getIdentifier());
            for (Consumer<CategoryConfiguration<?>> listener : listeners) {
                listener.accept(configuration);
            }
        }
    }
    
    @Override
    public <T extends Display> CategoryConfiguration<T> get(ResourceLocation category) {
        return (CategoryConfiguration<T>) this.categories.get(category);
    }
    
    @Override
    public <T extends Display> void configure(ResourceLocation category, Consumer<CategoryConfiguration<T>> action) {
        if (this.categories.containsKey(category)) {
            action.accept(get(category));
        } else {
            //noinspection rawtypes
            listeners.computeIfAbsent(category, location -> new ArrayList<>()).add((Consumer) action);
        }
    }
    
    @NotNull
    @Override
    public Iterator<CategoryConfiguration<?>> iterator() {
        return (Iterator) categories.values().iterator();
    }
    
    @Override
    public int size() {
        return categories.size();
    }
    private static class Configuration<T extends Display> implements CategoryConfiguration<T> {
        private final DisplayCategory<T> category;
        private final List<EntryIngredient> workstations = Collections.synchronizedList(new ArrayList<>());
    
        private Optional<ButtonAreaSupplier> plusButtonArea = Optional.of(ButtonAreaSupplier.defaultArea());
    
        public Configuration(DisplayCategory<T> category) {
            this.category = category;
        }
    
        @Override
        public void addWorkstations(EntryIngredient... stations) {
            this.workstations.addAll(Arrays.asList(stations));
        }
    
        @Override
        public void setPlusButtonArea(ButtonAreaSupplier supplier) {
            this.plusButtonArea = Optional.ofNullable(supplier);
        }
    
        @Override
        public Optional<ButtonAreaSupplier> getPlusButtonArea() {
            return plusButtonArea;
        }
    
        @Override
        public List<EntryIngredient> getWorkstations() {
            return Collections.unmodifiableList(this.workstations);
        }
    
        @Override
        public DisplayCategory<T> getCategory() {
            return this.category;
        }
        @Override
        public ResourceLocation getIdentifier() {
            return this.category.getIdentifier();
        }
    
    }
}
