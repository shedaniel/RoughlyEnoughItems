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

import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Optional;

/**
 * Interface for generating dynamic displays at runtime.
 * Invoked per display view search, so please keep this performant.
 *
 * @param <T> the type of displays to generate
 * @see DisplayRegistry#registerDisplayGenerator(CategoryIdentifier, DynamicDisplayGenerator)
 * @see DisplayRegistry#registerGlobalDisplayGenerator(DynamicDisplayGenerator)
 */
@Environment(EnvType.CLIENT)
public interface DynamicDisplayGenerator<T extends Display> {
    default Optional<List<T>> getRecipeFor(EntryStack<?> entry) {
        return Optional.empty();
    }
    
    default Optional<List<T>> getUsageFor(EntryStack<?> entry) {
        return Optional.empty();
    }
    
    default Optional<List<T>> generate(ViewSearchBuilder builder) {
        return Optional.empty();
    }
}
