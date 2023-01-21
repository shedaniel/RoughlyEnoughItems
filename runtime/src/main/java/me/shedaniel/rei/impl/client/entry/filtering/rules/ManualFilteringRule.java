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

package me.shedaniel.rei.impl.client.entry.filtering.rules;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.*;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.util.ThreadCreator;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ManualFilteringRule implements FilteringRule<LongSet> {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-ManualFiltering").asService();
    
    @Override
    public FilteringRuleType<? extends FilteringRule<LongSet>> getType() {
        return ManualFilteringRuleType.INSTANCE;
    }
    
    @Override
    public LongSet prepareCache(boolean async) {
        if (async) {
            LongSet all = new LongOpenHashSet();
            List<CompletableFuture<LongSet>> completableFutures = Lists.newArrayList();
            for (Iterable<EntryStackProvider<?>> partitionStacks : CollectionUtils.partition(ConfigObject.getInstance().getFilteredStackProviders(), 100)) {
                completableFutures.add(CompletableFuture.supplyAsync(() -> {
                    LongSet output = new LongOpenHashSet();
                    for (EntryStackProvider<?> provider : partitionStacks) {
                        if (provider != null && provider.isValid()) {
                            output.add(EntryStacks.hashExact(provider.provide()));
                        }
                    }
                    return output;
                }, EXECUTOR_SERVICE));
            }
            try {
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(5, TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            for (CompletableFuture<LongSet> future : completableFutures) {
                LongSet now = future.getNow(null);
                if (now != null) {
                    all.addAll(now);
                }
            }
            return all;
        } else {
            return ConfigObject.getInstance().getFilteredStackProviders().stream().filter(EntryStackProvider::isValid).map(provider -> EntryStacks.hashExact(provider.provide())).collect(Collectors.toCollection(LongOpenHashSet::new));
        }
    }
    
    @Override
    public FilteringResult processFilteredStacks(FilteringContext context, FilteringResultFactory resultFactory, LongSet cache, boolean async) {
        FilteringResult result = resultFactory.create();
        processList(context.getShownStacks(), result, async, cache);
        processList(context.getUnsetStacks(), result, async, cache);
        return result;
    }
    
    private void processList(Collection<EntryStack<?>> stacks, FilteringResult result, boolean async, LongSet filteredStacks) {
        result.hide((async ? stacks.parallelStream() : stacks.stream()).filter(stack -> filteredStacks.contains(EntryStacks.hashExact(stack))).collect(Collectors.toList()));
    }
}
