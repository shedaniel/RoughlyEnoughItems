/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.common.entry.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextType;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class EntryRegistryImpl implements EntryRegistry {
    private final List<EntryStack<?>> preFilteredList = Lists.newCopyOnWriteArrayList();
    private final List<EntryStack<?>> entries = Lists.newCopyOnWriteArrayList();
    @Nullable
    private List<HashedEntryStackWrapper> reloadingRegistry;
    private boolean reloading;
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerEntries(this);
    }
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void startReload() {
        entries.clear();
        if (reloadingRegistry != null) {
            reloadingRegistry.clear();
        }
        reloadingRegistry = Lists.newArrayListWithCapacity(Registry.ITEM.keySet().size() + 100);
        preFilteredList.clear();
        reloading = true;
    }
    
    @Override
    public void endReload() {
        reloading = false;
        preFilteredList.clear();
        reloadingRegistry.removeIf(HashedEntryStackWrapper::isEmpty);
        entries.clear();
        entries.addAll(CollectionUtils.map(reloadingRegistry, HashedEntryStackWrapper::unwrap));
        reloadingRegistry = null;
        refilter();
    }
    
    @Override
    public int size() {
        return reloading ? reloadingRegistry.size() : entries.size();
    }
    
    @Override
    public Stream<EntryStack<?>> getEntryStacks() {
        return reloading ? reloadingRegistry.stream().map(HashedEntryStackWrapper::unwrap) : entries.stream();
    }
    
    @Override
    public List<EntryStack<?>> getPreFilteredList() {
        return Collections.unmodifiableList(preFilteredList);
    }
    
    @Override
    public void refilter() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        FilteringContextImpl context = new FilteringContextImpl(entries);
        List<FilteringRule<?>> rules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
        Stopwatch innerStopwatch = Stopwatch.createStarted();
        for (int i = rules.size() - 1; i >= 0; i--) {
            innerStopwatch.reset().start();
            FilteringRule<?> rule = rules.get(i);
            context.handleResult(rule.processFilteredStacks(context));
            RoughlyEnoughItemsCore.LOGGER.debug("Refiltered rule [%s] in %s.", FilteringRule.REGISTRY.getKey(rule).toString(), innerStopwatch.stop().toString());
        }
        
        Set<HashedEntryStackWrapper> hiddenStacks = context.stacks.get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            preFilteredList.clear();
            preFilteredList.addAll(entries);
        } else {
            preFilteredList.clear();
            preFilteredList.addAll(entries.parallelStream()
                    .map(HashedEntryStackWrapper::new)
                    .filter(not(hiddenStacks::contains))
                    .map(HashedEntryStackWrapper::unwrap)
                    .collect(Collectors.toList()));
        }
        
        RoughlyEnoughItemsCore.LOGGER.debug("Refiltered %d entries with %d rules in %s.", entries.size() - preFilteredList.size(), rules.size(), stopwatch.stop().toString());
    }
    
    private static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>) target.negate();
    }
    
    private static final Comparator<ItemStack> STACK_COMPARATOR = (a, b) -> ItemStack.matches(a, b) ? 0 : 1;
    
    @Override
    public List<ItemStack> appendStacksForItem(Item item) {
        NonNullList<ItemStack> list = NonNullList.create();
        CreativeModeTab category = item.getItemCategory();
        item.fillItemCategory(MoreObjects.firstNonNull(category, CreativeModeTab.TAB_SEARCH), list);
        if (list.isEmpty()) {
            return Collections.singletonList(item.getDefaultInstance());
        }
        list.sort(STACK_COMPARATOR);
        return list;
    }
    
    @Override
    public void addEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack) {
        if (reloading) {
            int index = afterEntry != null ? reloadingRegistry.lastIndexOf(new HashedEntryStackWrapper(afterEntry)) : -1;
            if (index >= 0) {
                reloadingRegistry.add(index, new HashedEntryStackWrapper(stack));
            } else reloadingRegistry.add(new HashedEntryStackWrapper(stack));
        } else {
            if (afterEntry != null) {
                int index = entries.lastIndexOf(afterEntry);
                entries.add(index, stack);
            } else entries.add(stack);
        }
    }
    
    @Override
    public void addEntriesAfter(@Nullable EntryStack<?> afterEntry, Collection<? extends EntryStack<?>> stacks) {
        if (reloading) {
            int index = afterEntry != null ? reloadingRegistry.lastIndexOf(new HashedEntryStackWrapper(afterEntry)) : -1;
            if (index >= 0) {
                reloadingRegistry.addAll(index, CollectionUtils.mapParallel(stacks, HashedEntryStackWrapper::new));
            } else reloadingRegistry.addAll(CollectionUtils.mapParallel(stacks, HashedEntryStackWrapper::new));
        } else {
            if (afterEntry != null) {
                int index = entries.lastIndexOf(afterEntry);
                entries.addAll(index, stacks);
            } else entries.addAll(stacks);
        }
    }
    
    @Override
    public boolean alreadyContain(EntryStack<?> stack) {
        if (reloading) {
            return reloadingRegistry.parallelStream().anyMatch(s -> EntryStacks.equalsExact(s.unwrap(), stack));
        }
        return entries.parallelStream().anyMatch(s -> EntryStacks.equalsExact(s, stack));
    }
    
    @Override
    public boolean removeEntry(EntryStack<?> stack) {
        if (reloading) {
            return reloadingRegistry.remove(new HashedEntryStackWrapper(stack));
        } else {
            return entries.remove(stack);
        }
    }
    
    @Override
    public boolean removeEntryIf(Predicate<? extends EntryStack<?>> predicate) {
        if (reloading) {
            return reloadingRegistry.removeIf(wrapper -> ((Predicate<EntryStack<?>>) predicate).test(wrapper.unwrap()));
        } else {
            return entries.removeIf((Predicate<EntryStack<?>>) predicate);
        }
    }
    
    @Override
    public boolean removeEntryExactHashIf(LongPredicate predicate) {
        if (reloading) {
            return reloadingRegistry.removeIf(wrapper -> predicate.test(wrapper.hashExact()));
        } else {
            return entries.removeIf(stack -> predicate.test(EntryStacks.hashExact(stack)));
        }
    }
    
    @Override
    public boolean removeEntryFuzzyHashIf(LongPredicate predicate) {
        if (reloading) {
            return reloadingRegistry.removeIf(wrapper -> predicate.test(EntryStacks.hashFuzzy(wrapper.unwrap())));
        } else {
            return entries.removeIf(stack -> predicate.test(EntryStacks.hashFuzzy(stack)));
        }
    }
}
