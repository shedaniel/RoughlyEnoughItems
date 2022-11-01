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

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.entry.filtering.*;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.util.ThreadCreator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public enum BasicFilteringRuleImpl implements BasicFilteringRule<Pair<LongSet, LongSet>> {
    INSTANCE;
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-BasicFiltering").asService();
    private final List<EntryStack<?>> hidden = new ArrayList<>(), shown = new ArrayList<>();
    
    @Override
    public FilteringRuleType<? extends FilteringRule<Pair<LongSet, LongSet>>> getType() {
        return BasicFilteringRuleType.INSTANCE;
    }
    
    @Override
    public Pair<LongSet, LongSet> prepareCache(boolean async) {
        return new Pair<>(prepareCacheFor(hidden, async), prepareCacheFor(shown, async));
    }
    
    @NotNull
    private static LongSet prepareCacheFor(List<EntryStack<?>> stacks, boolean async) {
        if (async) {
            LongSet all = new LongOpenHashSet();
            List<CompletableFuture<LongSet>> completableFutures = Lists.newArrayList();
            for (Iterable<EntryStack<?>> partitionStacks : CollectionUtils.partition(stacks, 100)) {
                completableFutures.add(CompletableFuture.supplyAsync(() -> {
                    LongSet output = new LongOpenHashSet();
                    for (EntryStack<?> stack : partitionStacks) {
                        if (stack != null && !stack.isEmpty()) {
                            output.add(EntryStacks.hashExact(stack));
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
            return stacks.stream().map(EntryStacks::hashExact).collect(Collectors.toCollection(LongOpenHashSet::new));
        }
    }
    
    @Override
    public FilteringResult processFilteredStacks(FilteringContext context, FilteringResultFactory resultFactory, Pair<LongSet, LongSet> cache, boolean async) {
        FilteringResult result = resultFactory.create();
        hideList(context.getShownStacks(), result, async, cache.getFirst());
        hideList(context.getUnsetStacks(), result, async, cache.getFirst());
        showList(context.getHiddenStacks(), result, async, cache.getSecond());
        showList(context.getUnsetStacks(), result, async, cache.getSecond());
        return result;
    }
    
    private void hideList(Collection<EntryStack<?>> stacks, FilteringResult result, boolean async, LongSet filteredStacks) {
        result.hide((async ? stacks.parallelStream() : stacks.stream()).filter(stack -> filteredStacks.contains(EntryStacks.hashExact(stack))).collect(Collectors.toList()));
    }
    
    private void showList(Collection<EntryStack<?>> stacks, FilteringResult result, boolean async, LongSet filteredStacks) {
        result.show((async ? stacks.parallelStream() : stacks.stream()).filter(stack -> filteredStacks.contains(EntryStacks.hashExact(stack))).collect(Collectors.toList()));
    }
    
    @Override
    public FilteringResult hide(EntryStack<?> stack) {
        hidden.add(stack);
        shown.remove(stack);
        return this;
    }
    
    @Override
    public FilteringResult hide(Collection<? extends EntryStack<?>> stacks) {
        hidden.addAll(stacks);
        shown.removeAll(stacks);
        return this;
    }
    
    @Override
    public FilteringResult show(EntryStack<?> stack) {
        shown.add(stack);
        hidden.remove(stack);
        return this;
    }
    
    @Override
    public FilteringResult show(Collection<? extends EntryStack<?>> stacks) {
        shown.addAll(stacks);
        hidden.removeAll(stacks);
        return this;
    }
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void startReload() {
        hidden.clear();
        shown.clear();
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerBasicEntryFiltering(this);
    }
}
