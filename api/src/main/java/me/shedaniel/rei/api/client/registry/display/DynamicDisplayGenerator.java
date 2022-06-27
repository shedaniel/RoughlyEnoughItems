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

package me.shedaniel.rei.api.client.registry.display;

import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Optional;

/**
 * Generator of dynamic displays at runtime.
 * Invoked per display view search, please keep this performant.
 *
 * @param <T> the type of displays to generate
 * @see DisplayRegistry#registerDisplayGenerator(CategoryIdentifier, DynamicDisplayGenerator)
 * @see DisplayRegistry#registerGlobalDisplayGenerator(DynamicDisplayGenerator)
 */
@Environment(EnvType.CLIENT)
public interface DynamicDisplayGenerator<T extends Display> {
    /**
     * Returns the list of displays generated for querying the recipes of the given stack.
     * <p>
     * Displays generated should have the given stack as an output, but that
     * is not required.
     *
     * @param entry the recipes of the stack to query for
     * @return the list of displays generated
     * @see ViewSearchBuilder#addRecipesFor(EntryStack)
     */
    default Optional<List<T>> getRecipeFor(EntryStack<?> entry) {
        return Optional.empty();
    }
    
    /**
     * Returns the list of displays generated for querying the usages of the given stack.
     * <p>
     * Displays generated should have the given stack as an input, but that
     * is not required.
     *
     * @param entry the usages of the stack to query for
     * @return the list of displays generated
     * @see ViewSearchBuilder#addUsagesFor(EntryStack)
     */
    default Optional<List<T>> getUsageFor(EntryStack<?> entry) {
        return Optional.empty();
    }
    
    /**
     * Returns the list of displays generated for a given view search.
     *
     * @param builder the builder of the view search
     * @return the list of displays generated
     */
    default Optional<List<T>> generate(ViewSearchBuilder builder) {
        return Optional.empty();
    }
}
