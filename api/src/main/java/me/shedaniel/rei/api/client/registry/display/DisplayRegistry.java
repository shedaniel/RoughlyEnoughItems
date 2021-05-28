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

package me.shedaniel.rei.api.client.registry.display;

import com.google.common.base.Predicates;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Registry for registering displays for categories, this is called right after
 * {@link me.shedaniel.rei.api.client.registry.category.CategoryRegistry}.
 *
 * <p>Each display should have a category associated with it that's registered,
 * For any dynamic displays, you may want to look at {@link DynamicDisplayGenerator}.
 *
 * <p>Plugins may also determine the visibility of the displays dynamically via
 * {@link DisplayVisibilityPredicate}, these predicates are preferred comparing to
 * removing the displays from the registry.
 *
 * <p>Displays filler may be used for automatically registering displays from {@link Recipe},
 * these are filled after client recipe manager sync, and are invoked with one cycle.
 * Additionally, display filters allow other mods to easily register additional displays
 * for your mod.
 *
 * @see Display
 * @see DynamicDisplayGenerator
 * @see DisplayVisibilityPredicate
 * @see REIClientPlugin#registerDisplays(DisplayRegistry)
 */
@Environment(EnvType.CLIENT)
public interface DisplayRegistry extends RecipeManagerContext<REIClientPlugin> {
    /**
     * @return the instance of {@link DisplayRegistry}
     */
    
    static DisplayRegistry getInstance() {
        return PluginManager.getClientInstance().get(DisplayRegistry.class);
    }
    
    /**
     * Gets the total display count registered
     *
     * @return the recipe count
     */
    int displaySize();
    
    /**
     * Registers a recipe display
     *
     * @param display the recipe display
     */
    void add(Display display);
    
    /**
     * Returns an unmodifiable map of displays visible to the player
     *
     * @return an unmodifiable map of displays
     */
    Map<CategoryIdentifier<?>, List<Display>> getAll();
    
    /**
     * Returns the list of displays visible to the player for a category
     *
     * @return the list of displays
     */
    default <D extends Display> List<D> get(CategoryIdentifier<D> categoryId) {
        return (List<D>) getAll().getOrDefault(categoryId, Collections.emptyList());
    }
    
    /**
     * Registers a global category-less display generator
     *
     * @param generator the generator to register
     */
    <A extends Display> void registerGlobalDisplayGenerator(DynamicDisplayGenerator<A> generator);
    
    /**
     * Registers a display generator
     *
     * @param categoryId the identifier of the category
     * @param generator  the generator to register
     */
    <A extends Display> void registerDisplayGenerator(CategoryIdentifier<A> categoryId, DynamicDisplayGenerator<A> generator);
    
    /**
     * Returns an unmodifiable map of display generators
     *
     * @return an unmodifiable map of display generators
     */
    Map<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> getCategoryDisplayGenerators();
    
    /**
     * Returns an unmodifiable list of category-less display generators
     *
     * @return an unmodifiable list of category-less display generators
     */
    List<DynamicDisplayGenerator<?>> getGlobalDisplayGenerators();
    
    /**
     * Returns the list of display generators for a category
     *
     * @return the list of display generators
     */
    default <D extends Display> List<DynamicDisplayGenerator<?>> getCategoryDisplayGenerators(CategoryIdentifier<D> categoryId) {
        return getCategoryDisplayGenerators().getOrDefault(categoryId, Collections.emptyList());
    }
    
    /**
     * Registers a display visibility predicate
     *
     * @param predicate the predicate to be registered
     */
    void registerVisibilityPredicate(DisplayVisibilityPredicate predicate);
    
    /**
     * Tests the display against all visibility predicates to determine whether it is visible
     *
     * @param display the display to test against
     * @return whether the display is visible
     */
    boolean isDisplayVisible(Display display);
    
    /**
     * Tests the display against all visibility predicates to determine whether it is invisible
     *
     * @param display the display to test against
     * @return whether the display is invisible
     */
    default boolean isDisplayInvisible(Display display) {
        return !isDisplayVisible(display);
    }
    
    /**
     * Returns an unmodifiable list of visibility predicates.
     *
     * @return an unmodifiable list of visibility predicates.
     */
    List<DisplayVisibilityPredicate> getVisibilityPredicates();
    
    /**
     * Registers a display filler, to be filled during {@link #tryFillDisplay(Object)}.
     * <p>
     * Vanilla {@link Recipe} are by default filled, display filters
     * can be used to automatically generate displaies for vanilla {@link Recipe}.
     *
     * @param typeClass the type of {@code T}
     * @param filler    the filler, taking a {@code T} and returning a {@code D}
     * @param <T>       the type of object
     * @param <D>       the type of display
     */
    default <T, D extends Display> void registerFiller(Class<T> typeClass, Function<T, D> filler) {
        registerFiller(typeClass, Predicates.alwaysTrue(), filler);
    }
    
    /**
     * Registers a display filler, to be filled during {@link #tryFillDisplay(Object)}.
     * <p>
     * Vanilla {@link Recipe} are by default filled, display filters
     * can be used to automatically generate displaies for vanilla {@link Recipe}.
     *
     * @param typeClass the type of {@code T}
     * @param predicate the predicate of {@code T}
     * @param filler    the filler, taking a {@code T} and returning a {@code D}
     * @param <T>       the type of object
     * @param <D>       the type of display
     */
    <T, D extends Display> void registerFiller(Class<T> typeClass, Predicate<? extends T> predicate, Function<T, D> filler);
    
    /**
     * Tries to fill displays from {@code T}.
     *
     * @param value the object
     * @param <T>   the type of object
     * @return the collection of displays
     */
    <T> Collection<Display> tryFillDisplay(T value);
}
