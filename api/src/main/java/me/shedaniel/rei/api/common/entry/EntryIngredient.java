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

package me.shedaniel.rei.api.common.entry;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;

/**
 * An immutable representation of a list of {@link EntryStack}.
 */
public interface EntryIngredient extends List<EntryStack<?>> {
    static EntryIngredient empty() {
        return Internals.getEntryIngredientProvider().empty();
    }
    
    static <T> EntryIngredient of(EntryStack<T> stack) {
        return Internals.getEntryIngredientProvider().of(stack);
    }
    
    @SafeVarargs
    static <T> EntryIngredient of(EntryStack<T>... stacks) {
        return Internals.getEntryIngredientProvider().of(stacks);
    }
    
    @SuppressWarnings({"RedundantCast", "rawtypes"})
    static <T> EntryIngredient of(Iterable<? extends EntryStack<? extends T>> stacks) {
        return Internals.getEntryIngredientProvider().of((Iterable<EntryStack<?>>) (Iterable) stacks);
    }
    
    static EntryIngredient from(ListTag tag) {
        if (tag.isEmpty()) return empty();
        EntryStack<?>[] stacks = new EntryStack[tag.size()];
        for (int i = 0; i < tag.size(); i++) {
            stacks[i] = EntryStack.read((CompoundTag) tag.get(i));
        }
        return Internals.getEntryIngredientProvider().of(stacks);
    }
    
    ListTag save();
}
