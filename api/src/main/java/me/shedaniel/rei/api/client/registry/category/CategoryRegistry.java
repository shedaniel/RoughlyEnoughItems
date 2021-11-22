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

package me.shedaniel.rei.api.client.registry.category;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.category.visibility.CategoryVisibilityPredicate;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Registry for registering new categories for displays.
 * Relies on {@link CategoryIdentifier}, and is reset per plugin reload.
 *
 * <p>Plugins may also determine the visibility of the categories dynamically via
 * {@link CategoryVisibilityPredicate}, these predicates are preferred comparing to
 * removing the categories from the registry.
 *
 * @see CategoryVisibilityPredicate
 * @see REIClientPlugin#registerCategories(CategoryRegistry)
 */
@Environment(EnvType.CLIENT)
public interface CategoryRegistry extends Reloadable<REIClientPlugin>, Iterable<CategoryRegistry.CategoryConfiguration<?>> {
    /**
     * @return the instance of {@link CategoryRegistry}
     */
    static CategoryRegistry getInstance() {
        return PluginManager.getClientInstance().get(CategoryRegistry.class);
    }
    
    default Stream<CategoryRegistry.CategoryConfiguration<?>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    
    /**
     * Registers a category.
     *
     * @param category the category to register
     */
    default <T extends Display> void add(DisplayCategory<T> category) {
        add(category, config -> {});
    }
    
    /**
     * Registers a category.
     *
     * @param category     the category to register
     * @param configurator the consumer for configuring the attributes of the category
     */
    <T extends Display> void add(DisplayCategory<T> category, Consumer<CategoryConfiguration<T>> configurator);
    
    /**
     * Registers the categories supplied.
     *
     * @param categories the categories to register
     */
    default <T extends Display> void add(Iterable<DisplayCategory<? extends T>> categories) {
        for (DisplayCategory<?> category : categories) {
            add(category);
        }
    }
    
    /**
     * Registers the categories supplied.
     *
     * @param categories the categories to register
     */
    default <T extends Display> void add(DisplayCategory<? extends T>... categories) {
        for (DisplayCategory<?> category : categories) {
            add(category);
        }
    }
    
    <T extends Display> CategoryConfiguration<T> get(CategoryIdentifier<T> category);
    
    <T extends Display> Optional<CategoryConfiguration<T>> tryGet(CategoryIdentifier<T> category);
    
    <T extends Display> void configure(CategoryIdentifier<T> category, Consumer<CategoryConfiguration<T>> action);
    
    int size();
    
    /**
     * Registers a category visibility predicate
     *
     * @param predicate the predicate to be registered
     */
    void registerVisibilityPredicate(CategoryVisibilityPredicate predicate);
    
    /**
     * Tests the category against all visibility predicates to determine whether it is visible
     *
     * @param category the category to test against
     * @return whether the category is visible
     */
    boolean isCategoryVisible(DisplayCategory<?> category);
    
    /**
     * Tests the category against all visibility predicates to determine whether it is invisible
     *
     * @param category the category to test against
     * @return whether the category is invisible
     */
    default boolean isCategoryInvisible(DisplayCategory<?> category) {
        return !isCategoryVisible(category);
    }
    
    /**
     * Returns an unmodifiable list of visibility predicates.
     *
     * @return an unmodifiable list of visibility predicates.
     */
    List<CategoryVisibilityPredicate> getVisibilityPredicates();
    
    default <D extends Display> void addWorkstations(CategoryIdentifier<D> category, EntryIngredient... stations) {
        configure(category, config -> config.addWorkstations(stations));
    }
    
    default <D extends Display> void addWorkstations(CategoryIdentifier<D> category, EntryStack<?>... stations) {
        configure(category, config -> config.addWorkstations(stations));
    }
    
    default <D extends Display> void removePlusButton(CategoryIdentifier<D> category) {
        configure(category, CategoryConfiguration::removePlusButton);
    }
    
    default <D extends Display> void setPlusButtonArea(CategoryIdentifier<D> category, ButtonArea area) {
        configure(category, config -> config.setPlusButtonArea(area));
    }
    
    interface CategoryConfiguration<T extends Display> extends Identifiable {
        /**
         * Registers the working stations of a category
         *
         * @param stations the working stations
         */
        void addWorkstations(EntryIngredient... stations);
        
        /**
         * Registers the working stations of a category
         *
         * @param stations the working stations
         */
        default void addWorkstations(EntryStack<?>... stations) {
            addWorkstations(CollectionUtils.map(stations, EntryIngredient::of).toArray(new EntryIngredient[0]));
        }
        
        /**
         * Removes the plus button.
         */
        default void removePlusButton() {
            setPlusButtonArea(bounds -> null);
        }
        
        /**
         * Sets the plus button area
         *
         * @param area the button area
         */
        void setPlusButtonArea(ButtonArea area);
        
        /**
         * Returns the optional plus button area
         *
         * @return the optional plus button area
         */
        Optional<ButtonArea> getPlusButtonArea();
        
        List<EntryIngredient> getWorkstations();
        
        /**
         * @return the underlying category
         */
        DisplayCategory<T> getCategory();
        
        /**
         * Gets the display display category identifier
         *
         * @return the identifier of the category
         */
        CategoryIdentifier<?> getCategoryIdentifier();
        
        @ApiStatus.Experimental
        void registerExtension(CategoryExtensionProvider<T> provider);
        
        @ApiStatus.Experimental
        DisplayCategoryView<T> getView(T display);
        
        @Override
        default ResourceLocation getIdentifier() {
            return getCategoryIdentifier().getIdentifier();
        }
    }
}
