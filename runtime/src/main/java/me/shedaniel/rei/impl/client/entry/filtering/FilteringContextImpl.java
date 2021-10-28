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

package me.shedaniel.rei.impl.client.entry.filtering;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Environment(EnvType.CLIENT)
public class FilteringContextImpl implements FilteringContext {
    public final Map<FilteringContextType, Set<HashedEntryStackWrapper>> stacks;
    private final Map<FilteringContextType, Collection<EntryStack<?>>> cachedStacks;
    
    public FilteringContextImpl(Collection<EntryStack<?>> allStacks) {
        this.stacks = Maps.newHashMap();
        this.cachedStacks = Maps.newHashMap();
        for (FilteringContextType type : FilteringContextType.values()) {
            this.stacks.computeIfAbsent(type, t -> Sets.newHashSet());
        }
        this.stacks.get(FilteringContextType.DEFAULT).addAll(CollectionUtils.mapParallel(allStacks, HashedEntryStackWrapper::new));
        fillCache();
    }
    
    public FilteringContextImpl(Map<FilteringContextType, Set<HashedEntryStackWrapper>> stacks) {
        this.stacks = stacks;
        this.cachedStacks = Maps.newHashMap();
        for (FilteringContextType type : FilteringContextType.values()) {
            this.stacks.computeIfAbsent(type, t -> Sets.newHashSet());
        }
        fillCache();
    }
    
    private void fillCache() {
        this.cachedStacks.clear();
        for (FilteringContextType type : FilteringContextType.values()) {
            this.cachedStacks.put(type, CollectionUtils.map(stacks.get(type), HashedEntryStackWrapper::unwrap));
        }
    }
    
    @Override
    public Collection<EntryStack<?>> getStacks(FilteringContextType type) {
        return cachedStacks.get(type);
    }
    
    public void handleResult(FilteringResult result) {
        Collection<HashedEntryStackWrapper> hiddenStacks = result.getHiddenStacks();
        Collection<HashedEntryStackWrapper> shownStacks = result.getShownStacks();
        
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
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(20, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        fillCache();
    }
}
