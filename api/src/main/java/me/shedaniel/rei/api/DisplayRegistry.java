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

package me.shedaniel.rei.api;

import me.shedaniel.rei.api.registry.RecipeManagerContext;
import me.shedaniel.rei.api.registry.Reloadable;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

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
        return Internals.getDisplayRegistry();
    }
    
    /**
     * Gets the total display count registered
     *
     * @return the recipe count
     */
    int getDisplayCount();
    
    /**
     * Registers a recipe display.
     *
     * @param display the recipe display
     */
    void registerDisplay(Display display);
    
    /**
     * Gets the map of all recipes visible to the player
     *
     * @return the map of recipes
     */
    Map<ResourceLocation, List<Display>> getAllRecipes();
    
    /**
     * Registers a live display generator.
     *
     * @param categoryId the identifier of the category
     * @param generator  the generator to register
     */
    void registerLiveDisplayGenerator(ResourceLocation categoryId, LiveDisplayGenerator<?> generator);
    
    /**
     * Registers a display visibility handler.
     *
     * @param visibilityHandler the handler to be registered
     */
    void registerRecipeVisibilityHandler(DisplayVisibilityHandler visibilityHandler);
    
    default <T extends Recipe<?>> void registerRecipes(ResourceLocation category, Class<T> recipeClass, Function<T, Display> mappingFunction) {
        registerRecipes(category, recipe -> recipeClass.isAssignableFrom(recipe.getClass()), mappingFunction);
    }
    
    <T extends Recipe<?>> void registerRecipes(ResourceLocation category, Predicate<? extends T> recipeFilter, Function<T, Display> mappingFunction);
}
