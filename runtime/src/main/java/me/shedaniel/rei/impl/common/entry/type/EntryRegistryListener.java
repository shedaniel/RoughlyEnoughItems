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

package me.shedaniel.rei.impl.common.entry.type;

import it.unimi.dsi.fastutil.longs.LongList;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public interface EntryRegistryListener {
    default void addEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack, long stackHashExact) {}
    
    default void addEntriesAfter(@Nullable EntryStack<?> afterEntry, List<EntryStack<?>> stacks, @Nullable LongList hashes) {}
    
    default void removeEntry(EntryStack<?> stack, long hashExact) {}
    
    default void removeEntries(List<EntryStack<?>> stacks, @Nullable LongList hashes) {}
    
    default void removeEntriesIf(Predicate<EntryStack<?>> predicate) {}
    
    default void onReFilter(List<EntryStack<?>> stacks) {}
}
