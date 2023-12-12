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
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.Util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public class AsyncSearchManager {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-AsyncSearchManager").asService(Math.min(3, Runtime.getRuntime().availableProcessors()));
    private final Function<SearchFilter, List<? extends HashedEntryStackWrapper>> stacksProvider;
    private final Supplier<Predicate<HashedEntryStackWrapper>> additionalPredicateSupplier;
    private final UnaryOperator<HashedEntryStackWrapper> transformer;
    private volatile Map.Entry<List<HashedEntryStackWrapper>, SearchFilter> last;
    public volatile ExecutorTuple executor;
    public volatile SearchFilter filter;
    
    public AsyncSearchManager(Function<SearchFilter, List<? extends HashedEntryStackWrapper>> stacksProvider, Supplier<Predicate<HashedEntryStackWrapper>> additionalPredicateSupplier, UnaryOperator<HashedEntryStackWrapper> transformer) {
        this.stacksProvider = stacksProvider;
        this.additionalPredicateSupplier = additionalPredicateSupplier;
        this.transformer = transformer;
    }
    
    public void markDirty() {
        this.last = null;
    }
    
    public record ExecutorTuple(SearchFilter filter,
                                 CompletableFuture<Map.Entry<List<HashedEntryStackWrapper>, SearchFilter>> future,
                                 Steps steps) {
    }
    
    public static class Steps {
        public long startTime = 0;
        public AtomicInteger partitionsDone = new AtomicInteger(0);
        public int totalPartitions = 0;
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
            Steps steps = new Steps();
            this.executor = new ExecutorTuple(filter, get(EXECUTOR_SERVICE, steps), steps);
        }
        SearchFilter savedFilter = filter;
        return (this.executor = new ExecutorTuple(this.executor.filter(), this.executor.future().thenApplyAsync(result -> {
            if (savedFilter == filter) {
                consumer.accept(result.getKey(), result.getValue());
            }
            
            return result;
        }, EXECUTOR_SERVICE), executor.steps)).future();
    }
    
    public List<HashedEntryStackWrapper> getNow() {
        try {
            return get(Runnable::run, new Steps()).get().getKey();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException | CancellationException e) {
            return Lists.newArrayList();
        }
    }
    
    public CompletableFuture<Map.Entry<List<HashedEntryStackWrapper>, SearchFilter>> get(Executor executor, Steps steps) {
        if (isDirty()) {
            Map.Entry<List<HashedEntryStackWrapper>, SearchFilter> last;
            last = this.last;
            return get(this.filter, this.additionalPredicateSupplier.get(), this.transformer,
                    this.stacksProvider.apply(filter), last, this, executor, steps)
                    .thenApply(entry -> {
                        this.last = entry;
                        return entry;
                    });
        }
        
        return CompletableFuture.completedFuture(last);
    }
    
    public static CompletableFuture<Map.Entry<List<HashedEntryStackWrapper>, SearchFilter>> get(SearchFilter filter, Predicate<HashedEntryStackWrapper> additionalPredicate,
            UnaryOperator<HashedEntryStackWrapper> transformer, List<? extends HashedEntryStackWrapper> stacks, Map.Entry<List<HashedEntryStackWrapper>, SearchFilter> last,
            AsyncSearchManager manager, Executor executor, Steps steps) {
        int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
        boolean shouldAsync = ConfigObject.getInstance().shouldAsyncSearch() && stacks.size() > searchPartitionSize * 4;
        InternalLogger.getInstance().debug("Starting Search: \"" + filter.getFilter() + "\" with " + stacks.size() + " stacks, shouldAsync: " + shouldAsync + " on " + Thread.currentThread().getName());
        
        if (!stacks.isEmpty()) {
            if (shouldAsync) {
                List<CompletableFuture<List<HashedEntryStackWrapper>>> futures = Lists.newArrayList();
                int partitions = 0;
                for (Iterable<? extends HashedEntryStackWrapper> partitionStacks : CollectionUtils.partition(stacks, searchPartitionSize * 4)) {
                    final int finalPartitions = partitions;
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        List<HashedEntryStackWrapper> filtered = Lists.newArrayList();
                        if (manager.filter != filter) throw new CancellationException();
                        for (HashedEntryStackWrapper stack : partitionStacks) {
                            if (stack != null && test(filter, stack.unwrap(), stack.hashExact()) && additionalPredicate.test(stack)) {
                                filtered.add(transformer.apply(stack));
                            }
                            if (manager.filter != filter) throw new CancellationException();
                        }
                        steps.partitionsDone.incrementAndGet();
                        return filtered;
                    }, executor));
                    partitions++;
                }
                steps.startTime = Util.getEpochMillis();
                steps.totalPartitions = partitions;
                InternalLogger.getInstance().debug("Async Search: " + partitions + " partitions for \"" + filter.getFilter() + "\"");
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
                    if (test(filter, stack.unwrap(), stack.hashExact()) && additionalPredicate.test(stack)) {
                        list.add(transformer.apply(stack));
                    }
                    if (manager.filter != filter) throw new CancellationException();
                }
                
                return CompletableFuture.completedFuture(new AbstractMap.SimpleImmutableEntry<>(list, filter));
            }
        }
        
        return CompletableFuture.completedFuture(new AbstractMap.SimpleImmutableEntry<>(Lists.newArrayList(), filter));
    }
    
    private static boolean test(SearchFilter filter, EntryStack<?> stack, long hashExact) {
        try {
            return filter.test(stack, hashExact);
        } catch (Throwable throwable) {
            InternalLogger.getInstance().debug("Error while testing filter", throwable);
            return false;
        }
    }
    
    public boolean matches(EntryStack<?> stack) {
        return filter.test(stack);
    }
}
