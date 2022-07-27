package me.shedaniel.rei.impl.client.entry.filtering.rules;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringCache;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContext;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringResult;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.entry.filtering.*;
import net.minecraft.util.StringUtil;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class SearchFilteringRule extends AbstractFilteringRule {
    String filterStr;
    Supplier<SearchFilter> filter;
    boolean show;
    
    public SearchFilteringRule() {
    }
    
    public SearchFilteringRule(String filter, boolean show) {
        setFilter(filter);
        this.show = show;
    }
    
    public void setFilter(String filter) {
        this.filterStr = filter;
        this.filter = Suppliers.memoize(() -> StringUtil.isNullOrEmpty(filter) ? SearchFilter.matchAll() : SearchProvider.getInstance().createFilter(filter));
    }
    
    @Override
    public FilteringRuleType<?> getType() {
        return SearchFilteringRuleType.INSTANCE;
    }
    
    @Override
    public FilteringResult processFilteredStacks(FilteringContext context, FilteringCache cache, boolean async) {
        List<CompletableFuture<List<EntryStack<?>>>> completableFutures = Lists.newArrayList();
        processList(context.getUnsetStacks(), completableFutures);
        if (show) processList(context.getHiddenStacks(), completableFutures);
        else processList(context.getShownStacks(), completableFutures);
        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        FilteringResult result = FilteringResult.create();
        for (CompletableFuture<List<EntryStack<?>>> future : completableFutures) {
            List<EntryStack<?>> now = future.getNow(null);
            if (now != null) {
                if (show) {
                    result.show(now);
                } else {
                    result.hide(now);
                }
            }
        }
        return result;
    }
    
    private void processList(Collection<EntryStack<?>> stacks, List<CompletableFuture<List<EntryStack<?>>>> completableFutures) {
        for (Iterable<EntryStack<?>> partitionStacks : CollectionUtils.partition((List<EntryStack<?>>) stacks, 100)) {
            completableFutures.add(CompletableFuture.supplyAsync(() -> {
                List<EntryStack<?>> output = Lists.newArrayList();
                for (EntryStack<?> stack : partitionStacks) {
                    if (stack != null && filter.get().test(stack)) {
                        output.add(stack);
                    }
                }
                return output;
            }));
        }
    }
}
