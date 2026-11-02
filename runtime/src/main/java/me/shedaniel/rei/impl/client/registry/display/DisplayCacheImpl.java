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
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;

import java.util.Collections;
import java.util.Set;

public class DisplayCacheImpl implements DisplayCache {
    private final boolean cache;
    private Set<Display> displaysCached = new ReferenceOpenHashSet<>();
    private Set<Display> displaysNotCached = Collections.synchronizedSet(new ReferenceOpenHashSet<>());
    private volatile SetMultimap<EntryStack<?>, Display> displaysByInput;
    private volatile SetMultimap<EntryStack<?>, Display> displaysByOutput;
    private boolean preprocessed = false;
    
    public DisplayCacheImpl(boolean init) {
        this.cache = init && ConfigObject.getInstance().doesCacheDisplayLookup();
        if (this.cache) {
            this.displaysByInput = createSetMultimap();
            this.displaysByOutput = createSetMultimap();
        }
    }
    
    @Override
    public int cachedSize() {
        return this.displaysCached.size();
    }
    
    @Override
    public int notCachedSize() {
        return this.displaysNotCached.size();
    }
    
    @Override
    public boolean doesCache() {
        return this.cache;
    }
    
    @Override
    public boolean isCached(Display display) {
        return this.cache && this.displaysCached.contains(display);
    }
    
    @Override
    public void add(Display display) {
        if (this.cache) {
            if (!this.preprocessed) {
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
    public boolean remove(Display display) {
        if (this.cache) {
            if (!this.preprocessed) {
                return this.displaysNotCached.remove(display);
            } else {
                boolean removed = this.displaysCached.remove(display);
                if (removed) {
                    for (EntryIngredient input : display.getInputEntries()) {
                        for (EntryStack<?> stack : input) {
                            this.displaysByInput.remove(stack, display);
                        }
                    }
                    for (EntryIngredient output : display.getOutputEntries()) {
                        for (EntryStack<?> stack : output) {
                            this.displaysByOutput.remove(stack, display);
                        }
                    }
                }
                return removed;
            }
        } else {
            return this.displaysNotCached.remove(display);
        }
    }
    
    @Override
    public void endReload() {
        if (this.cache) {
            if (this.preprocessed) {
                InternalLogger.getInstance().error("DisplayCache#endReload called after preprocessed!");
            }
            
            InternalLogger.getInstance().debug("Processing %d displays for optimal lookup performance...", this.displaysNotCached.size());
            Stopwatch stopwatch = Stopwatch.createStarted();
            this.displaysCached = new ReferenceOpenHashSet<>(this.displaysNotCached.size());
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
    
    @Override
    public Set<Display> getDisplaysNotCached() {
        return this.displaysNotCached;
    }
    
    @Override
    public Set<Display> getDisplaysByInput(EntryStack<?> stack) {
        SetMultimap<EntryStack<?>, Display> index = this.displaysByInput;
        if (index == null) index = this.inputIndex();
        return index.get(stack);
    }
    
    @Override
    public Set<Display> getDisplaysByOutput(EntryStack<?> stack) {
        SetMultimap<EntryStack<?>, Display> index = this.displaysByOutput;
        if (index == null) index = this.outputIndex();
        return index.get(stack);
    }

    private synchronized SetMultimap<EntryStack<?>, Display> inputIndex() {
        if (this.displaysByInput == null) this.displaysByInput = createSetMultimap();
        return this.displaysByInput;
    }

    private synchronized SetMultimap<EntryStack<?>, Display> outputIndex() {
        if (this.displaysByOutput == null) this.displaysByOutput = createSetMultimap();
        return this.displaysByOutput;
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
    
    private static SetMultimap<EntryStack<?>, Display> createSetMultimap() {
        return Multimaps.newSetMultimap(
                new Object2ObjectOpenCustomHashMap<>(5000, new Hash.Strategy<>() {
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
