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
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReloadingEntryRegistryList implements EntryRegistryList {
    private List<HashedEntryStackWrapper> list = new ArrayList<>(Registry.ITEM.keySet().size() + 100);
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public Stream<EntryStack<?>> stream() {
        return list.stream().map(HashedEntryStackWrapper::unwrap);
    }
    
    @Override
    public List<EntryStack<?>> collect() {
        return CollectionUtils.map(list, HashedEntryStackWrapper::unwrap);
    }
    
    @Override
    public int indexOf(EntryStack<?> stack) {
        return list.indexOf(new HashedEntryStackWrapper(stack));
    }
    
    @Override
    public int lastIndexOf(EntryStack<?> stack) {
        return list.lastIndexOf(new HashedEntryStackWrapper(stack));
    }
    
    @Override
    public void add(EntryStack<?> stack, long hashExact) {
        list.add(new HashedEntryStackWrapper(stack, hashExact));
    }
    
    @Override
    public void add(int index, EntryStack<?> stack, long hashExact) {
        list.add(index, new HashedEntryStackWrapper(stack, hashExact));
    }
    
    @Override
    public void addAll(List<EntryStack<?>> stacks, LongList hashes) {
        List<HashedEntryStackWrapper> wrappers = CollectionUtils.mapIndexed(stacks, (i, stack) -> new HashedEntryStackWrapper(stack, hashes.getLong(i)));
        list.addAll(wrappers);
    }
    
    @Override
    public void addAll(int index, List<EntryStack<?>> stacks, LongList hashes) {
        List<HashedEntryStackWrapper> wrappers = CollectionUtils.mapIndexed(stacks, (i, stack) -> new HashedEntryStackWrapper(stack, hashes.getLong(i)));
        list.addAll(index, wrappers);
    }
    
    @Override
    public void remove(EntryStack<?> stack, long hashExact) {
        list.remove(new HashedEntryStackWrapper(stack, hashExact));
    }
    
    @Override
    public boolean removeIf(Predicate<? extends EntryStack<?>> predicate) {
        return list.removeIf(wrapper -> ((Predicate<EntryStack<?>>) predicate).test(wrapper.unwrap()));
    }
    
    @Override
    public boolean removeExactIf(LongPredicate predicate) {
        return list.removeIf(wrapper -> predicate.test(wrapper.hashExact()));
    }
    
    @Override
    public boolean needsHash() {
        return true;
    }
}
