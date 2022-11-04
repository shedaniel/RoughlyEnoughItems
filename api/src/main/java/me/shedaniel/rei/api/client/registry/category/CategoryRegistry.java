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
    
    /**
     * Returns the category configuration for the given category identifier.
     *
     * @param category the identifier of the category
     * @param <T>      the type of the display
     * @return the category configuration
     * @throws NullPointerException if no {@link CategoryConfiguration} is found for the given identifier
     */
    <T extends Display> CategoryConfiguration<T> get(CategoryIdentifier<T> category);
    
    /**
     * Returns the category configuration for the given category identifier.
     *
     * @param category the identifier of the category
     * @param <T>      the type of the display
     * @return the category configuration
     */
    <T extends Display> Optional<CategoryConfiguration<T>> tryGet(CategoryIdentifier<T> category);
    
    /**
     * Configures the category configuration for the given category identifier.
     * <p>
     * This method is deferred to when the category is registered. This means that configuration can be
     * done anytime during the reload phase.
     *
     * @param category the identifier of the category
     * @param action   the action to perform
     * @param <T>      the type of the display
     */
    <T extends Display> void configure(CategoryIdentifier<T> category, Consumer<CategoryConfiguration<T>> action);
    
    /**
     * Returns the size of the category registry.
     *
     * @return the size of the category registry
     */
    int size();
    
    /**
     * Registers a category visibility predicate. This is used to determine if a category is visible or not.
     *
     * @param predicate the predicate to be registered
     * @see CategoryVisibilityPredicate
     */
    void registerVisibilityPredicate(CategoryVisibilityPredicate predicate);
    
    /**
     * Returns whether the category is visible against visibility predicates.
     *
     * @param category the category to test against
     * @return whether the category is visible
     */
    boolean isCategoryVisible(DisplayCategory<?> category);
    
    /**
     * Returns whether the category is invisible against visibility predicates.
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
    
    /**
     * Registers workstations for a category.
     * <p>
     * This method is deferred to when the category is registered. This means that registration can be
     * done anytime during the reload phase.
     *
     * @param category the category to register the workstation for
     * @param stations the workstations to register
     * @param <D>      the type of the display
     */
    default <D extends Display> void addWorkstations(CategoryIdentifier<D> category, EntryIngredient... stations) {
        configure(category, config -> config.addWorkstations(stations));
    }
    
    /**
     * Registers workstations for a category.
     * <p>
     * This method is deferred to when the category is registered. This means that registration can be
     * done anytime during the reload phase.
     *
     * @param category the category to register the workstation for
     * @param stations the workstations to register
     * @param <D>      the type of the display
     */
    default <D extends Display> void addWorkstations(CategoryIdentifier<D> category, EntryStack<?>... stations) {
        configure(category, config -> config.addWorkstations(stations));
    }
    
    /**
     * Removes the plus button from a category.
     *
     * @param category the category to remove the plus button from
     * @param <D>      the type of the display
     * @deprecated No longer supported, the plus button is not available for removal
     */
    @Deprecated(forRemoval = true)
    default <D extends Display> void removePlusButton(CategoryIdentifier<D> category) {
        configure(category, CategoryConfiguration::removePlusButton);
    }
    
    /**
     * Sets a plus button area provider for a category.
     * <p>
     * This method is deferred to when the category is registered. This means that registration can be
     * done anytime during the reload phase.
     *
     * @param category the category to set the plus button area provider for
     * @param area     the area provider
     * @param <D>      the type of the display
     */
    default <D extends Display> void setPlusButtonArea(CategoryIdentifier<D> category, ButtonArea area) {
        configure(category, config -> config.setPlusButtonArea(area));
    }
    
    interface CategoryConfiguration<T extends Display> extends Identifiable {
        /**
         * Registers workstations for a category.
         *
         * @param stations the workstations to register
         */
        void addWorkstations(EntryIngredient... stations);
        
        /**
         * Registers workstations for a category.
         *
         * @param stations the workstations to register
         */
        default void addWorkstations(EntryStack<?>... stations) {
            addWorkstations(CollectionUtils.map(stations, EntryIngredient::of).toArray(new EntryIngredient[0]));
        }
        
        /**
         * Removes the plus button from a category.
         *
         * @deprecated No longer supported, the plus button is not available for removal
         */
        @Deprecated(forRemoval = true)
        default void removePlusButton() {
            setPlusButtonArea(bounds -> null);
        }
        
        /**
         * Sets a plus button area provider for a category.
         *
         * @param area the area provider
         */
        void setPlusButtonArea(ButtonArea area);
        
        /**
         * Returns whether the category is available for quick crafting by default.
         *
         * @return whether the category is available for quick crafting by default
         */
        @ApiStatus.Experimental
        boolean isQuickCraftingEnabledByDefault();
        
        /**
         * Sets whether the category is available for quick crafting by default.
         *
         * @param enabled whether the category is available for quick crafting by default
         */
        @ApiStatus.Experimental
        void setQuickCraftingEnabledByDefault(boolean enabled);
        
        /**
         * Returns the optional plus button area provider
         *
         * @return the optional plus button area provider
         * @implNote This method no longer returns an empty optional
         */
        Optional<ButtonArea> getPlusButtonArea();
        
        /**
         * Returns the list of workstations for the category.
         *
         * @return the list of workstations for the category
         */
        List<EntryIngredient> getWorkstations();
        
        /**
         * Returns the underlying category.
         *
         * @return the underlying category
         */
        DisplayCategory<T> getCategory();
        
        /**
         * Returns the category identifier
         *
         * @return the identifier of the category
         */
        CategoryIdentifier<?> getCategoryIdentifier();
        
        /**
         * Returns an extension for the category.
         * <p>
         * An extension may be used to modify how the category set-ups widgets for displays.
         *
         * @param provider the provider to register
         * @see CategoryExtensionProvider
         */
        @ApiStatus.Experimental
        void registerExtension(CategoryExtensionProvider<T> provider);
        
        /**
         * Returns a category view for a display.
         *
         * @param display the display
         * @return the category view
         */
        @ApiStatus.Experimental
        DisplayCategoryView<T> getView(T display);
        
        /**
         * {@inheritDoc}
         */
        @Override
        default ResourceLocation getIdentifier() {
            return getCategoryIdentifier().getIdentifier();
        }
    }
}
