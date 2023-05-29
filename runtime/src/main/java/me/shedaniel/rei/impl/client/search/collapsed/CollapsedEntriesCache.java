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

package me.shedaniel.rei.impl.client.search.collapsed;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsibleEntryRegistryImpl;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CollapsedEntriesCache {
    private static CollapsedEntriesCache instance = new CollapsedEntriesCache();
    private final Long2ObjectMap<Set<ResourceLocation>> cache = new Long2ObjectOpenHashMap<>();
    
    public static void reset() {
        CollapsedEntriesCache.instance = new CollapsedEntriesCache();
    }
    
    public static CollapsedEntriesCache getInstance() {
        return instance;
    }
    
    public void prepare(Collection<HashedEntryStackWrapper> stacks) {
        Collection<CollapsibleEntryRegistryImpl.Entry> entries = ((CollapsibleEntryRegistryImpl) CollapsibleEntryRegistry.getInstance()).getEntries();
        InternalLogger.getInstance().debug("Preparing collapsed entry groups cache with %d entries and %d stacks", entries.size(), stacks.size());
        
        List<CompletableFuture<?>> futures = Lists.newArrayList();
        for (CollapsibleEntryRegistryImpl.Entry entry : entries) {
            if (entry.canCache()) {
                for (HashedEntryStackWrapper stack : stacks) {
                    if (entry.getMatcher().matches(stack.unwrap(), stack.hashExact())) {
                        cache.computeIfAbsent(stack.hashExact(), $ -> new HashSet<>())
                                .add(entry.getId());
                    } else {
                        Set<ResourceLocation> locations = cache.get(stack.hashExact());
                        if (locations != null) {
                            locations.remove(entry.getId());
                            if (locations.isEmpty()) {
                                cache.remove(stack.hashExact());
                            }
                        }
                    }
                }
            }
        }
        
        InternalLogger.getInstance().debug("Prepared collapsed entry groups cache with %d entries and %d stacks", entries.size(), stacks.size());
    }
    
    @Nullable
    public Set<ResourceLocation> getEntries(long hash) {
        return cache.get(hash);
    }
}
