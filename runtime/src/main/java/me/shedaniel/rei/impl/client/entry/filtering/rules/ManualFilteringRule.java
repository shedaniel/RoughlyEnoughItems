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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.entry.filtering.AbstractFilteringRule;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringCache;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContext;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ManualFilteringRule extends AbstractFilteringRule<ManualFilteringRule> {
    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }
    
    @Override
    public ManualFilteringRule createFromTag(CompoundTag tag) {
        return new ManualFilteringRule();
    }
    
    @Override
    public Object prepareCache(boolean async) {
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
                }));
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
    public FilteringResult processFilteredStacks(FilteringContext context, FilteringCache cache, boolean async) {
        LongSet filteredStacks = (LongSet) cache.getCache(this);
        FilteringResult result = FilteringResult.create();
        processList(context.getShownStacks(), result, async, filteredStacks);
        processList(context.getUnsetStacks(), result, async, filteredStacks);
        return result;
    }
    
    private void processList(Collection<EntryStack<?>> stacks, FilteringResult result, boolean async, LongSet filteredStacks) {
        result.hide((async ? stacks.parallelStream() : stacks.stream()).filter(stack -> filteredStacks.contains(EntryStacks.hashExact(stack))).collect(Collectors.toList()));
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("rule.roughlyenoughitems.filtering.manual");
    }
    
    @Override
    public Component getSubtitle() {
        return Component.translatable("rule.roughlyenoughitems.filtering.manual.subtitle");
    }
    
    @Override
    public ManualFilteringRule createNew() {
        throw new UnsupportedOperationException();
    }
}
