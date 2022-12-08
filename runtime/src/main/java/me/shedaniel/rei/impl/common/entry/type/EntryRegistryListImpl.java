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
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntryRegistryListImpl implements EntryRegistryList {
    private final List<HashedEntryStackWrapper> hashedList = new ArrayList<>(BuiltInRegistries.ITEM.keySet().size() + 100);
    private final List<EntryStack<?>> list = createMappedList(hashedList);
    
    public EntryRegistryListImpl() {
    }
    
    public EntryRegistryListImpl(Stream<EntryStack<?>> list) {
        list.collect(Collectors.toCollection(() -> this.list));
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public Stream<EntryStack<?>> stream() {
        return list.stream();
    }
    
    @Override
    public List<EntryStack<?>> collect() {
        return list;
    }
    
    @Override
    public List<HashedEntryStackWrapper> collectHashed() {
        return hashedList;
    }
    
    @Override
    public int indexOf(EntryStack<?> stack) {
        return list.indexOf(stack);
    }
    
    @Override
    public int lastIndexOf(EntryStack<?> stack) {
        return list.lastIndexOf(stack);
    }
    
    @Override
    public void add(EntryStack<?> stack, long hashExact) {
        hashedList.add(new HashedEntryStackWrapper(stack, hashExact));
    }
    
    @Override
    public void add(int index, EntryStack<?> stack, long hashExact) {
        hashedList.add(index, new HashedEntryStackWrapper(stack, hashExact));
    }
    
    @Override
    public void addAll(List<EntryStack<?>> stacks, LongList hashes) {
        hashedList.addAll(new AbstractList<>() {
            @Override
            public HashedEntryStackWrapper get(int index) {
                return new HashedEntryStackWrapper(stacks.get(index), hashes.getLong(index));
            }
            
            @Override
            public int size() {
                return stacks.size();
            }
        });
    }
    
    @Override
    public void addAll(int index, List<EntryStack<?>> stacks, LongList hashes) {
        hashedList.addAll(index, new AbstractList<>() {
            @Override
            public HashedEntryStackWrapper get(int index) {
                return new HashedEntryStackWrapper(stacks.get(index), hashes.getLong(index));
            }
            
            @Override
            public int size() {
                return stacks.size();
            }
        });
    }
    
    @Override
    public void remove(EntryStack<?> stack, long hashExact) {
        hashedList.remove(new HashedEntryStackWrapper(stack, hashExact));
    }
    
    @Override
    public boolean removeExactIf(StackFilteringPredicate predicate) {
        return hashedList.removeIf(stack -> predicate.test(stack.unwrap(), stack.hashExact()));
    }
    
    @Override
    public boolean needsHash() {
        return true;
    }
    
    public List<EntryStack<?>> getList() {
        return list;
    }
    
    private static List<EntryStack<?>> createMappedList(List<HashedEntryStackWrapper> hashedList) {
        return new AbstractList<>() {
            @Override
            public EntryStack<?> get(int index) {
                return hashedList.get(index).unwrap();
            }
            
            @Override
            public int size() {
                return hashedList.size();
            }
            
            @Override
            public void add(int index, EntryStack<?> element) {
                hashedList.add(index, new HashedEntryStackWrapper(element));
            }
            
            @Override
            public EntryStack<?> set(int index, EntryStack<?> element) {
                return hashedList.set(index, new HashedEntryStackWrapper(element)).unwrap();
            }
            
            @Override
            public boolean remove(Object o) {
                if (o instanceof EntryStack) {
                    return hashedList.remove(new HashedEntryStackWrapper((EntryStack<?>) o));
                } else {
                    return false;
                }
            }
            
            @Override
            public EntryStack<?> remove(int index) {
                return hashedList.remove(index).unwrap();
            }
            
            @Override
            public void clear() {
                hashedList.clear();
            }
            
            @Override
            public int indexOf(Object o) {
                if (o instanceof EntryStack<?> stack) {
                    return hashedList.indexOf(new HashedEntryStackWrapper(stack));
                } else {
                    return -1;
                }
            }
            
            @Override
            public int lastIndexOf(Object o) {
                if (o instanceof EntryStack<?> stack) {
                    return hashedList.lastIndexOf(new HashedEntryStackWrapper(stack));
                } else {
                    return -1;
                }
            }
            
            @Override
            public boolean contains(Object o) {
                if (o instanceof EntryStack<?> stack) {
                    return hashedList.contains(new HashedEntryStackWrapper(stack));
                } else {
                    return false;
                }
            }
        };
    }
}
