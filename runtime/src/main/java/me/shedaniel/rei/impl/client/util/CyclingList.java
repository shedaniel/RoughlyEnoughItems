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

package me.shedaniel.rei.impl.client.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
public interface CyclingList<T> extends Supplier<List<T>> {
    default T peek() {
        next();
        return previous();
    }
    
    default boolean hasNext() {
        int nextIndex = nextIndex();
        return currentIndex() < nextIndex && nextIndex < size();
    }
    
    default boolean hasPrevious() {
        int previousIndex = previousIndex();
        return previousIndex >= 0 && currentIndex() > previousIndex;
    }
    
    T next();
    
    T previous();
    
    int nextIndex();
    
    int previousIndex();
    
    void resetToStart();
    
    int size();
    
    int currentIndex();
    
    @ApiStatus.Experimental
    interface Mutable<T> extends CyclingList<T> {
        void add(T entry);
        
        void addAll(Collection<? extends T> entries);
        
        void clear();
    }
    
    static <T> CyclingList<T> of(List<T> list, Supplier<T> empty) {
        return new AbstractIndexedCyclingList<>() {
            @Override
            public List<T> get() {
                return list;
            }
            
            @Override
            protected T get(int index) {
                return list.get(index);
            }
            
            @Override
            protected T empty() {
                return empty.get();
            }
            
            @Override
            public int size() {
                return list.size();
            }
        };
    }
    
    static <T> CyclingList.Mutable<T> ofMutable(List<T> list, Supplier<T> empty) {
        return new AbstractIndexedCyclingList.Mutable<>() {
            @Override
            public List<T> get() {
                return list;
            }
            
            @Override
            protected T get(int index) {
                return list.get(index);
            }
            
            @Override
            protected T empty() {
                return empty.get();
            }
            
            @Override
            public int size() {
                return list.size();
            }
            
            @Override
            public void add(T entry) {
                list.add(entry);
            }
            
            @Override
            public void addAll(Collection<? extends T> entries) {
                list.addAll(entries);
            }
            
            @Override
            public void clear() {
                list.clear();
            }
        };
    }
    
    static <T> CyclingList<T> of(Supplier<T> empty) {
        return new CyclingList<>() {
            @Override
            public List<T> get() {
                return List.of();
            }
            
            @Override
            public T peek() {
                return empty.get();
            }
            
            @Override
            public void resetToStart() {
            }
            
            @Override
            public int size() {
                return 0;
            }
            
            @Override
            public int currentIndex() {
                return 0;
            }
            
            @Override
            public T next() {
                return empty.get();
            }
            
            @Override
            public T previous() {
                return empty.get();
            }
            
            @Override
            public int nextIndex() {
                return 1;
            }
            
            @Override
            public int previousIndex() {
                return -1;
            }
        };
    }
    
    static <T> CyclingList.Mutable<T> ofMutable(Supplier<T> empty) {
        return new Mutable<>() {
            private List<T> list;
            private CyclingList<T> provider;
            
            
            @Override
            public List<T> get() {
                return this.list == null ? List.of() : this.list;
            }
            
            @Override
            public T peek() {
                if (this.provider == null) return empty.get();
                return this.provider.peek();
            }
            
            @Override
            public T previous() {
                if (this.provider == null) return empty.get();
                return this.provider.previous();
            }
            
            @Override
            public int nextIndex() {
                return this.provider == null ? 1 : this.provider.nextIndex();
            }
            
            @Override
            public int previousIndex() {
                return this.provider == null ? -1 : this.provider.previousIndex();
            }
            
            @Override
            public T next() {
                if (this.provider == null) return empty.get();
                return this.provider.next();
            }
            
            @Override
            public void add(T entry) {
                if (this.list == null || this.list.isEmpty()) {
                    this.list = Collections.singletonList(entry);
                    this.provider = CyclingList.ofMutable(this.list, empty);
                } else {
                    if (!(this.list instanceof ArrayList)) {
                        this.list = new ArrayList<>(this.list);
                        this.provider = CyclingList.ofMutable(this.list, empty);
                    }
                    this.list.add(entry);
                }
            }
            
            @Override
            public void resetToStart() {
                if (this.provider != null) {
                    this.provider.resetToStart();
                }
            }
            
            @Override
            public int size() {
                return this.list == null ? 0 : this.list.size();
            }
            
            @Override
            public int currentIndex() {
                return this.provider == null ? 0 : this.provider.currentIndex();
            }
            
            @Override
            public void addAll(Collection<? extends T> entries) {
                if (this.list == null) {
                    this.list = new ArrayList<>(entries);
                    this.provider = CyclingList.ofMutable(this.list, empty);
                } else {
                    if (!(this.list instanceof ArrayList)) {
                        this.list = new ArrayList<>(this.list);
                        this.provider = CyclingList.ofMutable(this.list, empty);
                    }
                    this.list.addAll(entries);
                }
            }
            
            @Override
            public void clear() {
                this.list = null;
                this.provider = null;
            }
        };
    }
    
    static <T> CyclingList.Mutable<T> ofMutable(CyclingList<T> provider, Supplier<T> empty) {
        return new Mutable<>() {
            private final CyclingList.Mutable<T> mutable = CyclingList.ofMutable(empty);
            private CyclingList<T> concat = CyclingList.concat(List.of(provider, mutable), empty);
            
            @Override
            public List<T> get() {
                return this.concat.get();
            }
            
            @Override
            public T peek() {
                return this.concat.peek();
            }
            
            @Override
            public T previous() {
                return this.concat.previous();
            }
            
            @Override
            public int nextIndex() {
                return this.concat.nextIndex();
            }
            
            @Override
            public int previousIndex() {
                return this.concat.previousIndex();
            }
            
            @Override
            public T next() {
                return this.concat.next();
            }
            
            @Override
            public void add(T entry) {
                this.mutable.add(entry);
            }
            
            @Override
            public void resetToStart() {
                this.concat.resetToStart();
            }
            
            @Override
            public int size() {
                return this.concat.size();
            }
            
            @Override
            public int currentIndex() {
                return this.concat.currentIndex();
            }
            
            @Override
            public void addAll(Collection<? extends T> entries) {
                this.mutable.addAll(entries);
            }
            
            @Override
            public void clear() {
                this.concat = this.mutable;
                this.mutable.clear();
            }
        };
    }
    
    static <T> CyclingList<T> concat(Collection<CyclingList<T>> providers, Supplier<T> empty) {
        return providers.stream().reduce((a, b) -> new ConcatenatedListIterator<T>(a, b) {
            @Override
            protected T empty() {
                return empty.get();
            }
        }).orElse(CyclingList.of(empty));
    }
}
