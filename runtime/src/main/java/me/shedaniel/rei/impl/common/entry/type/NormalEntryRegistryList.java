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
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NormalEntryRegistryList implements EntryRegistryList {
    private List<EntryStack<?>> list = new ArrayList<>();
    
    public NormalEntryRegistryList() {
    }
    
    public NormalEntryRegistryList(Stream<EntryStack<?>> list) {
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
    public int indexOf(EntryStack<?> stack) {
        return list.indexOf(stack);
    }
    
    @Override
    public int lastIndexOf(EntryStack<?> stack) {
        return list.lastIndexOf(stack);
    }
    
    @Override
    public void add(EntryStack<?> stack, long hashExact) {
        list.add(stack);
    }
    
    @Override
    public void add(int index, EntryStack<?> stack, long hashExact) {
        list.add(index, stack);
    }
    
    @Override
    public void addAll(List<EntryStack<?>> stacks, LongList hashes) {
        list.addAll(stacks);
    }
    
    @Override
    public void addAll(int index, List<EntryStack<?>> stacks, LongList hashes) {
        list.addAll(index, stacks);
    }
    
    @Override
    public void remove(EntryStack<?> stack, long hashExact) {
        list.remove(stack);
    }
    
    @Override
    public boolean removeIf(Predicate<? extends EntryStack<?>> predicate) {
        return list.removeIf((Predicate<? super EntryStack<?>>) predicate);
    }
    
    @Override
    public boolean removeExactIf(LongPredicate predicate) {
        return list.removeIf(stack -> predicate.test(EntryStacks.hashExact(stack)));
    }
    
    @Override
    public boolean needsHash() {
        return false;
    }
    
    public List<EntryStack<?>> getList() {
        return list;
    }
}
