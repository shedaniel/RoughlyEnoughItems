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

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import me.shedaniel.rei.api.client.entry.filtering.*;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.util.ThreadCreator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Unit;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class SearchFilteringRule implements FilteringRule<Unit> {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-SearchFiltering").asService();
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
    public FilteringRuleType<? extends FilteringRule<Unit>> getType() {
        return SearchFilteringRuleType.INSTANCE;
    }
    
    @Override
    public FilteringResult processFilteredStacks(FilteringContext context, FilteringResultFactory resultFactory, Unit cache, boolean async) {
        List<CompletableFuture<List<EntryStack<?>>>> completableFutures = Lists.newArrayList();
        processList(context.getUnsetStacks(), completableFutures);
        if (show) processList(context.getHiddenStacks(), completableFutures);
        else processList(context.getShownStacks(), completableFutures);
        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(90, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        FilteringResult result = resultFactory.create();
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
        for (Iterable<EntryStack<?>> partitionStacks : (Iterable<List<EntryStack<?>>>) () -> Iterators.partition(stacks.iterator(), 100)) {
            completableFutures.add(CompletableFuture.supplyAsync(() -> {
                List<EntryStack<?>> output = Lists.newArrayList();
                for (EntryStack<?> stack : partitionStacks) {
                    if (stack != null && filter.get().test(stack)) {
                        output.add(stack);
                    }
                }
                return output;
            }, EXECUTOR_SERVICE));
        }
    }
}
