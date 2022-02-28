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

package me.shedaniel.rei.impl.client.search;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AsyncSearchManager {
    private final Supplier<List<EntryStack<?>>> stacksProvider;
    private final Supplier<Predicate<EntryStack<?>>> additionalPredicateSupplier;
    private final UnaryOperator<EntryStack<?>> transformer;
    private Predicate<EntryStack<?>> additionalPredicate;
    private SearchFilter filter;
    private boolean dirty = false;
    private boolean filterDirty = false;
    private CompletableFuture<List<EntryStack<?>>> future;
    private List<EntryStack<?>> last;
    
    public AsyncSearchManager(Supplier<List<EntryStack<?>>> stacksProvider, Supplier<Predicate<EntryStack<?>>> additionalPredicateSupplier, UnaryOperator<EntryStack<?>> transformer) {
        this.stacksProvider = stacksProvider;
        this.additionalPredicateSupplier = additionalPredicateSupplier;
        this.transformer = transformer;
    }
    
    public static AsyncSearchManager createDefault() {
        return new AsyncSearchManager(EntryRegistry.getInstance()::getPreFilteredList, () -> {
            boolean checkCraftable = ConfigManager.getInstance().isCraftableOnlyEnabled();
            LongSet workingItems = checkCraftable ? new LongOpenHashSet() : null;
            if (checkCraftable) {
                for (EntryStack<?> stack : Views.getInstance().findCraftableEntriesByMaterials()) {
                    workingItems.add(EntryStacks.hashExact(stack));
                }
            }
            return checkCraftable ? stack -> workingItems.contains(EntryStacks.hashExact(stack)) : stack -> true;
        }, EntryStack::normalize);
    }
    
    public void markDirty() {
        this.dirty = true;
    }
    
    public void markFilterDirty() {
        this.filterDirty = true;
    }
    
    public void updateFilter(String filter) {
        if (this.filter == null || !this.filter.getFilter().equals(filter)) {
            this.filter = SearchProvider.getInstance().createFilter(filter);
            markDirty();
            markFilterDirty();
        }
    }
    
    public boolean isDirty() {
        return last == null || dirty;
    }
    
    public boolean isFilterDirty() {
        return filterDirty;
    }
    
    public Future<Void> getAsync(Consumer<List<EntryStack<?>>> consumer) {
        if (future == null || future.isCancelled() || future.isDone() || future.isCompletedExceptionally()) {
            if (future != null) future.cancel(true);
            future = CompletableFuture.supplyAsync(this::get)
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
        }
        return future.thenAccept(consumer);
    }
    
    public List<EntryStack<?>> get() {
        if (isDirty()) {
            this.additionalPredicate = additionalPredicateSupplier.get();
            int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
            List<EntryStack<?>> stacks = stacksProvider.get();
            last = new ArrayList<>();
            
            if (!stacks.isEmpty()) {
                if (filterDirty) {
                    filter.prepareFilter(stacks);
                    filterDirty = false;
                }
                
                if (ConfigObject.getInstance().shouldAsyncSearch() && stacks.size() > searchPartitionSize * 4) {
                    List<CompletableFuture<List<EntryStack<?>>>> futures = Lists.newArrayList();
                    for (Iterable<EntryStack<?>> partitionStacks : CollectionUtils.partition(stacks, searchPartitionSize)) {
                        futures.add(CompletableFuture.supplyAsync(() -> {
                            List<EntryStack<?>> filtered = Lists.newArrayList();
                            for (EntryStack<?> stack : partitionStacks) {
                                if (stack != null && matches(stack) && additionalPredicate.test(stack)) {
                                    filtered.add(transformer.apply(stack));
                                }
                            }
                            return filtered;
                        }));
                    }
                    try {
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                    }
                    for (CompletableFuture<List<EntryStack<?>>> future : futures) {
                        List<EntryStack<?>> now = future.getNow(null);
                        if (now != null) last.addAll(now);
                    }
                } else {
                    for (EntryStack<?> stack : stacks) {
                        if (matches(stack) && additionalPredicate.test(stack)) {
                            last.add(transformer.apply(stack));
                        }
                    }
                }
            }
            
            dirty = false;
        }
        
        return last;
    }
    
    public boolean matches(EntryStack<?> stack) {
        return filter.test(stack);
    }
}
