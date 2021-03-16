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

package me.shedaniel.rei.api.registry.display;

import me.shedaniel.rei.api.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.LiveDisplayGenerator;
import me.shedaniel.rei.api.plugins.PluginManager;
import me.shedaniel.rei.api.registry.RecipeManagerContext;
import me.shedaniel.rei.api.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public interface DisplayRegistry extends RecipeManagerContext, Reloadable {
    /**
     * @return the instance of {@link DisplayRegistry}
     */
    @NotNull
    static DisplayRegistry getInstance() {
        return PluginManager.getInstance().get(DisplayRegistry.class);
    }
    
    /**
     * Gets the total display count registered
     *
     * @return the recipe count
     */
    int getDisplayCount();
    
    /**
     * Registers a recipe display
     *
     * @param display the recipe display
     */
    void registerDisplay(Display display);
    
    /**
     * Returns an unmodifiable map of displays visible to the player
     *
     * @return an unmodifiable map of displays
     */
    Map<ResourceLocation, List<Display>> getAllDisplays();
    
    /**
     * Returns the list of displays visible to the player for a category
     *
     * @return the list of displays
     */
    default List<Display> getDisplays(ResourceLocation categoryId) {
        return getAllDisplays().getOrDefault(categoryId, Collections.emptyList());
    }
    
    /**
     * Registers a global category-less display generator
     *
     * @param generator the generator to register
     */
    <A extends Display> void registerGlobalDisplayGenerator(LiveDisplayGenerator<A> generator);
    
    /**
     * Registers a display generator
     *
     * @param categoryId the identifier of the category
     * @param generator  the generator to register
     */
    <A extends Display> void registerDisplayGenerator(ResourceLocation categoryId, LiveDisplayGenerator<A> generator);
    
    /**
     * Returns an unmodifiable map of display generators
     *
     * @return an unmodifiable map of display generators
     */
    Map<ResourceLocation, List<LiveDisplayGenerator<?>>> getCategoryDisplayGenerators();
    
    /**
     * Returns an unmodifiable list of category-less display generators
     *
     * @return an unmodifiable list of category-less display generators
     */
    List<LiveDisplayGenerator<?>> getGlobalDisplayGenerators();
    
    /**
     * Returns the list of display generators for a category
     *
     * @return the list of display generators
     */
    default List<LiveDisplayGenerator<?>> getCategoryDisplayGenerators(ResourceLocation categoryId) {
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
    
    default <T, D extends Display> void registerFiller(Class<T> typeClass, Function<T, D> mappingFunction) {
        registerFiller(typeClass, typeClass::isInstance, mappingFunction);
    }
    
    <T, D extends Display> void registerFiller(Class<T> typeClass, Predicate<? extends T> predicate, Function<T, D> mappingFunction);
    
    @Nullable <T> Display tryFillDisplay(T value);
}
