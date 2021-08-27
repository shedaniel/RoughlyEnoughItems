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

package me.shedaniel.rei.impl.common.entry.comparison;

import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparatorRegistry;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

@ApiStatus.Internal
public abstract class EntryComparatorRegistryImpl<T, S> implements EntryComparatorRegistry<T, S> {
    private static final Logger LOGGER = LogManager.getLogger(EntryComparatorRegistryImpl.class);
    private final Map<S, EntryComparator<T>> comparators = new IdentityHashMap<>();
    
    @Override
    public void register(EntryComparator<T> comparator, S entry) {
        EntryComparator<T> put = this.comparators.put(entry, comparator);
        if (put != null) {
            LOGGER.warn("[REI] Overriding " + put + "entry comparator with " + comparator + "for " + entry + "! This may result in unwanted comparisons!");
        }
    }
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void startReload() {
        comparators.clear();
    }
    
    public abstract S getEntry(T stack);
    
    public abstract EntryComparator<T> defaultComparator();
    
    @Override
    public long hashOf(ComparisonContext context, T stack) {
        EntryComparator<T> comparator = comparators.get(getEntry(stack));
        if (comparator != null) {
            return comparator.hash(context, stack);
        }
        return defaultComparator().hash(context, stack);
    }
    
    @Override
    public boolean containsComparator(S item) {
        return comparators.containsKey(item);
    }
    
    @Override
    public int comparatorSize() {
        return this.comparators.size();
    }
}
