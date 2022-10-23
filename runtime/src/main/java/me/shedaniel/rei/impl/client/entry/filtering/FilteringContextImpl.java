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

package me.shedaniel.rei.impl.client.entry.filtering;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.rei.api.client.entry.filtering.FilteringContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Environment(EnvType.CLIENT)
public class FilteringContextImpl implements FilteringContext {
    private final boolean async;
    public final Map<FilteringContextType, Set<HashedEntryStackWrapper>> stacks;
    
    public FilteringContextImpl(Collection<EntryStack<?>> allStacks) {
        this(true, allStacks);
    }
    
    public FilteringContextImpl(boolean async, Collection<EntryStack<?>> allStacks) {
        this.async = async;
        this.stacks = Maps.newHashMap();
        for (FilteringContextType type : FilteringContextType.values()) {
            this.stacks.computeIfAbsent(type, t -> Sets.newHashSet());
        }
        this.stacks.get(FilteringContextType.DEFAULT).addAll(async ? CollectionUtils.mapParallel(allStacks, HashedEntryStackWrapper::new)
                : CollectionUtils.map(allStacks, HashedEntryStackWrapper::new));
    }
    
    public FilteringContextImpl(Map<FilteringContextType, Set<HashedEntryStackWrapper>> stacks) {
        this.async = false;
        this.stacks = stacks;
        for (FilteringContextType type : FilteringContextType.values()) {
            this.stacks.computeIfAbsent(type, t -> Sets.newHashSet());
        }
    }
    
    @Override
    public Collection<EntryStack<?>> getHiddenStacks() {
        return getPublicFacing(FilteringContextType.HIDDEN);
    }
    
    @Override
    public Collection<EntryStack<?>> getShownStacks() {
        return getPublicFacing(FilteringContextType.SHOWN);
    }
    
    @Override
    public Collection<EntryStack<?>> getUnsetStacks() {
        return getPublicFacing(FilteringContextType.DEFAULT);
    }
    
    private Collection<EntryStack<?>> getPublicFacing(FilteringContextType type) {
        Set<HashedEntryStackWrapper> wrappers = this.stacks.get(type);
        if (wrappers == null || wrappers.isEmpty()) return List.of();
        return new AbstractSet<>() {
            @Override
            public Iterator<EntryStack<?>> iterator() {
                return Iterators.transform(wrappers.iterator(), HashedEntryStackWrapper::unwrap);
            }
            
            @Override
            public int size() {
                return wrappers.size();
            }
        };
    }
    
    public void handleResult(FilteringResultImpl result) {
        Collection<HashedEntryStackWrapper> hiddenStacks = result.hiddenStacks;
        Collection<HashedEntryStackWrapper> shownStacks = result.shownStacks;
        
        if (async) {
            List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
            completableFutures.add(CompletableFuture.runAsync(() -> {
                this.stacks.get(FilteringContextType.DEFAULT).removeAll(hiddenStacks);
                this.stacks.get(FilteringContextType.DEFAULT).removeAll(shownStacks);
            }));
            completableFutures.add(CompletableFuture.runAsync(() -> {
                this.stacks.get(FilteringContextType.SHOWN).removeAll(hiddenStacks);
                this.stacks.get(FilteringContextType.SHOWN).addAll(shownStacks);
            }));
            completableFutures.add(CompletableFuture.runAsync(() -> {
                this.stacks.get(FilteringContextType.HIDDEN).addAll(hiddenStacks);
                this.stacks.get(FilteringContextType.HIDDEN).removeAll(shownStacks);
            }));
            try {
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(5, TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        } else {
            this.stacks.get(FilteringContextType.DEFAULT).removeAll(hiddenStacks);
            this.stacks.get(FilteringContextType.DEFAULT).removeAll(shownStacks);
            this.stacks.get(FilteringContextType.SHOWN).removeAll(hiddenStacks);
            this.stacks.get(FilteringContextType.SHOWN).addAll(shownStacks);
            this.stacks.get(FilteringContextType.HIDDEN).addAll(hiddenStacks);
            this.stacks.get(FilteringContextType.HIDDEN).removeAll(shownStacks);
        }
    }
}
