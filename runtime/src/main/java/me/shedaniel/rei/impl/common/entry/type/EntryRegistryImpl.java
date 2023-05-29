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

package me.shedaniel.rei.impl.common.entry.type;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.*;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    private final EntryRegistryList registryList = new EntryRegistryListImpl();
    private FilteredEntryList filteredList;
    private LongSet entriesHash;
    private boolean reloading;
    
    public EntryRegistryImpl() {
        this.entriesHash = new LongOpenHashSet();
        this.filteredList = new PreFilteredEntryList(this, this.registryList);
        this.listeners.add(this.filteredList);
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
        this.listeners.clear();
        this.registryList.collectHashed().clear();
        this.entriesHash = new LongOpenHashSet();
        this.filteredList = new PreFilteredEntryList(this, this.registryList);
        this.listeners.add(filteredList);
        this.reloading = true;
    }
    
    @Override
    public void endReload() {
        this.reloading = false;
        refilter();
        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
        InternalLogger.getInstance().debug("Reloaded entry registry with %d entries and %d filtered entries", size(), getPreFilteredList().size());
    }
    
    @Override
    public boolean isReloading() {
        return reloading;
    }
    
    @Override
    public <Cache> void markFilteringRuleDirty(FilteringRule<Cache> cacheFilteringRule, Collection<EntryStack<?>> stacks, @Nullable LongCollection hashes) {
        this.filteredList.refreshFilteringFor(Set.of(cacheFilteringRule), stacks, hashes);
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
        return Collections.unmodifiableList(filteredList.getList());
    }
    
    public List<HashedEntryStackWrapper> getPreFilteredComplexList() {
        return Collections.unmodifiableList(filteredList.getComplexList());
    }
    
    @Override
    public void refilter() {
        List<HashedEntryStackWrapper> stacks = registryList.collectHashed();
        
        for (EntryRegistryListener listener : listeners) {
            listener.onReFilter(stacks);
        }
    }
    
    private static final Comparator<ItemStack> STACK_COMPARATOR = (a, b) -> ItemStack.matches(a, b) ? 0 : 1;
    
    @Override
    public List<ItemStack> appendStacksForItem(Item item) {
        return List.of(item.getDefaultInstance());
    }
    
    @ApiStatus.Internal
    @Override
    public Collection<EntryStack<?>> refilterNew(boolean warn, Collection<EntryStack<?>> entries) {
        if (warn) FilteringLogic.warnFiltering();
        return FilteringLogic.filter(FilteringLogic.getRules(), false, true, entries);
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
            if (index != -1) {
                registryList.addAll(index, filtered, hashes);
            } else {
                registryList.addAll(filtered, hashes);
            }
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
        
        boolean removed = registryList.removeExactIf((stack, hashExact) -> {
            if (((Predicate<EntryStack<?>>) predicate).test(stack)) {
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
        EntryRegistryList.StackFilteringPredicate entryStackPredicate = (stack, hash) -> {
            if (predicate.test(hash)) {
                entriesHash.remove(hash);
                for (EntryRegistryListener listener : listeners) {
                    listener.removeEntry(stack, hash);
                }
                return true;
            }
            
            return false;
        };
        
        return registryList.removeExactIf(entryStackPredicate);
    }
    
    @Override
    public boolean removeEntryFuzzyHashIf(LongPredicate predicate) {
        EntryRegistryList.StackFilteringPredicate entryStackPredicate = (stack, hashExact) -> {
            if (predicate.test(EntryStacks.hashFuzzy(stack))) {
                entriesHash.remove(hashExact);
                for (EntryRegistryListener listener : listeners) {
                    listener.removeEntry(stack, hashExact);
                }
                return true;
            }
            
            return false;
        };
        
        return registryList.removeExactIf(entryStackPredicate);
    }
}
