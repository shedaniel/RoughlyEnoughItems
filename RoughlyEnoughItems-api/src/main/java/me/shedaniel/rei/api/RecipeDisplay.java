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

import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface RecipeDisplay {
    
    /**
     * @return a list of inputs
     */
    @NotNull
    List<List<EntryStack>> getInputEntries();
    
    /**
     * @return a list of outputs
     * @deprecated Use {@link RecipeDisplay#getResultingEntries()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.17")
    @NotNull
    default List<EntryStack> getOutputEntries() {
        return Collections.emptyList();
    }
    
    /**
     * @return a list of outputs
     */
    @NotNull
    default List<List<EntryStack>> getResultingEntries() {
        return CollectionUtils.map(getOutputEntries(), Collections::singletonList);
    }
    
    /**
     * Gets the required items used in craftable filters
     *
     * @return the list of required items
     */
    @NotNull
    default List<List<EntryStack>> getRequiredEntries() {
        return Collections.emptyList();
    }
    
    /**
     * Gets the recipe display category identifier
     *
     * @return the identifier of the category
     */
    @NotNull
    ResourceLocation getRecipeCategory();
    
    /**
     * Gets the recipe location from datapack.
     *
     * @return the recipe location
     */
    @NotNull
    default Optional<ResourceLocation> getRecipeLocation() {
        return Optional.empty();
    }
    
}
