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

package me.shedaniel.rei.impl.client.entry.filtering.rules;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.rei.api.client.entry.filtering.*;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.util.ThreadCreator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum BasicFilteringRuleImpl implements BasicFilteringRule<Unit> {
    INSTANCE;
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-BasicFiltering").asService();
    private final LongSet hiddenHashes = new LongOpenHashSet(), shownHashes = new LongOpenHashSet();
    private final List<CachedProvider> hiddenProviders = new ArrayList<>(), shownProviders = new ArrayList<>();
    
    @Override
    public FilteringRuleType<? extends FilteringRule<Unit>> getType() {
        return BasicFilteringRuleType.INSTANCE;
    }
    
    @Override
    public Unit prepareCache(boolean async) {
        for (CachedProvider provider : hiddenProviders) {
            provider.get();
        }
        for (CachedProvider provider : shownProviders) {
            provider.get();
        }
        return Unit.INSTANCE;
    }
    
    @Override
    public FilteringResult processFilteredStacks(FilteringContext context, FilteringResultFactory resultFactory, Unit cache, boolean async) {
        FilteringResult result = resultFactory.create();
        hideList(context.getShownStacks(), context.getShownExactHashes(), result, async, hiddenHashes);
        hideList(context.getUnsetStacks(), context.getUnsetExactHashes(), result, async, hiddenHashes);
        
        for (CachedProvider provider : hiddenProviders) {
            hideList(context.getShownStacks(), context.getShownExactHashes(), result, async, provider.getExactHashes());
            hideList(context.getUnsetStacks(), context.getUnsetExactHashes(), result, async, provider.getExactHashes());
        }
        
        showList(context.getHiddenStacks(), context.getHiddenExactHashes(), result, async, shownHashes);
        showList(context.getUnsetStacks(), context.getUnsetExactHashes(), result, async, shownHashes);
        
        for (CachedProvider provider : shownProviders) {
            showList(context.getHiddenStacks(), context.getHiddenExactHashes(), result, async, provider.getExactHashes());
            showList(context.getUnsetStacks(), context.getUnsetExactHashes(), result, async, provider.getExactHashes());
        }
        
        return result;
    }
    
    private void hideList(Collection<EntryStack<?>> stacks, LongCollection hashes, FilteringResult result, boolean async, LongSet filteredStacks) {
        LongIterator iterator = hashes.iterator();
        result.hide(stacks.stream()
                .filter(stack -> filteredStacks.contains(iterator.nextLong()))
                .collect(Collectors.toList()));
    }
    
    private void showList(Collection<EntryStack<?>> stacks, LongCollection hashes, FilteringResult result, boolean async, LongSet filteredStacks) {
        LongIterator iterator = hashes.iterator();
        result.show(stacks.stream()
                .filter(stack -> filteredStacks.contains(iterator.nextLong()))
                .collect(Collectors.toList()));
    }
    
    @Override
    public FilteringResult hide(EntryStack<?> stack) {
        long hashExact = EntryStacks.hashExact(stack);
        hiddenHashes.add(hashExact);
        shownHashes.remove(hashExact);
        
        if (!isReloading()) {
            markDirty(List.of(stack), null);
        }
        
        return this;
    }
    
    @Override
    public FilteringResult hide(Collection<? extends EntryStack<?>> stacks) {
        for (EntryStack<?> stack : stacks) {
            long hashExact = EntryStacks.hashExact(stack);
            hiddenHashes.add(hashExact);
            shownHashes.remove(hashExact);
        }
        
        if (!isReloading()) {
            markDirty((Collection<EntryStack<?>>) stacks, null);
        }
        
        return this;
    }
    
    @Override
    public FilteringResult show(EntryStack<?> stack) {
        long hashExact = EntryStacks.hashExact(stack);
        shownHashes.add(hashExact);
        hiddenHashes.remove(hashExact);
        
        if (!isReloading()) {
            markDirty(List.of(stack), null);
        }
        
        return this;
    }
    
    @Override
    public FilteringResult show(Collection<? extends EntryStack<?>> stacks) {
        for (EntryStack<?> stack : stacks) {
            long hashExact = EntryStacks.hashExact(stack);
            shownHashes.add(hashExact);
            hiddenHashes.remove(hashExact);
        }
        
        if (!isReloading()) {
            markDirty((Collection<EntryStack<?>>) stacks, null);
        }
        
        return this;
    }
    
    @Override
    public MarkDirty hide(Supplier<Collection<EntryStack<?>>> provider) {
        CachedProvider cachedProvider = new CachedProvider(provider);
        shownProviders.remove(cachedProvider);
        hiddenProviders.add(cachedProvider);
        
        cachedProvider.markDirty();
        return cachedProvider;
    }
    
    @Override
    public MarkDirty show(Supplier<Collection<EntryStack<?>>> provider) {
        CachedProvider cachedProvider = new CachedProvider(provider);
        hiddenProviders.remove(cachedProvider);
        shownProviders.add(cachedProvider);
        
        cachedProvider.markDirty();
        return cachedProvider;
    }
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void startReload() {
        hiddenHashes.clear();
        shownHashes.clear();
        hiddenProviders.clear();
        shownProviders.clear();
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerBasicEntryFiltering(this);
    }
    
    private class CachedProvider implements MarkDirty {
        private final Supplier<Collection<EntryStack<?>>> provider;
        private final LazyResettable<Pair<Collection<EntryStack<?>>, LongSet>> cache = new LazyResettable<>(this::compose);
        
        private CachedProvider(Supplier<Collection<EntryStack<?>>> provider) {
            this.provider = provider;
        }
        
        @Override
        public void markDirty() {
            Pair<Collection<EntryStack<?>>, LongSet> prev = this.cache.get();
            this.cache.reset();
            Pair<Collection<EntryStack<?>>, LongSet> next = this.cache.get();
            BasicFilteringRuleImpl.this.markDirty(prev.getFirst(), prev.getSecond());
            BasicFilteringRuleImpl.this.markDirty(next.getFirst(), next.getSecond());
        }
        
        public Collection<EntryStack<?>> get() {
            return this.cache.get().getFirst();
        }
        
        public LongSet getExactHashes() {
            return this.cache.get().getSecond();
        }
        
        private Pair<Collection<EntryStack<?>>, LongSet> compose() {
            Collection<EntryStack<?>> stacks = this.provider.get();
            LongSet hashes = new LongOpenHashSet(stacks.size());
            for (EntryStack<?> stack : stacks) {
                hashes.add(EntryStacks.hashExact(stack));
            }
            return Pair.of(stacks, hashes);
        }
    }
}
