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

package me.shedaniel.rei.impl.client.registry.display;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisplaysHolderImpl implements DisplaysHolder {
    private final boolean cache;
    private final Map<CategoryIdentifier<?>, DisplaysList> displays = new ConcurrentHashMap<>();
    private final Map<CategoryIdentifier<?>, List<Display>> unmodifiableDisplays;
    private final WeakHashMap<Display, Object> displaysBase = new WeakHashMap<>();
    private Set<Display> displaysCached = new ReferenceOpenHashSet<>();
    private Set<Display> displaysNotCached = Collections.synchronizedSet(new ReferenceOpenHashSet<>());
    private boolean preprocessed = false;
    private SetMultimap<EntryStack<?>, Display> displaysByInput;
    private SetMultimap<EntryStack<?>, Display> displaysByOutput;
    private final MutableInt displayCount = new MutableInt(0);
    
    public DisplaysHolderImpl(boolean init) {
        this.cache = init && ConfigObject.getInstance().doesCacheDisplayLookup();
        this.unmodifiableDisplays = new RemappingMap<>(Collections.unmodifiableMap(displays), list -> {
            if (list == null) {
                return null;
            } else {
                return ((DisplaysList) list).unmodifiableList;
            }
        }, key -> CategoryRegistry.getInstance().tryGet(key).isPresent());
        this.displaysByInput = createSetMultimap();
        this.displaysByOutput = createSetMultimap();
    }
    
    @Override
    public boolean doesCache() {
        return this.cache;
    }
    
    @Override
    public void add(Display display, @Nullable Object origin) {
        this.displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new DisplaysList())
                .add(display);
        this.displayCount.increment();
        if (origin != null) {
            synchronized (this.displaysBase) {
                this.displaysBase.put(display, origin);
            }
        }
        if (this.cache) {
            if (!preprocessed) {
                this.displaysNotCached.add(display);
            } else {
                this.process(display);
                this.displaysCached.add(display);
            }
        } else {
            this.displaysNotCached.add(display);
        }
    }
    
    @Override
    public int size() {
        return this.displayCount.intValue();
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<Display>> get() {
        return this.unmodifiableDisplays;
    }
    
    @Override
    public void endReload() {
        if (this.cache) {
            InternalLogger.getInstance().debug("Processing %d displays for optimal lookup performance...", this.size());
            Stopwatch stopwatch = Stopwatch.createStarted();
            this.displaysCached = new ReferenceOpenHashSet<>(this.size());
            this.displaysByInput = createSetMultimap();
            this.displaysByOutput = createSetMultimap();
            for (Display display : this.displaysNotCached) {
                this.process(display);
            }
            this.displaysCached.addAll(this.displaysNotCached);
            this.displaysNotCached = Set.of();
            this.preprocessed = true;
            InternalLogger.getInstance().debug("Processed displays for optimal lookup performance in %s.", stopwatch.stop());
        }
    }
    
    private void process(Display display) {
        for (EntryIngredient input : display.getInputEntries()) {
            for (EntryStack<?> stack : input) {
                this.displaysByInput.put(stack, display);
            }
        }
        for (EntryIngredient output : display.getOutputEntries()) {
            for (EntryStack<?> stack : output) {
                this.displaysByOutput.put(stack, display);
            }
        }
    }
    
    @Override
    public boolean isCached(Display display) {
        return this.cache && this.displaysCached.contains(display);
    }
    
    @Override
    public Set<Display> getDisplaysNotCached() {
        return this.displaysNotCached;
    }
    
    @Override
    public Set<Display> getDisplaysByInput(EntryStack<?> stack) {
        return this.displaysByInput.get(stack);
    }
    
    @Override
    public Set<Display> getDisplaysByOutput(EntryStack<?> stack) {
        return this.displaysByOutput.get(stack);
    }
    
    @Override
    @Nullable
    public Object getDisplayOrigin(Display display) {
        synchronized (this.displaysBase) {
            return this.displaysBase.get(display);
        }
    }
    
    private static class DisplaysList extends ArrayList<Display> {
        private final List<Display> unmodifiableList;
        private final List<Display> synchronizedList;
        
        public DisplaysList() {
            this.synchronizedList = Collections.synchronizedList(this);
            this.unmodifiableList = Collections.unmodifiableList(synchronizedList);
        }
    }
    
    private SetMultimap<EntryStack<?>, Display> createSetMultimap() {
        return Multimaps.newSetMultimap(
                new Object2ObjectOpenCustomHashMap<>(Math.max(10000, this.size() * 5 / 2), new Hash.Strategy<>() {
                    @Override
                    public int hashCode(EntryStack<?> stack) {
                        return Long.hashCode(EntryStacks.hashFuzzy(stack));
                    }
                    
                    @Override
                    public boolean equals(EntryStack<?> o1, EntryStack<?> o2) {
                        return EntryStacks.equalsFuzzy(o1, o2);
                    }
                }),
                ReferenceOpenHashSet::new
        );
    }
}
