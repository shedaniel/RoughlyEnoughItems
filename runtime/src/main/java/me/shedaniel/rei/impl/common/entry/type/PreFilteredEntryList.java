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

import com.google.common.base.Stopwatch;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.*;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextType;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class PreFilteredEntryList implements FilteredEntryList {
    private final EntryRegistry registry;
    private final EntryRegistryList list;
    private final Map<FilteringRule<?>, DataPair> filteringData = new HashMap<>();
    private final Long2BooleanMap cached = new Long2BooleanOpenHashMap();
    private final List<EntryStack<?>> listView = new InternalListView();
    private long mod = 0;
    
    public PreFilteredEntryList(EntryRegistry registry, EntryRegistryList list) {
        this.registry = registry;
        this.list = list;
    }
    
    private static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>) target.negate();
    }
    
    @Override
    public void addEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack, long stackHashExact) {
        if (!registry.isReloading()) {
            refreshFilteringFor(null, List.of(stack), LongList.of(stackHashExact));
        }
    }
    
    @Override
    public void addEntriesAfter(@Nullable EntryStack<?> afterEntry, List<EntryStack<?>> stacks, @Nullable LongList hashes) {
        if (!registry.isReloading()) {
            refreshFilteringFor(null, stacks, hashes);
        }
    }
    
    @Override
    public void removeEntry(EntryStack<?> stack, long hashExact) {
        if (!registry.isReloading()) {
            removeFilteringFor(List.of(stack), LongList.of(hashExact));
        }
    }
    
    @Override
    public void removeEntries(List<EntryStack<?>> stacks, @Nullable LongList hashes) {
        if (!registry.isReloading()) {
            removeFilteringFor(stacks, hashes);
        }
    }
    
    @Override
    public void onReFilter(List<HashedEntryStackWrapper> stacks) {
        ConfigObject config = ConfigObject.getInstance();
        if (config.getFilteredStackProviders() != null) {
            List<EntryStack<?>> normalizedFilteredStacks = CollectionUtils.map(config.getFilteredStackProviders(), EntryStackProvider::provide);
            normalizedFilteredStacks.removeIf(EntryStack::isEmpty);
            config.getFilteredStackProviders().clear();
            config.getFilteredStackProviders().addAll(CollectionUtils.map(normalizedFilteredStacks, EntryStackProvider::ofStack));
        }
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        refreshFilteringFor(true, null, Lists.transform(stacks, HashedEntryStackWrapper::unwrap), new AbstractLongList() {
            @Override
            public long getLong(int index) {
                return stacks.get(index).hashExact();
            }
            
            @Override
            public int size() {
                return stacks.size();
            }
        });
        InternalLogger.getInstance().debug("Refiltered entries with %d rules in %s.", FilteringLogic.getRules().size(), stopwatch.stop().toString());
    }
    
    private void queueSearchUpdate() {
        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadSearch);
    }
    
    @Override
    public void refreshFilteringFor(@Nullable Set<FilteringRule<?>> refilterRules, Collection<EntryStack<?>> stack, @Nullable LongCollection hashes) {
        refreshFilteringFor(false, refilterRules, stack, hashes);
    }
    
    @Override
    public void refreshFilteringFor(boolean log, @Nullable Set<FilteringRule<?>> refilterRules, Collection<EntryStack<?>> stacks, @Nullable LongCollection hashes) {
        if (hashes == null) {
            hashes = new LongArrayList(stacks.size());
            for (EntryStack<?> stack : stacks) {
                hashes.add(EntryStacks.hashExact(stack));
            }
        }
        
        LongIterator hashIterator = hashes.iterator();
        while (hashIterator.hasNext()) {
            long hash = hashIterator.nextLong();
            cached.remove(hash);
        }
        
        List<FilteringRule<?>> rules = FilteringLogic.getRules();
        for (int i = rules.size() - 1; i >= 0; i--) {
            FilteringRule<?> rule = rules.get(i);
            if (!filteringData.containsKey(rule)) filteringData.put(rule, new DataPair());
            DataPair longPair = filteringData.get(rule);
            LongSet hidden = longPair.hidden();
            LongSet shown = longPair.shown();
            boolean refilter = refilterRules == null || refilterRules.contains(rule);
            if (refilter) {
                if (!hidden.isEmpty()) {
                    hidden.removeAll(hashes);
                    mod++;
                }
                if (!shown.isEmpty()) {
                    shown.removeAll(hashes);
                    mod++;
                }
                Map<FilteringContextType, Set<HashedEntryStackWrapper>> map = FilteringLogic.hidden(List.of(rule), log, true, stacks);
                Set<HashedEntryStackWrapper> hiddenWrappers = map.get(FilteringContextType.HIDDEN);
                Set<HashedEntryStackWrapper> shownWrappers = map.get(FilteringContextType.SHOWN);
                for (HashedEntryStackWrapper stack : hiddenWrappers) {
                    hidden.add(stack.hashExact());
                    cached.put(stack.hashExact(), false);
                }
                for (HashedEntryStackWrapper stack : shownWrappers) {
                    shown.add(stack.hashExact());
                    cached.put(stack.hashExact(), true);
                }
                if (!hiddenWrappers.isEmpty() || !shownWrappers.isEmpty()) mod++;
            } else {
                hashIterator = hashes.iterator();
                while (hashIterator.hasNext()) {
                    long hash = hashIterator.nextLong();
                    if (hidden.contains(hash)) {
                        cached.put(hash, false);
                        mod++;
                    } else if (shown.contains(hash)) {
                        cached.put(hash, true);
                        mod++;
                    }
                }
            }
        }
        
        queueSearchUpdate();
    }
    
    private void removeFilteringFor(List<EntryStack<?>> stacks, @Nullable LongList hashes) {
        if (hashes == null) {
            hashes = new LongArrayList(stacks.size());
            for (EntryStack<?> stack : stacks) {
                hashes.add(EntryStacks.hashExact(stack));
            }
        }
        
        removeFilteringFor(hashes);
    }
    
    private void removeFilteringFor(LongList hashes) {
        for (DataPair value : filteringData.values()) {
            value.hidden().removeAll(hashes);
            value.shown().removeAll(hashes);
        }
        
        LongListIterator hashIterator = hashes.iterator();
        while (hashIterator.hasNext()) {
            long hash = hashIterator.nextLong();
            cached.remove(hash);
        }
    }
    
    @Override
    public List<EntryStack<?>> getList() {
        return listView;
    }
    
    private class InternalListView extends AbstractList<EntryStack<?>> {
        private long prevMod = -1;
        private List<EntryStack<?>> stacks;
        
        @Override
        public EntryStack<?> get(int index) {
            if (prevMod == mod) {
                return stacks.get(index);
            }
            
            if (index < list.size() / 5) {
                return Iterators.get(iterator(), index);
            }
            
            stacks = Lists.newArrayList(iterator());
            prevMod = mod;
            return stacks.get(index);
        }
        
        @Override
        public int size() {
            if (prevMod != mod) {
                stacks = Lists.newArrayList(iterator());
                prevMod = mod;
            }
            
            return stacks.size();
        }
        
        @Override
        public Iterator<EntryStack<?>> iterator() {
            if (prevMod == mod) {
                return stacks.iterator();
            }
            
            Iterator<HashedEntryStackWrapper> iterator = list.collectHashed().iterator();
            return new AbstractIterator<>() {
                @Nullable
                @Override
                protected EntryStack<?> computeNext() {
                    while (iterator.hasNext()) {
                        HashedEntryStackWrapper wrapper = iterator.next();
                        if (isFiltered(wrapper.unwrap(), wrapper.hashExact())) return wrapper.unwrap();
                    }
                    
                    return endOfData();
                }
            };
        }
    }
    
    @Override
    public boolean isFiltered(EntryStack<?> stack, long hashExact) {
        return !stack.isEmpty() && cached.getOrDefault(hashExact, true);
    }
    
    private record DataPair(LongSet hidden, LongSet shown) {
        private DataPair() {
            this(new LongOpenHashSet(), new LongOpenHashSet());
        }
    }
}
