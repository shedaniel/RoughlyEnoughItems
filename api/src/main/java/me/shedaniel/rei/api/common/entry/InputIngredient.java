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

package me.shedaniel.rei.api.common.entry;

import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * An input ingredient is a {@link EntryIngredient} and index pair.
 * <p>
 * The index is the index of the ingredient slot-wise, and not necessarily the index of the ingredient in the list of inputs.
 *
 * @param <T> the type of entry
 */
public interface InputIngredient<T> {
    /**
     * Creates an empty input ingredient at the given index.
     *
     * @param index the index
     * @param <T>   the type of entry
     * @return the empty input ingredient
     */
    static <T> InputIngredient<T> empty(int index) {
        return of(index, Collections.emptyList());
    }
    
    /**
     * Creates an input ingredient at the given index.
     *
     * @param index      the index
     * @param ingredient the ingredient
     * @param <T>        the type of entry
     * @return the input ingredient
     */
    static <T> InputIngredient<T> of(int index, List<T> ingredient) {
        return new InputIngredient<>() {
            @Override
            public List<T> get() {
                return ingredient;
            }
            
            @Override
            public int getIndex() {
                return index;
            }
        };
    }
    
    /**
     * Returns an input ingredient with only the stacks matching given entry type.
     *
     * @param ingredient the original ingredient
     * @param type       the entry type
     * @param <T>        the type of entry
     * @return the input ingredient
     */
    static <T> InputIngredient<T> withType(InputIngredient<EntryStack<?>> ingredient, EntryType<T> type) {
        return new InputIngredient<>() {
            @SuppressWarnings("RedundantTypeArguments")
            List<T> list = CollectionUtils.<EntryStack<?>, T>filterAndMap(ingredient.get(),
                    stack -> stack.getType() == type, EntryStack::castValue);
            
            @Override
            public List<T> get() {
                return list;
            }
            
            @Override
            public int getIndex() {
                return ingredient.getIndex();
            }
        };
    }
    
    /**
     * Returns the ingredient.
     *
     * @return the ingredient
     */
    List<T> get();
    
    /**
     * Returns the index.
     * <p>
     * This is the index of the ingredient slot-wise, and not necessarily the index of the ingredient in the list of inputs.
     *
     * @return the index
     */
    int getIndex();
}
