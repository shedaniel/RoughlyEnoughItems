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

package me.shedaniel.rei.impl.client.registry.category;

import com.google.common.base.MoreObjects;
import dev.architectury.event.EventResult;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.common.InternalLogger;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class CategoryRegistryImpl implements CategoryRegistry {
    private final Map<CategoryIdentifier<?>, Configuration<?>> categories = new LinkedHashMap<>();
    private final Map<CategoryIdentifier<?>, List<Consumer<CategoryConfiguration<?>>>> listeners = new HashMap<>();
    private final List<CategoryVisibilityPredicate> visibilityPredicates = new ArrayList<>();
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerCategories(this);
    }
    
    @Override
    public void startReload() {
        this.categories.clear();
        this.listeners.clear();
        this.visibilityPredicates.clear();
    }
    
    @Override
    public void endReload() {
        InternalLogger.getInstance().debug("Registered %d categories: %s", this.categories.size(),
                this.categories.values().stream().map(configuration -> configuration.getCategory().getTitle().getString())
                        .collect(Collectors.joining(", ")));
    }
    
    @Override
    public <T extends Display> void add(DisplayCategory<T> category, Consumer<CategoryConfiguration<T>> configurator) {
        Configuration<T> configuration = new Configuration<>(category);
        this.categories.put(category.getCategoryIdentifier(), configuration);
        configurator.accept(configuration);
        
        List<Consumer<CategoryConfiguration<?>>> listeners = this.listeners.get(category.getCategoryIdentifier());
        if (listeners != null) {
            this.listeners.remove(category.getCategoryIdentifier());
            for (Consumer<CategoryConfiguration<?>> listener : listeners) {
                listener.accept(configuration);
            }
        }
        
        InternalLogger.getInstance().debug("Added category [%s]: %s", category.getCategoryIdentifier(), category.getTitle().getString());
    }
    
    @Override
    public <T extends Display> CategoryConfiguration<T> get(CategoryIdentifier<T> category) {
        return (CategoryConfiguration<T>) Objects.requireNonNull(this.categories.get(category), category.toString());
    }
    
    @Override
    public <T extends Display> Optional<CategoryConfiguration<T>> tryGet(CategoryIdentifier<T> category) {
        return Optional.ofNullable((CategoryConfiguration<T>) this.categories.get(category));
    }
    
    @Override
    public <T extends Display> void configure(CategoryIdentifier<T> category, Consumer<CategoryConfiguration<T>> action) {
        if (this.categories.containsKey(category)) {
            action.accept(get(category));
        } else {
            //noinspection rawtypes
            listeners.computeIfAbsent(category, location -> new ArrayList<>()).add((Consumer) action);
        }
    }
    
    @Override
    public Iterator<CategoryConfiguration<?>> iterator() {
        return (Iterator) categories.values().iterator();
    }
    
    @Override
    public int size() {
        return categories.size();
    }
    
    @Override
    public void registerVisibilityPredicate(CategoryVisibilityPredicate predicate) {
        visibilityPredicates.add(predicate);
        visibilityPredicates.sort(Comparator.reverseOrder());
        InternalLogger.getInstance().debug("Added category visibility predicate: %s [%.2f priority]", predicate, predicate.getPriority());
    }
    
    @Override
    public boolean isCategoryVisible(DisplayCategory<?> category) {
        for (CategoryVisibilityPredicate predicate : visibilityPredicates) {
            try {
                EventResult result = predicate.handleCategory(category);
                if (result.interruptsFurtherEvaluation()) {
                    return result.isEmpty() || result.isTrue();
                }
            } catch (Throwable throwable) {
                InternalLogger.getInstance().error("Failed to check if the category is visible!", throwable);
            }
        }
        
        return true;
    }
    
    @Override
    public List<CategoryVisibilityPredicate> getVisibilityPredicates() {
        return Collections.unmodifiableList(visibilityPredicates);
    }
    
    private static class Configuration<T extends Display> implements CategoryConfiguration<T> {
        private final DisplayCategory<T> category;
        private final List<EntryIngredient> workstations = Collections.synchronizedList(new ArrayList<>());
        private final List<CategoryExtensionProvider<T>> extensionProviders = Collections.synchronizedList(new ArrayList<>());
        private boolean quickCraftingEnabledByDefault = true;
        
        private ButtonArea plusButtonArea = ButtonArea.defaultArea();
        
        public Configuration(DisplayCategory<T> category) {
            this.category = category;
        }
        
        @Override
        public void addWorkstations(EntryIngredient... stations) {
            this.workstations.addAll(Arrays.asList(stations));
        }
        
        @Override
        public void setPlusButtonArea(ButtonArea area) {
            this.plusButtonArea = MoreObjects.firstNonNull(area, ButtonArea.defaultArea());
        }
        
        @Override
        public boolean isQuickCraftingEnabledByDefault() {
            return quickCraftingEnabledByDefault;
        }
        
        @Override
        public void setQuickCraftingEnabledByDefault(boolean enabled) {
            this.quickCraftingEnabledByDefault = enabled;
        }
        
        @Override
        public Optional<ButtonArea> getPlusButtonArea() {
            return Optional.of(bounds -> Objects.requireNonNullElseGet(this.plusButtonArea.get(bounds), () -> ButtonArea.defaultArea().get(bounds)));
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
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return this.category.getCategoryIdentifier();
        }
        
        @Override
        public void registerExtension(CategoryExtensionProvider<T> provider) {
            this.extensionProviders.add(provider);
        }
        
        @Override
        public DisplayCategoryView<T> getView(T display) {
            DisplayCategory<T> category = getCategory();
            DisplayCategoryView<T> view = getCategory();
            for (CategoryExtensionProvider<T> provider : this.extensionProviders) {
                view = Objects.requireNonNull(provider.provide(display, category, view));
            }
            return view;
        }
    }
}
