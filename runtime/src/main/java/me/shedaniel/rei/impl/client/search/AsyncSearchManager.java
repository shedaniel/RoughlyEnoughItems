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
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.InternalLogger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AsyncSearchManager {
    private static final ThreadGroup GROUP = new ThreadGroup("REI-SearchManager");
    private static final AtomicInteger THREAD_ID = new AtomicInteger(0);
    private final Supplier<List<EntryStack<?>>> stacksProvider;
    private final Supplier<Predicate<EntryStack<?>>> additionalPredicateSupplier;
    private final UnaryOperator<EntryStack<?>> transformer;
    private ExecutorTuple executor;
    private SearchFilter filter;
    private Map.Entry<List<EntryStack<?>>, SearchFilter> last;
    
    public AsyncSearchManager(Supplier<List<EntryStack<?>>> stacksProvider, Supplier<Predicate<EntryStack<?>>> additionalPredicateSupplier, UnaryOperator<EntryStack<?>> transformer) {
        this.stacksProvider = stacksProvider;
        this.additionalPredicateSupplier = additionalPredicateSupplier;
        this.transformer = transformer;
    }
    
    private static Thread createThread(Runnable task) {
        Thread thread = new Thread(GROUP, task, "REI-SearchManager-" + THREAD_ID.getAndIncrement());
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(($, exception) -> {
            if (!(exception instanceof InterruptedException) && !(exception instanceof CancellationException) && !(exception instanceof ThreadDeath)) {
                InternalLogger.getInstance().throwException(exception);
            }
        });
        return thread;
    }
    
    public void markDirty() {
        synchronized (AsyncSearchManager.this) {
            this.last = null;
        }
    }
    
    private record ExecutorTuple(List<Thread> threads, SearchFilter filter, CompletableFuture<Map.Entry<List<EntryStack<?>>, SearchFilter>> future) {
    }
    
    public void updateFilter(String filter) {
        if (this.filter == null || !this.filter.getFilter().equals(filter)) {
            if (this.executor != null) {
                for (Thread thread : this.executor.threads()) {
                    try {
                        thread.stop();
                    } catch (ThreadDeath ignored) {}
                }
            }
            this.executor = null;
            this.filter = SearchProvider.getInstance().createFilter(filter);
        }
    }
    
    public boolean isDirty() {
        synchronized (AsyncSearchManager.this) {
            return this.last == null || this.last.getValue() != this.filter;
        }
    }
    
    public Future<?> getAsync(BiConsumer<List<EntryStack<?>>, SearchFilter> consumer) {
        if (executor == null || executor.filter() != filter) {
            if (executor != null) {
                for (Thread thread : this.executor.threads()) {
                    try {
                        thread.stop();
                    } catch (ThreadDeath ignored) {}
                }
                executor = null;
            }
            List<Thread> threads = new ArrayList<>();
            executor = new ExecutorTuple(threads, filter, get(task -> {
                Thread thread = createThread(task);
                threads.add(thread);
                thread.start();
            }));
        }
        SearchFilter savedFilter = filter;
        ExecutorTuple tuple = executor;
        return (executor = new ExecutorTuple(tuple.threads(), executor.filter(), executor.future().thenApplyAsync(result -> {
            if (savedFilter == filter) {
                consumer.accept(result.getKey(), result.getValue());
            }
            
            return result;
        }, task -> {
            Thread thread = createThread(task);
            tuple.threads().add(thread);
            thread.start();
        }))).future();
    }
    
    public List<EntryStack<?>> getNow() {
        try {
            return get(Runnable::run).get().getKey();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException | CancellationException e) {
            return Lists.newArrayList();
        }
    }
    
    public CompletableFuture<Map.Entry<List<EntryStack<?>>, SearchFilter>> get(Executor executor) {
        if (isDirty()) {
            Map.Entry<List<EntryStack<?>>, SearchFilter> last;
            synchronized (AsyncSearchManager.this) {
                last = this.last;
            }
            return get(this.filter, this.additionalPredicateSupplier.get(), this.transformer,
                    this.stacksProvider.get(), last, this, executor)
                    .thenApply(entry -> {
                        synchronized (AsyncSearchManager.this) {
                            this.last = entry;
                        }
                        return entry;
                    });
        }
        
        return CompletableFuture.completedFuture(last);
    }
    
    public static CompletableFuture<Map.Entry<List<EntryStack<?>>, SearchFilter>> get(SearchFilter filter, Predicate<EntryStack<?>> additionalPredicate,
            UnaryOperator<EntryStack<?>> transformer, List<EntryStack<?>> stacks, Map.Entry<List<EntryStack<?>>, SearchFilter> last,
            AsyncSearchManager manager, Executor executor) {
        int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
        
        if (!stacks.isEmpty()) {
            CompletableFuture<Void> preparationFuture = CompletableFuture.completedFuture(null);
            
            if (last == null || last.getValue() != filter) {
                preparationFuture = CompletableFuture.runAsync(() -> {
                    if (manager.filter == filter) {
                        filter.prepareFilter(stacks);
                    } else {
                        throw new CancellationException();
                    }
                }, executor);
            }
            
            if (ConfigObject.getInstance().shouldAsyncSearch() && stacks.size() > searchPartitionSize * 4) {
                List<CompletableFuture<List<EntryStack<?>>>> futures = Lists.newArrayList();
                for (Iterable<EntryStack<?>> partitionStacks : CollectionUtils.partition(stacks, searchPartitionSize)) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        List<EntryStack<?>> filtered = Lists.newArrayList();
                        for (EntryStack<?> stack : partitionStacks) {
                            if (stack != null && filter.test(stack) && additionalPredicate.test(stack)) {
                                filtered.add(transformer.apply(stack));
                            }
                            if (manager.filter != filter) throw new CancellationException();
                        }
                        return filtered;
                    }));
                }
                return preparationFuture.thenCompose($ -> CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .orTimeout(30, TimeUnit.SECONDS))
                        .thenApplyAsync($ -> {
                            List<EntryStack<?>> list = new ArrayList<>();
                            
                            if (manager.filter == filter) {
                                for (CompletableFuture<List<EntryStack<?>>> future : futures) {
                                    List<EntryStack<?>> now = future.getNow(null);
                                    if (now != null) list.addAll(now);
                                }
                            } else {
                                throw new CancellationException();
                            }
                            
                            return list;
                        }, executor)
                        .thenApply(result -> {
                            return new AbstractMap.SimpleImmutableEntry<>(result, filter);
                        });
            } else {
                return preparationFuture.thenApplyAsync($ -> {
                            List<EntryStack<?>> list = new ArrayList<>();
                            
                            for (EntryStack<?> stack : stacks) {
                                if (filter.test(stack) && additionalPredicate.test(stack)) {
                                    list.add(transformer.apply(stack));
                                }
                                if (manager.filter != filter) throw new CancellationException();
                            }
                            
                            return list;
                        }, executor)
                        .thenApply(result -> {
                            return new AbstractMap.SimpleImmutableEntry<>(result, filter);
                        });
                
            }
        }
        
        return CompletableFuture.completedFuture(new AbstractMap.SimpleImmutableEntry<>(Lists.newArrayList(), filter));
    }
    
    public boolean matches(EntryStack<?> stack) {
        return filter.test(stack);
    }
}
