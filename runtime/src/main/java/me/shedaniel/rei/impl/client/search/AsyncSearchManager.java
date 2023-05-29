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

package me.shedaniel.rei.impl.client.search;

import com.google.common.collect.Lists;
import dev.architectury.platform.Platform;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.util.ThreadCreator;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AsyncSearchManager {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-AsyncSearchManager").asService(Math.min(3, Runtime.getRuntime().availableProcessors()));
    private final Supplier<List<HashedEntryStackWrapper>> stacksProvider;
    private final Supplier<Predicate<HashedEntryStackWrapper>> additionalPredicateSupplier;
    private final UnaryOperator<HashedEntryStackWrapper> transformer;
    private volatile ExecutorTuple executor;
    private volatile Map.Entry<List<HashedEntryStackWrapper>, SearchFilter> last;
    public volatile SearchFilter filter;
    
    public AsyncSearchManager(Supplier<List<HashedEntryStackWrapper>> stacksProvider, Supplier<Predicate<HashedEntryStackWrapper>> additionalPredicateSupplier, UnaryOperator<HashedEntryStackWrapper> transformer) {
        this.stacksProvider = stacksProvider;
        this.additionalPredicateSupplier = additionalPredicateSupplier;
        this.transformer = transformer;
    }
    
    public void markDirty() {
        this.last = null;
    }
    
    private record ExecutorTuple(SearchFilter filter,
                                 CompletableFuture<Map.Entry<List<HashedEntryStackWrapper>, SearchFilter>> future) {
    }
    
    public void updateFilter(String filter) {
        if (this.filter == null || !this.filter.getFilter().equals(filter)) {
            if (this.executor != null) {
                this.executor.future().cancel(Platform.isFabric());
            }
            this.executor = null;
            this.filter = SearchProvider.getInstance().createFilter(filter);
        }
    }
    
    public boolean isDirty() {
        return this.last == null || this.last.getValue() != this.filter;
    }
    
    public Future<?> getAsync(BiConsumer<List<HashedEntryStackWrapper>, SearchFilter> consumer) {
        if (this.executor == null || this.executor.filter() != filter || isDirty()) {
            if (this.executor != null) {
                this.executor.future().cancel(Platform.isFabric());
            }
            this.executor = new ExecutorTuple(filter, get(EXECUTOR_SERVICE));
        }
        SearchFilter savedFilter = filter;
        return (this.executor = new ExecutorTuple(this.executor.filter(), this.executor.future().thenApplyAsync(result -> {
            if (savedFilter == filter) {
                consumer.accept(result.getKey(), result.getValue());
            }
            
            return result;
        }, EXECUTOR_SERVICE))).future();
    }
    
    public List<HashedEntryStackWrapper> getNow() {
        try {
            return get(Runnable::run).get().getKey();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException | CancellationException e) {
            return Lists.newArrayList();
        }
    }
    
    public CompletableFuture<Map.Entry<List<HashedEntryStackWrapper>, SearchFilter>> get(Executor executor) {
        if (isDirty()) {
            Map.Entry<List<HashedEntryStackWrapper>, SearchFilter> last;
            last = this.last;
            return get(this.filter, this.additionalPredicateSupplier.get(), this.transformer,
                    this.stacksProvider.get(), last, this, executor)
                    .thenApply(entry -> {
                        this.last = entry;
                        return entry;
                    });
        }
        
        return CompletableFuture.completedFuture(last);
    }
    
    public static CompletableFuture<Map.Entry<List<HashedEntryStackWrapper>, SearchFilter>> get(SearchFilter filter, Predicate<HashedEntryStackWrapper> additionalPredicate,
            UnaryOperator<HashedEntryStackWrapper> transformer, List<HashedEntryStackWrapper> stacks, Map.Entry<List<HashedEntryStackWrapper>, SearchFilter> last,
            AsyncSearchManager manager, Executor executor) {
        int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
        boolean shouldAsync = ConfigObject.getInstance().shouldAsyncSearch() && stacks.size() > searchPartitionSize * 4;
        
        if (!stacks.isEmpty()) {
            if (shouldAsync) {
                List<CompletableFuture<List<HashedEntryStackWrapper>>> futures = Lists.newArrayList();
                for (Iterable<HashedEntryStackWrapper> partitionStacks : CollectionUtils.partition(stacks, Math.max(searchPartitionSize, stacks.size() * 3 / Runtime.getRuntime().availableProcessors()))) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        List<HashedEntryStackWrapper> filtered = Lists.newArrayList();
                        for (HashedEntryStackWrapper stack : partitionStacks) {
                            if (stack != null && filter.test(stack.unwrap(), stack.hashExact()) && additionalPredicate.test(stack)) {
                                filtered.add(transformer.apply(stack));
                            }
                            if (manager.filter != filter) throw new CancellationException();
                        }
                        return filtered;
                    }, executor));
                }
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .orTimeout(90, TimeUnit.SECONDS)
                        .thenApplyAsync($ -> {
                            List<HashedEntryStackWrapper> list = new ArrayList<>();
                            
                            for (CompletableFuture<List<HashedEntryStackWrapper>> future : futures) {
                                List<HashedEntryStackWrapper> now = future.getNow(null);
                                if (now != null) list.addAll(now);
                            }
                            if (manager.filter != filter) throw new CancellationException();
                            
                            return list;
                        }, executor)
                        .thenApply(result -> {
                            return new AbstractMap.SimpleImmutableEntry<>(result, filter);
                        });
            } else {
                List<HashedEntryStackWrapper> list = new ArrayList<>();
                
                for (HashedEntryStackWrapper stack : stacks) {
                    if (filter.test(stack.unwrap(), stack.hashExact()) && additionalPredicate.test(stack)) {
                        list.add(transformer.apply(stack));
                    }
                    if (manager.filter != filter) throw new CancellationException();
                }
                
                return CompletableFuture.completedFuture(new AbstractMap.SimpleImmutableEntry<>(list, filter));
            }
        }
        
        return CompletableFuture.completedFuture(new AbstractMap.SimpleImmutableEntry<>(Lists.newArrayList(), filter));
    }
    
    public boolean matches(EntryStack<?> stack) {
        return filter.test(stack);
    }
}
