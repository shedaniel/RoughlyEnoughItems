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
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.entries.FilteringEntry;
import me.shedaniel.rei.impl.client.config.entries.FilteringRuleOptionsScreen;
import me.shedaniel.rei.impl.client.entry.filtering.AbstractFilteringRule;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringCache;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContext;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SearchFilteringRule extends AbstractFilteringRule<SearchFilteringRule> {
    private SearchFilter filter;
    private boolean show;
    
    public SearchFilteringRule() {
    }
    
    public SearchFilteringRule(SearchFilter filter, boolean show) {
        this.filter = filter;
        this.show = show;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("filter", filter.getFilter());
        tag.putBoolean("show", show);
        return tag;
    }
    
    @Override
    public SearchFilteringRule createFromTag(CompoundTag tag) {
        String filter = tag.getString("filter");
        boolean show = tag.getBoolean("show");
        return new SearchFilteringRule(SearchProvider.getInstance().createFilter(filter), show);
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
    
    @Override
    public SearchFilteringRule createNew() {
        return new SearchFilteringRule(SearchFilter.matchAll(), true);
    }
    
    private void processList(Collection<EntryStack<?>> stacks, List<CompletableFuture<List<EntryStack<?>>>> completableFutures) {
        for (Iterable<EntryStack<?>> partitionStacks : CollectionUtils.partition((List<EntryStack<?>>) stacks, 100)) {
            completableFutures.add(CompletableFuture.supplyAsync(() -> {
                List<EntryStack<?>> output = Lists.newArrayList();
                for (EntryStack<?> stack : partitionStacks) {
                    if (stack != null && filter.test(stack)) {
                        output.add(stack);
                    }
                }
                return output;
            }));
        }
    }
    
    @Override
    public Component getTitle() {
        return new TranslatableComponent("rule.roughlyenoughitems.filtering.search");
    }
    
    @Override
    public Component getSubtitle() {
        return new TranslatableComponent("rule.roughlyenoughitems.filtering.search.subtitle");
    }
    
    @Override
    public Optional<BiFunction<FilteringEntry, Screen, Screen>> createEntryScreen() {
        return Optional.of((entry, screen) -> new FilteringRuleOptionsScreen<SearchFilteringRule>(entry, this, screen) {
            TextFieldRuleEntry entry = null;
            BooleanRuleEntry show = null;
            
            @Override
            public void addEntries(Consumer<RuleEntry> entryConsumer) {
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, new TranslatableComponent("rule.roughlyenoughitems.filtering.search.filter").withStyle(ChatFormatting.GRAY));
                entryConsumer.accept(entry = new TextFieldRuleEntry(width - 36, rule, widget -> {
                    widget.setMaxLength(9999);
                    if (entry != null) widget.setValue(entry.getWidget().getValue());
                    else widget.setValue(rule.filter.getFilter());
                }));
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, new TranslatableComponent("rule.roughlyenoughitems.filtering.search.show").withStyle(ChatFormatting.GRAY));
                entryConsumer.accept(show = new BooleanRuleEntry(width - 36, show == null ? rule.show : show.getBoolean(), rule, bool -> {
                    return new TranslatableComponent("rule.roughlyenoughitems.filtering.search.show." + bool);
                }));
            }
            
            @Override
            public void save() {
                rule.filter = SearchProvider.getInstance().createFilter(entry.getWidget().getValue());
                rule.show = show.getBoolean();
            }
        });
    }
}
