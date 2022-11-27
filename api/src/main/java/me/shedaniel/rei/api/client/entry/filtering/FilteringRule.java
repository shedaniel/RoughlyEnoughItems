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

package me.shedaniel.rei.api.client.entry.filtering;

import it.unimi.dsi.fastutil.longs.LongCollection;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A filtering rule type that is used to filter the entries on the entry panel,
 * dictate what shows up in slots, or hide the entire display if all ingredients are filtered.
 *
 * @param <Cache> the type of the cache
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FilteringRule<Cache> {
    /**
     * Returns the type of this filtering rule.
     *
     * @return the type of this filtering rule
     */
    FilteringRuleType<? extends FilteringRule<Cache>> getType();
    
    /**
     * Prepares the cache for this filtering rule.
     *
     * @param async whether the cache should be prepared asynchronously
     * @return the cache
     */
    default Cache prepareCache(boolean async) {
        return null;
    }
    
    /**
     * Processes the specified entry with the specified cache.
     *
     * @param context       the context of this filtering
     * @param resultFactory the result factory
     * @param cache         the cache
     * @param async         whether the stacks should be processed asynchronously
     * @return the result of the processing
     */
    FilteringResult processFilteredStacks(FilteringContext context, FilteringResultFactory resultFactory, Cache cache, boolean async);
    
    /**
     * Returns whether the entry registry is in its reloading phase.
     * Registration after the reloading phase will be slow and may not be reflected immediately.
     *
     * @return whether the entry registry is in its reloading phase
     */
    @ApiStatus.NonExtendable
    default boolean isReloading() {
        return EntryRegistry.getInstance().isReloading();
    }
    
    @ApiStatus.NonExtendable
    default void markDirty(Collection<EntryStack<?>> stacks, @Nullable LongCollection hashes) {
        EntryRegistry.getInstance().markFilteringRuleDirty(this, stacks, hashes);
    }
}