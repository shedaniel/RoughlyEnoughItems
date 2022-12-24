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

package me.shedaniel.rei.impl.client.util;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class OriginalRetainingCyclingList<T> implements CyclingList.Mutable<T> {
    private final Supplier<T> empty;
    @Nullable
    private CyclingList<T> backing = null;
    
    public OriginalRetainingCyclingList(Supplier<T> empty) {
        this.empty = empty;
    }
    
    @Override
    public List<T> get() {
        if (this.backing == null) return List.of();
        return this.backing.get();
    }
    
    @Override
    public T peek() {
        if (this.backing == null) return empty.get();
        return this.backing.peek();
    }
    
    @Override
    public T previous() {
        if (this.backing == null) return empty.get();
        return this.backing.previous();
    }
    
    @Override
    public int nextIndex() {
        return this.backing == null ? 0 : this.backing.nextIndex();
    }
    
    @Override
    public int previousIndex() {
        return this.backing == null ? -1 : this.backing.previousIndex();
    }
    
    @Override
    public T next() {
        if (this.backing == null) return empty.get();
        return this.backing.next();
    }
    
    @Override
    public void add(T entry) {
        if (this.backing instanceof CyclingList.Mutable<T> mutable) {
            mutable.add(entry);
        } else if (this.backing == null) {
            this.backing = CyclingList.of(List.of(entry), this.empty);
        } else {
            CyclingList.Mutable<T> mutable = CyclingList.ofMutable(this.backing, this.empty);
            mutable.add(entry);
            this.backing = mutable;
        }
    }
    
    @Override
    public void resetToStart() {
        if (this.backing != null) this.backing.resetToStart();
    }
    
    @Override
    public int size() {
        return this.backing == null ? 0 : this.backing.size();
    }
    
    @Override
    public int currentIndex() {
        return this.backing == null ? 0 : this.backing.currentIndex();
    }
    
    @Override
    public void addAll(Collection<? extends T> entries) {
        if (!entries.isEmpty()) {
            if (this.backing instanceof CyclingList.Mutable<T> mutable) {
                mutable.addAll(entries);
            } else if (this.backing == null) {
                List<T> list;
                if (entries instanceof List<?> stacksAsList) list = (List<T>) entries;
                else list = getListFromCollection(entries);
                this.backing = CyclingList.of(list, this.empty);
            } else {
                CyclingList.Mutable<T> mutable = CyclingList.ofMutable(this.backing, this.empty);
                mutable.addAll(entries);
                this.backing = mutable;
            }
        }
    }
    
    @Override
    public void clear() {
        if (this.backing instanceof CyclingList.Mutable<T> mutable) {
            mutable.clear();
        } else {
            this.backing = null;
        }
    }
    
    public void setBacking(@Nullable CyclingList<T> backing) {
        this.backing = backing;
    }
    
    private static <T> AbstractList<T> getListFromCollection(Collection<? extends T> entries) {
        return new AbstractList<>() {
            @Override
            public T get(int index) {
                return Iterables.get(entries, index);
            }
            
            @Override
            public int size() {
                return entries.size();
            }
            
            @Override
            public Iterator<T> iterator() {
                return (Iterator<T>) entries.iterator();
            }
            
            @Override
            public boolean add(T element) {
                return ((Collection<T>) entries).add(element);
            }
            
            @Override
            public void add(int index, T element) {
                add(element);
            }
            
            @Override
            public T remove(int index) {
                T stack = get(index);
                return stack == null && entries.remove(stack) ? stack : null;
            }
        };
    }
    
    public CyclingList<T> getBacking() {
        if (this.backing == null) return CyclingList.of(this.empty);
        return this.backing;
    }
}
