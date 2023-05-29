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

package me.shedaniel.rei.impl.client.search.argument;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.util.ThreadCreator;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.Util;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class ArgumentCache {
    public static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-Cache").asService(2);
    private final Short2ObjectMap<Long2ObjectMap<Object>> cache = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());
    public Long prepareStart = null;
    public List<HashedEntryStackWrapper> prepareStacks = null;
    public CurrentStep currentStep = null;
    
    public Long2ObjectMap<Object> getSearchCache(ArgumentType<?, ?> argumentType) {
        short argumentIndex = (short) argumentType.getIndex();
        Long2ObjectMap<Object> map = cache.get(argumentIndex);
        if (map == null) {
            cache.put(argumentIndex, map = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>()));
        }
        return map;
    }
    
    public void prepareFilter(Collection<HashedEntryStackWrapper> stacks, Collection<ArgumentType<?, ?>> argumentTypes) {
        this.prepareFilter(stacks, argumentTypes, null);
    }
    
    public void prepareFilter(Collection<HashedEntryStackWrapper> stacks, Collection<ArgumentType<?, ?>> argumentTypes, @Nullable Executor executor) {
        if (currentStep != null) return;
        try {
            prepareStart = Util.getEpochMillis();
            List<Long2ObjectMap<Object>> caches = CollectionUtils.map(argumentTypes, this::getSearchCache);
            prepareStacks = CollectionUtils.filterToList(stacks, stack -> {
                for (Long2ObjectMap<Object> cache : caches) {
                    if (!cache.containsKey(stack.hashExact())) {
                        return true;
                    }
                }
                
                return false;
            });
            if (prepareStacks.isEmpty()) {
                return;
            }
            InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Preparing " + (prepareStacks.size() * argumentTypes.size()) + " stacks for search arguments");
            currentStep = new CurrentStep(0, argumentTypes.size());
            int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
            boolean async = ConfigObject.getInstance().shouldAsyncSearch() && prepareStacks.size() > searchPartitionSize * 4;
            this.cache(argumentTypes, async ? executor : Runnable::run);
        } finally {
            prepareStart = null;
            prepareStacks = null;
            currentStep = null;
        }
    }
    
    private void cache(Collection<ArgumentType<?, ?>> argumentTypes, @Nullable Executor executor) {
        int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
        List<CompletableFuture<Long2ObjectMap<Object>>> futures = Lists.newArrayList();
        int[] sum = {0};
        
        for (ArgumentType<?, ?> argumentType : argumentTypes) {
            Long2ObjectMap<Object> cacheMap = getSearchCache(argumentType);
            CurrentStep.Step currentStage = currentStep.steps[currentStep.step] = new CurrentStep.Step(0, prepareStacks.size());
            currentStep.step++;
            
            for (Collection<HashedEntryStackWrapper> partitionStacks : CollectionUtils.partition(prepareStacks, searchPartitionSize)) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                            return cacheStacks(argumentType, cacheMap, partitionStacks);
                        }, Objects.requireNonNullElse(executor, EXECUTOR_SERVICE))
                        .whenComplete((map, throwable) -> {
                            if (map != null) {
                                currentStage.stacks += map.size();
                                cacheMap.putAll(map);
                                sum[0] += map.size();
                            }
                        }));
            }
        }
        
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(90, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException ignore) {
        } finally {
            InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Prepared " + sum[0] + " / " + (prepareStacks.size() * argumentTypes.size()) + " stacks for search arguments in " + (Util.getEpochMillis() - prepareStart) + "ms");
        }
    }
    
    private static Long2ObjectMap<Object> cacheStacks(ArgumentType<?, ?> argumentType, Long2ObjectMap<Object> cacheMap,
            Collection<HashedEntryStackWrapper> stacks) {
        Long2ObjectMap<Object> out = new Long2ObjectArrayMap<>(stacks.size() + 1);
        for (HashedEntryStackWrapper stack : stacks) {
            if (cacheMap.get(stack.hashExact()) == null) {
                Object data = argumentType.cacheData(stack.unwrap());
                
                if (data != null) {
                    out.put(stack.hashExact(), data);
                }
            }
        }
        return out;
    }
    
    public boolean isEmpty() {
        return cache.isEmpty();
    }
    
    public static class CurrentStep {
        public int step;
        public final int totalSteps;
        public final Step[] steps;
        
        public CurrentStep(int step, int totalSteps) {
            this.step = step;
            this.totalSteps = totalSteps;
            this.steps = new Step[totalSteps];
        }
        
        public static class Step {
            public int stacks;
            public int totalStacks;
            
            public Step(int stacks, int totalStacks) {
                this.stacks = stacks;
                this.totalStacks = totalStacks;
            }
        }
    }
}
