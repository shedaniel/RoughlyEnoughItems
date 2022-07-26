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

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class EntryRegistryImpl implements EntryRegistry {
    public List<EntryRegistryListener> listeners = Lists.newCopyOnWriteArrayList();
    private PreFilteredEntryList preFilteredList;
    private EntryRegistryList registryList;
    private LongSet entriesHash;
    private boolean reloading;
    
    public EntryRegistryImpl() {
        registryList = new NormalEntryRegistryList();
        entriesHash = new LongOpenHashSet();
        preFilteredList = new PreFilteredEntryList(this);
        listeners.add(preFilteredList);
    }
    
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
        listeners.clear();
        registryList = new ReloadingEntryRegistryList();
        entriesHash = new LongOpenHashSet();
        preFilteredList = new PreFilteredEntryList(this);
        listeners.add(preFilteredList);
        reloading = true;
    }
    
    @Override
    public void endReload() {
        reloading = false;
        if (!(registryList instanceof ReloadingEntryRegistryList)) {
            throw new IllegalStateException("Expected ReloadingEntryRegistryList, got " + registryList.getClass().getName());
        }
        registryList = new NormalEntryRegistryList(registryList.stream().filter(((Predicate<EntryStack<?>>) EntryStack::isEmpty).negate()));
        refilter();
        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
        InternalLogger.getInstance().debug("Reloaded entry registry with %d entries and %d filtered entries", size(), getPreFilteredList().size());
    }
    
    @Override
    public boolean isReloading() {
        return reloading;
    }
    
    @Override
    public int size() {
        return registryList.size();
    }
    
    @Override
    public Stream<EntryStack<?>> getEntryStacks() {
        return registryList.stream();
    }
    
    @Override
    public List<EntryStack<?>> getPreFilteredList() {
        return Collections.unmodifiableList(preFilteredList.getList());
    }
    
    @Override
    public void refilter() {
        List<EntryStack<?>> stacks = registryList.collect();
        
        for (EntryRegistryListener listener : listeners) {
            listener.onReFilter(stacks);
        }
    }
    
    private static final Comparator<ItemStack> STACK_COMPARATOR = (a, b) -> ItemStack.matches(a, b) ? 0 : 1;
    
    @Override
    public List<ItemStack> appendStacksForItem(Item item) {
        NonNullList<ItemStack> list = NonNullList.create();
        LongSet set = new LongOpenHashSet();
        EntryDefinition<ItemStack> itemDefinition = VanillaEntryTypes.ITEM.getDefinition();
        for (CreativeModeTab tab : CreativeModeTab.TABS) {
            if (tab != CreativeModeTab.TAB_HOTBAR && tab != CreativeModeTab.TAB_INVENTORY) {
                NonNullList<ItemStack> tabList = NonNullList.create();
                item.fillItemCategory(tab, tabList);
                for (ItemStack stack : tabList) {
                    if (set.add(itemDefinition.hash(null, stack, ComparisonContext.EXACT))) {
                        list.add(stack);
                    }
                }
            }
        }
        if (list.isEmpty()) {
            return Collections.singletonList(item.getDefaultInstance());
        }
        if (list.size() > 1) {
            list.sort(STACK_COMPARATOR);
        }
        return list;
    }
    
    @ApiStatus.Internal
    @Override
    public Collection<EntryStack<?>> refilterNew(boolean warn, Collection<EntryStack<?>> entries) {
        return preFilteredList.refilterNew(warn, entries);
    }
    
    @Override
    public boolean alreadyContain(EntryStack<?> stack) {
        return entriesHash.contains(EntryStacks.hashExact(stack));
    }
    
    @Override
    public void addEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack) {
        long hashExact = EntryStacks.hashExact(stack);
        if (this.entriesHash.add(hashExact)) {
            if (afterEntry != null) {
                int index = registryList.lastIndexOf(afterEntry);
                registryList.add(index, stack, hashExact);
            } else registryList.add(stack, hashExact);
            
            for (EntryRegistryListener listener : listeners) {
                listener.addEntryAfter(afterEntry, stack, hashExact);
            }
        }
    }
    
    @Override
    public void addEntriesAfter(@Nullable EntryStack<?> afterEntry, Collection<? extends EntryStack<?>> stacks) {
        List<EntryStack<?>> filtered;
        LongList hashes = registryList.needsHash() ? new LongArrayList(stacks.size()) : null;
        
        if (registryList.needsHash()) {
            filtered = new ArrayList<>(stacks.size());
            for (EntryStack<?> stack : stacks) {
                long hashExact = EntryStacks.hashExact(stack);
                if (entriesHash.add(hashExact)) {
                    filtered.add(stack);
                    hashes.add(hashExact);
                }
            }
        } else {
            filtered = CollectionUtils.filterToList((List<EntryStack<?>>) stacks, entry -> entriesHash.add(EntryStacks.hashExact(entry)));
        }
        
        if (afterEntry != null) {
            int index = registryList.lastIndexOf(afterEntry);
            registryList.addAll(index, filtered, hashes);
        } else registryList.addAll(filtered, hashes);
        
        for (EntryRegistryListener listener : listeners) {
            listener.addEntriesAfter(afterEntry, filtered, hashes);
        }
    }
    
    @Override
    public boolean removeEntry(EntryStack<?> stack) {
        long hashExact = EntryStacks.hashExact(stack);
        registryList.remove(stack, hashExact);
        boolean removed = entriesHash.remove(hashExact);
        
        if (removed) {
            for (EntryRegistryListener listener : listeners) {
                listener.removeEntry(stack, hashExact);
            }
        }
        
        return removed;
    }
    
    @Override
    public boolean removeEntryIf(Predicate<? extends EntryStack<?>> predicate) {
        List<EntryStack<?>> removedStacks = new ArrayList<>();
        LongList hashes = registryList.needsHash() ? new LongArrayList() : null;
        
        boolean removed = registryList.removeIf(stack -> {
            if (((Predicate<EntryStack<?>>) predicate).test(stack)) {
                long hashExact = EntryStacks.hashExact(stack);
                entriesHash.remove(hashExact);
                removedStacks.add(stack);
                if (hashes != null) hashes.add(hashExact);
                return true;
            }
            
            return false;
        });
        
        if (removed) {
            for (EntryRegistryListener listener : listeners) {
                listener.removeEntries(removedStacks, hashes);
            }
        }
        
        return removed;
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
        
        for (EntryRegistryListener listener : listeners) {
            listener.removeEntriesIf(stack -> predicate.test(EntryStacks.hashExact(stack)));
        }
        
        return registryList.removeExactIf(entryStackPredicate);
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
        
        for (EntryRegistryListener listener : listeners) {
            listener.removeEntriesIf(entryStackPredicate);
        }
        
        return registryList.removeIf(entryStackPredicate);
    }
}
