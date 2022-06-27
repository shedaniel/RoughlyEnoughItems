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

package me.shedaniel.rei.api.common.entry.comparison;

import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;

/**
 * Registry for registering custom methods for identifying variants of {@link T}.
 * The default comparator is {@link EntryComparator#noop()} when fuzzy, which does not compare the NBT of the entries;
 * and nbt when exact.
 */
public interface EntryComparatorRegistry<T, S> extends Reloadable<REIPlugin<?>> {
    /**
     * Registers an {@link EntryComparator} for the given entry {@link S}.
     *
     * @param comparator the comparator to register
     * @param entry      the entry to register the comparator for
     */
    void register(EntryComparator<T> comparator, S entry);
    
    /**
     * Registers an {@link EntryComparator} for the given entries {@link S}.
     *
     * @param comparator the comparator to register
     * @param entries    the entries to register the comparator for
     */
    default void register(EntryComparator<T> comparator, S... entries) {
        for (S entry : entries) {
            register(comparator, entry);
        }
    }
    
    /**
     * Registers an {@link EntryComparator} globally for all entries {@link S}.
     *
     * @param comparator the comparator to register
     */
    void registerGlobal(EntryComparator<T> comparator);
    
    /**
     * Returns hash code of the {@link T} stack.
     *
     * @param context the context to use
     * @param stack   the stack to hash code
     * @return the hash code of the {@code context} context
     */
    long hashOf(ComparisonContext context, T stack);
    
    /**
     * Returns whether there are any comparators registered for the {@link S} entry.
     *
     * @param entry the entry to check
     * @return whether there are any comparators registered for the {@code entry} entry
     */
    boolean containsComparator(S entry);
    
    /**
     * Returns the number of registered comparators for this registry.
     *
     * @return the number of registered comparators for this registry
     */
    int comparatorSize();
}
