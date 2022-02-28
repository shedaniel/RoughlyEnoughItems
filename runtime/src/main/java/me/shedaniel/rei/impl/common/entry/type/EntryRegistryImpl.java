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

package me.shedaniel.rei.impl.common.entry.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringCacheImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextImpl;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringContextType;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableLong;
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
    public List<Runnable> refilterListener = Lists.newCopyOnWriteArrayList();
    private List<EntryStack<?>> preFilteredList = Lists.newCopyOnWriteArrayList();
    private List<EntryStack<?>> entries = Lists.newCopyOnWriteArrayList();
    private LongSet entriesHash = new LongOpenHashSet();
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
        refilterListener.clear();
        entries = Lists.newCopyOnWriteArrayList();
        entriesHash = new LongOpenHashSet();
        reloadingRegistry = Lists.newArrayListWithCapacity(Registry.ITEM.keySet().size() + 100);
        preFilteredList = Lists.newCopyOnWriteArrayList();
        reloading = true;
    }
    
    @Override
    public void endReload() {
        reloading = false;
        preFilteredList = Lists.newCopyOnWriteArrayList();
        entries = Lists.newCopyOnWriteArrayList(CollectionUtils.filterAndMap(reloadingRegistry, ((Predicate<HashedEntryStackWrapper>) HashedEntryStackWrapper::isEmpty).negate(), HashedEntryStackWrapper::unwrap));
        reloadingRegistry = null;
        refilter();
        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
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
        ConfigObject config = ConfigObject.getInstance();
        if (config.getFilteredStackProviders() != null) {
            List<EntryStack<?>> normalizedFilteredStacks = CollectionUtils.map(config.getFilteredStackProviders(), EntryStackProvider::provide);
            normalizedFilteredStacks.removeIf(EntryStack::isEmpty);
            config.getFilteredStackProviders().clear();
            config.getFilteredStackProviders().addAll(CollectionUtils.map(normalizedFilteredStacks, EntryStackProvider::ofStack));
        }
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        FilteringContextImpl context = new FilteringContextImpl(entries);
        FilteringCacheImpl cache = new FilteringCacheImpl();
        List<FilteringRule<?>> rules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
        Stopwatch innerStopwatch = Stopwatch.createStarted();
        for (int i = rules.size() - 1; i >= 0; i--) {
            innerStopwatch.reset().start();
            FilteringRule<?> rule = rules.get(i);
            cache.setCache(rule, rule.prepareCache(true));
            context.handleResult(rule.processFilteredStacks(context, cache, true));
            RoughlyEnoughItemsCore.LOGGER.debug("Refiltered rule [%s] in %s.", FilteringRule.REGISTRY.inverse().get(rule).toString(), innerStopwatch.stop().toString());
        }
        
        Set<HashedEntryStackWrapper> hiddenStacks = context.stacks.get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            preFilteredList = Lists.newCopyOnWriteArrayList(entries);
        } else {
            preFilteredList = entries.parallelStream()
                    .map(HashedEntryStackWrapper::new)
                    .filter(not(hiddenStacks::contains))
                    .map(HashedEntryStackWrapper::unwrap)
                    .collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
        }
        
        RoughlyEnoughItemsCore.LOGGER.debug("Refiltered %d entries with %d rules in %s.", entries.size() - preFilteredList.size(), rules.size(), stopwatch.stop().toString());
        
        for (Runnable runnable : refilterListener) {
            runnable.run();
        }
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
            HashedEntryStackWrapper wrapper = new HashedEntryStackWrapper(stack);
            if (this.entriesHash.add(wrapper.hashExact())) {
                if (index >= 0) {
                    reloadingRegistry.add(index, wrapper);
                } else reloadingRegistry.add(wrapper);
            }
        } else if (this.entriesHash.add(EntryStacks.hashExact(stack))) {
            if (afterEntry != null) {
                int index = entries.lastIndexOf(afterEntry);
                entries.add(index, stack);
            } else entries.add(stack);
            preFilteredList.addAll(refilterNew(true, Collections.singletonList(stack)));
            queueSearchUpdate();
        }
    }
    
    @Override
    public void addEntriesAfter(@Nullable EntryStack<?> afterEntry, Collection<? extends EntryStack<?>> stacks) {
        if (reloading) {
            int index = afterEntry != null ? reloadingRegistry.lastIndexOf(new HashedEntryStackWrapper(afterEntry)) : -1;
            List<HashedEntryStackWrapper> filtered = CollectionUtils.mapAndFilter(stacks, wrapper -> entriesHash.add(wrapper.hashExact()), HashedEntryStackWrapper::new);
            if (index >= 0) {
                reloadingRegistry.addAll(index, filtered);
            } else reloadingRegistry.addAll(filtered);
        } else {
            List<EntryStack<?>> filtered = CollectionUtils.filterToList((Collection<EntryStack<?>>) stacks, stack -> entriesHash.add(EntryStacks.hashExact(stack)));
            if (afterEntry != null) {
                int index = entries.lastIndexOf(afterEntry);
                entries.addAll(index, filtered);
            } else entries.addAll(filtered);
            preFilteredList.addAll(refilterNew(true, filtered));
            queueSearchUpdate();
        }
    }
    
    private void queueSearchUpdate() {
        if (REIRuntimeImpl.getSearchField() != null) {
            ScreenOverlayImpl.getInstance().queueReloadSearch();
        }
    }
    
    private MutableLong lastRefilterWarning = new MutableLong(-1);
    
    @ApiStatus.Internal
    @Override
    public Collection<EntryStack<?>> refilterNew(boolean warn, Collection<EntryStack<?>> entries) {
        if (lastRefilterWarning != null && warn) {
            if (lastRefilterWarning.getValue() > 0 && System.currentTimeMillis() - lastRefilterWarning.getValue() > 5000) {
                RoughlyEnoughItemsCore.LOGGER.warn("Detected runtime EntryRegistry modification, this can be extremely dangerous, or be extremely inefficient!");
            }
            lastRefilterWarning.setValue(System.currentTimeMillis());
        }
        
        FilteringContextImpl context = new FilteringContextImpl(entries);
        FilteringCacheImpl cache = new FilteringCacheImpl();
        List<FilteringRule<?>> rules = ((ConfigObjectImpl) ConfigObject.getInstance()).getFilteringRules();
        for (int i = rules.size() - 1; i >= 0; i--) {
            FilteringRule<?> rule = rules.get(i);
            cache.setCache(rule, rule.prepareCache(true));
            context.handleResult(rule.processFilteredStacks(context, cache, true));
        }
        
        Set<HashedEntryStackWrapper> hiddenStacks = context.stacks.get(FilteringContextType.HIDDEN);
        if (hiddenStacks.isEmpty()) {
            return entries;
        } else {
            return entries.parallelStream()
                    .map(HashedEntryStackWrapper::new)
                    .filter(not(hiddenStacks::contains))
                    .map(HashedEntryStackWrapper::unwrap)
                    .collect(Collectors.toList());
        }
    }
    
    @Override
    public boolean alreadyContain(EntryStack<?> stack) {
        return entriesHash.contains(EntryStacks.hashExact(stack));
    }
    
    @Override
    public boolean removeEntry(EntryStack<?> stack) {
        if (reloading) {
            HashedEntryStackWrapper wrapper = new HashedEntryStackWrapper(stack);
            reloadingRegistry.remove(wrapper);
            return entriesHash.remove(wrapper.hashExact());
        } else {
            preFilteredList.remove(stack);
            entries.remove(stack);
            return entriesHash.remove(EntryStacks.hashExact(stack));
        }
    }
    
    @Override
    public boolean removeEntryIf(Predicate<? extends EntryStack<?>> predicate) {
        if (reloading) {
            return reloadingRegistry.removeIf(wrapper -> {
                if (((Predicate<EntryStack<?>>) predicate).test(wrapper.unwrap())) {
                    entriesHash.remove(wrapper.hashExact());
                    return true;
                }
                
                return false;
            });
        } else {
            Predicate<EntryStack<?>> entryStackPredicate = stack -> {
                if (((Predicate<EntryStack<?>>) predicate).test(stack)) {
                    entriesHash.remove(EntryStacks.hashExact(stack));
                    return true;
                }
                
                return false;
            };
            preFilteredList.removeIf(entryStackPredicate);
            return entries.removeIf(entryStackPredicate);
        }
    }
    
    @Override
    public boolean removeEntryExactHashIf(LongPredicate predicate) {
        LongPredicate entryStackPredicate = hash -> {
            if (predicate.test(hash)) {
                entriesHash.remove(hash);
                return true;
            }
            
            return false;
        };
        
        if (reloading) {
            return reloadingRegistry.removeIf(wrapper -> entryStackPredicate.test(wrapper.hashExact()));
        } else {
            preFilteredList.removeIf(stack -> entryStackPredicate.test(EntryStacks.hashExact(stack)));
            return entries.removeIf(stack -> entryStackPredicate.test(EntryStacks.hashExact(stack)));
        }
    }
    
    @Override
    public boolean removeEntryFuzzyHashIf(LongPredicate predicate) {
        Predicate<EntryStack<?>> entryStackPredicate = stack -> {
            if (predicate.test(EntryStacks.hashFuzzy(stack))) {
                entriesHash.remove(EntryStacks.hashExact(stack));
                return true;
            }
            
            return false;
        };
        
        if (reloading) {
            return reloadingRegistry.removeIf(wrapper -> entryStackPredicate.test(wrapper.unwrap()));
        } else {
            preFilteredList.removeIf(entryStackPredicate);
            return entries.removeIf(entryStackPredicate);
        }
    }
}
