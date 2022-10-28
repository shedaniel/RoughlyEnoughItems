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

package me.shedaniel.rei.api.client.registry.entry;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Registry for registering {@link EntryStack} for display on the overlay entry list.
 * <p>
 * The default REI plugin iterates both {@link Registry#ITEM} and {@link Registry#FLUID}
 * to register new entries. Other plugins may override this default behaviour, altering
 * the entry list.
 * <p>
 * REI plugins should only add entries during reload, modifications outside the
 * reload phase may not be reflected immediately.<br>
 * Runtime modifications are also warned about.
 * <p>
 * While this registry can be used for registering variants of stacks, there may be
 * better alternatives such as {@link Item#fillItemCategory(CreativeModeTab, NonNullList)}.
 *
 * @see REIClientPlugin#registerEntries(EntryRegistry)
 */
@Environment(EnvType.CLIENT)
public interface EntryRegistry extends Reloadable<REIClientPlugin> {
    /**
     * @return the instance of {@link EntryRegistry}
     */
    static EntryRegistry getInstance() {
        return PluginManager.getClientInstance().get(EntryRegistry.class);
    }
    
    /**
     * @return the size of entry stacks, before being filtered by filtering rules.
     */
    int size();
    
    /**
     * @return the unmodifiable stream of entry stacks, before being filtered by filtering rules.
     */
    Stream<EntryStack<?>> getEntryStacks();
    
    /**
     * @return the unmodifiable list of filtered entry stacks,
     * only available <b>after</b> plugins reload.
     */
    List<EntryStack<?>> getPreFilteredList();
    
    /**
     * Applies the filtering rules to the entry list, is rather computational expensive.
     * The filtered entries are retrievable at {@link EntryRegistry#getPreFilteredList()}
     */
    void refilter();
    
    /**
     * Returns all possible stacks from an item, tries to invoke {@link Item#fillItemCategory(CreativeModeTab, NonNullList)}.
     *
     * @param item the item to find
     * @return the list of possible stacks, will never be empty.
     * @deprecated deprecated logic due to 1.19.3 new creative tabs logic
     */
    @Deprecated(forRemoval = true)
    List<ItemStack> appendStacksForItem(Item item);
    
    /**
     * Adds a new stack to the entry list.
     *
     * @param stack the stack to add
     */
    default void addEntry(EntryStack<?> stack) {
        addEntryAfter(null, stack);
    }
    
    /**
     * Adds a new stack to the entry list, after a certain stack.
     *
     * @param afterEntry the stack to put after
     * @param stack      the stack to add
     */
    void addEntryAfter(@Nullable EntryStack<?> afterEntry, EntryStack<?> stack);
    
    /**
     * Adds multiple stacks to the item list, after a certain stack.
     *
     * @param afterStack the stack to put after
     * @param stacks     the stacks to add
     */
    default void addEntriesAfter(@Nullable EntryStack<?> afterStack, EntryStack<?>... stacks) {
        addEntriesAfter(afterStack, Arrays.asList(stacks));
    }
    
    /**
     * Adds multiple stacks to the item list, after a certain stack.
     *
     * @param afterStack the stack to put after
     * @param stacks     the stacks to add
     */
    void addEntriesAfter(@Nullable EntryStack<?> afterStack, Collection<? extends EntryStack<?>> stacks);
    
    /**
     * Adds multiple stacks to the item list.
     *
     * @param stacks the stacks to add
     */
    default void addEntries(EntryStack<?>... stacks) {
        addEntries(Arrays.asList(stacks));
    }
    
    /**
     * Adds multiple stacks to the item list.
     *
     * @param stacks the stacks to add
     */
    default void addEntries(Collection<? extends EntryStack<?>> stacks) {
        addEntriesAfter(null, stacks);
    }
    
    // TODO Re-evaluate the need for this
    @ApiStatus.Internal
    Collection<EntryStack<?>> refilterNew(boolean warn, Collection<EntryStack<?>> entries);
    
    /**
     * Checks if a stack is already registered.
     *
     * @param stack the stack to check
     * @return whether the stack has been registered
     */
    boolean alreadyContain(EntryStack<?> stack);
    
    /**
     * Removes an entry from the entry list, if it exists.
     *
     * @param stack the stack to remove
     * @return whether it was successful to remove the entry
     */
    boolean removeEntry(EntryStack<?> stack);
    
    /**
     * Removes entries from the entry list, if it matches the predicate.
     *
     * @param predicate a predicate which returns {@code true} for the entries to be removed
     * @return whether it was successful to remove any entry
     */
    boolean removeEntryIf(Predicate<? extends EntryStack<?>> predicate);
    
    /**
     * Removes entries from the entry list, if it matches the predicate.
     * This method is usually faster than {@link #removeEntryIf(Predicate)}
     * due to its fast comparison.
     *
     * @param predicate a predicate which returns {@code true} for the entries to be removed
     * @return whether it was successful to remove any entry
     */
    boolean removeEntryExactHashIf(LongPredicate predicate);
    
    /**
     * Removes entries from the entry list, if it matches the predicate.
     * This method is usually faster than {@link #removeEntryIf(Predicate)}
     * due to its fast comparison.
     *
     * @param predicate a predicate which returns {@code true} for the entries to be removed
     * @return whether it was successful to remove any entry
     */
    boolean removeEntryFuzzyHashIf(LongPredicate predicate);
    
    /**
     * Returns whether the registry is in its reloading phase.
     * Registration after the reloading phase will be slow and may not be reflected immediately.
     *
     * @return whether the registry is in its reloading phase
     */
    boolean isReloading();
}
