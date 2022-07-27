package me.shedaniel.rei.impl.client.entry.filtering.rules;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringCache;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContext;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringResult;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.entry.filtering.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ManualFilteringRule extends AbstractFilteringRule {
    @Override
    public FilteringRuleType<?> getType() {
        return ManualFilteringRuleType.INSTANCE;
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
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
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
}
