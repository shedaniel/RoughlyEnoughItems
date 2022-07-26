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
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongList;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringCacheImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextType;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PreFilteredEntryList implements EntryRegistryListener {
    private final EntryRegistry registry;
    private final MutableLong lastRefilterWarning = new MutableLong(-1);
    private List<EntryStack<?>> preFilteredList = Lists.newCopyOnWriteArrayList();
    
    public PreFilteredEntryList(EntryRegistry registry) {
        this.registry = registry;
    }
    
    private static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>) target.negate();
    }
    
    @Override
    public void addEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack, long stackHashExact) {
        if (!registry.isReloading()) {
            Collection<EntryStack<?>> refilterNew = refilterNew(true, Collections.singletonList(stack));
            if (afterEntry != null) {
                int index = preFilteredList.lastIndexOf(afterEntry);
                if (index >= 0) {
                    preFilteredList.addAll(index, refilterNew);
                    queueSearchUpdate();
                    return;
                }
            }
            
            preFilteredList.addAll(refilterNew);
            queueSearchUpdate();
        }
    }
    
    @Override
    public void addEntriesAfter(@Nullable EntryStack<?> afterEntry, List<EntryStack<?>> stacks, @Nullable LongList hashes) {
        if (!registry.isReloading()) {
            Collection<EntryStack<?>> refilterNew = refilterNew(true, stacks);
            if (afterEntry != null) {
                int index = preFilteredList.lastIndexOf(afterEntry);
                if (index >= 0) {
                    preFilteredList.addAll(index, refilterNew);
                    queueSearchUpdate();
                    return;
                }
            }
            
            preFilteredList.addAll(refilterNew);
            queueSearchUpdate();
        }
    }
    
    @Override
    public void removeEntry(EntryStack<?> stack, long hashExact) {
        if (!registry.isReloading()) {
            preFilteredList.remove(stack);
            queueSearchUpdate();
        }
    }
    
    @Override
    public void removeEntries(List<EntryStack<?>> stacks, @Nullable LongList hashes) {
        if (!registry.isReloading()) {
            preFilteredList.removeAll(stacks);
            queueSearchUpdate();
        }
    }
    
    @Override
    public void removeEntriesIf(Predicate<EntryStack<?>> predicate) {
        if (!registry.isReloading()) {
            preFilteredList.removeIf(predicate);
            queueSearchUpdate();
        }
    }
    
    @Override
    public void onReFilter(List<EntryStack<?>> stacks) {
        ConfigObject config = ConfigObject.getInstance();
        if (config.getFilteredStackProviders() != null) {
            List<EntryStack<?>> normalizedFilteredStacks = CollectionUtils.map(config.getFilteredStackProviders(), EntryStackProvider::provide);
            normalizedFilteredStacks.removeIf(EntryStack::isEmpty);
            config.getFilteredStackProviders().clear();
            config.getFilteredStackProviders().addAll(CollectionUtils.map(normalizedFilteredStacks, EntryStackProvider::ofStack));
        }
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        FilteringContextImpl context = new FilteringContextImpl(stacks);
        FilteringCacheImpl cache = new FilteringCacheImpl();
        List<FilteringRule<?>> rules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
        Stopwatch innerStopwatch = Stopwatch.createStarted();
        for (int i = rules.size() - 1; i >= 0; i--) {
            innerStopwatch.reset().start();
            FilteringRule<?> rule = rules.get(i);
            cache.setCache(rule, rule.prepareCache(true));
            context.handleResult(rule.processFilteredStacks(context, cache, true));
            InternalLogger.getInstance().debug("Refiltered rule [%s] in %s.", FilteringRule.REGISTRY.inverse().get(rule).toString(), innerStopwatch.stop().toString());
        }
        
        Set<HashedEntryStackWrapper> hiddenStacks = context.stacks.get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            preFilteredList = Lists.newCopyOnWriteArrayList(stacks);
        } else {
            preFilteredList = stacks.parallelStream()
                    .map(HashedEntryStackWrapper::new)
                    .filter(not(hiddenStacks::contains))
                    .map(HashedEntryStackWrapper::unwrap)
                    .collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
        }
        
        InternalLogger.getInstance().debug("Refiltered %d entries with %d rules in %s.", stacks.size() - preFilteredList.size(), rules.size(), stopwatch.stop().toString());
    }
    
    public Collection<EntryStack<?>> refilterNew(boolean warn, Collection<EntryStack<?>> entries) {
        if (lastRefilterWarning != null && warn) {
            if (lastRefilterWarning.getValue() > 0 && System.currentTimeMillis() - lastRefilterWarning.getValue() > 5000) {
                InternalLogger.getInstance().warn("Detected runtime EntryRegistry modification, this can be extremely dangerous, or be extremely inefficient!");
            }
            lastRefilterWarning.setValue(System.currentTimeMillis());
        }
        
        FilteringContextImpl context = new FilteringContextImpl(entries);
        FilteringCacheImpl cache = new FilteringCacheImpl();
        List<FilteringRule<?>> rules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
        for (int i = rules.size() - 1; i >= 0; i--) {
            FilteringRule<?> rule = rules.get(i);
            cache.setCache(rule, rule.prepareCache(true));
            context.handleResult(rule.processFilteredStacks(context, cache, true));
        }
        
        Set<HashedEntryStackWrapper> hiddenStacks = context.stacks.get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            return entries;
        } else {
            return entries.parallelStream()
                    .map(HashedEntryStackWrapper::new)
                    .filter(not(hiddenStacks::contains))
                    .map(HashedEntryStackWrapper::unwrap)
                    .collect(Collectors.toList());
        }
    }
    
    private void queueSearchUpdate() {
        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadSearch);
    }
    
    public List<EntryStack<?>> getList() {
        return preFilteredList;
    }
}
