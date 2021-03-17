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

import me.shedaniel.rei.api.ingredient.EntryIngredient;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public interface Display {
    /**
     * @return a list of inputs
     */
    List<EntryIngredient> getInputEntries();
    
    /**
     * @return a list of outputs
     */
    List<EntryIngredient> getResultingEntries();
    
    /**
     * Gets the required items used in craftable filters
     *
     * @return the list of required items
     */
    default List<EntryIngredient> getRequiredEntries() {
        return getInputEntries();
    }
    
    /**
     * Gets the display display category identifier
     *
     * @return the identifier of the category
     */
    ResourceLocation getCategoryIdentifier();
    
    /**
     * Returns the display location from data packs.
     *
     * @return the display location
     */
    default Optional<ResourceLocation> getDisplayLocation() {
        return Optional.empty();
    }
}
