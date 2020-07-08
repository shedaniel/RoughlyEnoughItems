/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl.filtering.rules;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.config.entry.FilteringEntry;
import me.shedaniel.rei.gui.config.entry.FilteringRuleOptionsScreen;
import me.shedaniel.rei.impl.SearchArgument;
import me.shedaniel.rei.impl.filtering.AbstractFilteringRule;
import me.shedaniel.rei.impl.filtering.FilteringContext;
import me.shedaniel.rei.impl.filtering.FilteringResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SearchFilteringRule extends AbstractFilteringRule<SearchFilteringRule> {
    private String filter;
    private List<SearchArgument.SearchArguments> arguments;
    private boolean show;
    
    public SearchFilteringRule() {
    }
    
    public SearchFilteringRule(String filter, List<SearchArgument.SearchArguments> arguments, boolean show) {
        this.filter = filter;
        this.arguments = arguments;
        this.show = show;
    }
    
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putString("filter", filter);
        tag.putBoolean("show", show);
        return tag;
    }
    
    @Override
    public SearchFilteringRule createFromTag(CompoundTag tag) {
        String filter = tag.getString("filter");
        boolean show = tag.getBoolean("show");
        return new SearchFilteringRule(filter, SearchArgument.processSearchTerm(filter), show);
    }
    
    @NotNull
    @Override
    public FilteringResult processFilteredStacks(@NotNull FilteringContext context) {
        List<CompletableFuture<List<EntryStack>>> completableFutures = Lists.newArrayList();
        processList(context.getUnsetStacks(), completableFutures);
        if (show) processList(context.getHiddenStacks(), completableFutures);
        else processList(context.getShownStacks(), completableFutures);
        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        FilteringResult result = FilteringResult.create();
        for (CompletableFuture<List<EntryStack>> future : completableFutures) {
            List<EntryStack> now = future.getNow(null);
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
        return new SearchFilteringRule("", Collections.singletonList(SearchArgument.SearchArguments.ALWAYS), true);
    }
    
    private void processList(Set<EntryStack> stacks, List<CompletableFuture<List<EntryStack>>> completableFutures) {
        int size = 100;
        List<EntryStack> stacks1 = Lists.newArrayList(stacks);
        Iterator<EntryStack> iterator = stacks1.iterator();
        for (int i = 0; i < stacks1.size(); i += size) {
            int[] start = {i};
            completableFutures.add(CompletableFuture.supplyAsync(() -> {
                int end = Math.min(stacks1.size(), start[0] + size);
                List<EntryStack> output = Lists.newArrayList();
                for (; start[0] < end; start[0]++) {
                    EntryStack stack = stacks1.get(start[0]);
                    boolean shown = SearchArgument.canSearchTermsBeAppliedTo(stack, arguments);
                    if (shown) {
                        output.add(stack);
                    }
                }
                return output;
            }));
        }
    }
    
    @Override
    public Text getTitle() {
        return new TranslatableText("rule.roughlyenoughitems.filtering.search");
    }
    
    @Override
    public Text getSubtitle() {
        return new TranslatableText("rule.roughlyenoughitems.filtering.search.subtitle");
    }
    
    @Override
    public Optional<BiFunction<FilteringEntry, Screen, Screen>> createEntryScreen() {
        return Optional.of((entry, screen) -> new FilteringRuleOptionsScreen<SearchFilteringRule>(entry, this, screen) {
            TextFieldRuleEntry entry = null;
            BooleanRuleEntry show = null;
            
            @Override
            public void addEntries(Consumer<RuleEntry> entryConsumer) {
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, new TranslatableText("rule.roughlyenoughitems.filtering.search.filter").formatted(Formatting.GRAY));
                entryConsumer.accept(entry = new TextFieldRuleEntry(width - 36, rule, widget -> {
                    widget.setMaxLength(9999);
                    if (entry != null) widget.setText(entry.getWidget().getText());
                    else widget.setText(rule.filter);
                }));
                addEmpty(entryConsumer, 10);
                addText(entryConsumer, new TranslatableText("rule.roughlyenoughitems.filtering.search.show").formatted(Formatting.GRAY));
                entryConsumer.accept(show = new BooleanRuleEntry(width - 36, show == null ? rule.show : show.getBoolean(), rule, bool -> {
                    return new TranslatableText("rule.roughlyenoughitems.filtering.search.show." + bool);
                }));
            }
            
            @Override
            public void save() {
                rule.filter = entry.getWidget().getText();
                rule.arguments = SearchArgument.processSearchTerm(rule.filter);
                rule.show = show.getBoolean();
            }
        });
    }
}
