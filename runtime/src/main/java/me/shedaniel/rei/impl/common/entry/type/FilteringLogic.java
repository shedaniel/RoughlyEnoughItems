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
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextType;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringResultImpl;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class FilteringLogic {
    private static final MutableLong LAST_WARNING = new MutableLong(-1);
    
    public static void warnFiltering() {
        if (LAST_WARNING.getValue() > 0 && System.currentTimeMillis() - LAST_WARNING.getValue() > 5000) {
            InternalLogger.getInstance().warn("Detected runtime EntryRegistry modification, this can be extremely dangerous, or be extremely inefficient!");
        }
        LAST_WARNING.setValue(System.currentTimeMillis());
    }
    
    public static List<FilteringRule<?>> getRules() {
        return ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
    }
    
    private static LinkedHashMap<FilteringRule<?>, Object> prepareCache(List<FilteringRule<?>> rules, boolean async, Collection<EntryStack<?>> entries) {
        LinkedHashMap<FilteringRule<?>, Object> cache = new LinkedHashMap<>();
        for (int i = rules.size() - 1; i >= 0; i--) {
            FilteringRule<?> rule = rules.get(i);
            cache.put(rule, rule.prepareCache(async));
        }
        return cache;
    }
    
    public static List<EntryStack<?>> filter(List<FilteringRule<?>> rules, boolean log, boolean async, List<EntryStack<?>> entries) {
        return (List<EntryStack<?>>) filter(rules, log, async, (Collection<EntryStack<?>>) entries);
    }
    
    public static Collection<EntryStack<?>> filter(List<FilteringRule<?>> rules, boolean log, boolean async, Collection<EntryStack<?>> entries) {
        Set<HashedEntryStackWrapper> hiddenStacks = hidden(rules, log, async, entries).get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            return entries;
        } else if (async) {
            return entries.parallelStream()
                    .filter(stack -> !hiddenStacks.contains(new HashedEntryStackWrapper(stack)))
                    .collect(Collectors.toList());
        } else {
            return CollectionUtils.filterToList(entries, stack -> !hiddenStacks.contains(new HashedEntryStackWrapper(stack)));
        }
    }
    
    public static Map<FilteringContextType, Set<HashedEntryStackWrapper>> hidden(List<FilteringRule<?>> rules, boolean log, boolean async, Collection<EntryStack<?>> entries) {
        async = entries.size() > 100 && async;
        FilteringContextImpl context = new FilteringContextImpl(async, entries);
        LinkedHashMap<FilteringRule<?>, Object> cache = prepareCache(rules, async, entries);
        filter0(log, context, cache, entries);
        
        return context.stacks;
    }
    
    private static void filter0(boolean log, FilteringContextImpl context, LinkedHashMap<FilteringRule<?>, Object> cache, Collection<EntryStack<?>> entries) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Map.Entry<FilteringRule<?>, Object> entry : cache.entrySet()) {
            stopwatch.reset().start();
            FilteringRule<?> rule = entry.getKey();
            Object cacheObject = entry.getValue();
            context.handleResult((FilteringResultImpl) ((FilteringRule<Object>) rule).processFilteredStacks(context,
                    () -> new FilteringResultImpl(new ArrayList<>(), new ArrayList<>()),
                    cache.get(rule), true));
            if (log) {
                InternalLogger.getInstance().debug("Refiltered rule [%s] in %s.", rule.getType().toString(), stopwatch.stop().toString());
            }
        }
    }
}
