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

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ApiStatus.Internal
public class EntryRegistryImpl implements EntryRegistry {
    
    private final CopyOnWriteArrayList<EntryStack> preFilteredList = Lists.newCopyOnWriteArrayList();
    private final CopyOnWriteArrayList<EntryStack> entries = Lists.newCopyOnWriteArrayList();
    private final Queue<Pair<EntryStack, Collection<? extends EntryStack>>> queueRegisterEntryStackAfter = Queues.newConcurrentLinkedQueue();
    private List<EntryStack> reloadList;
    private boolean doingDistinct = false;
    
    private static EntryStack findFirstOrNullEqualsEntryIgnoreAmount(Collection<EntryStack> list, EntryStack obj) {
        for (EntryStack t : list) {
            if (t.equalsIgnoreAmount(obj))
                return t;
        }
        return null;
    }
    
    public void distinct() {
        preFilteredList.clear();
        doingDistinct = true;
        Set<EntryStack> set = Sets.newLinkedHashSet();
        set.addAll(reloadList);
        entries.clear();
        entries.addAll(set);
        entries.removeIf(EntryStack::isEmpty);
        reloadList.clear();
        while (true) {
            Pair<EntryStack, Collection<? extends EntryStack>> pair = queueRegisterEntryStackAfter.poll();
            if (pair == null)
                break;
            registerEntriesAfter(pair.getLeft(), pair.getRight());
        }
        set.clear();
        set.addAll(entries);
        set.removeIf(EntryStack::isEmpty);
        entries.clear();
        entries.addAll(set);
        set.clear();
        doingDistinct = false;
    }
    
    @Override
    public List<EntryStack> getStacksList() {
        return RecipeHelper.getInstance().arePluginsLoading() && !doingDistinct ? reloadList : entries;
    }
    
    @Override
    public List<EntryStack> getPreFilteredList() {
        return preFilteredList;
    }
    
    public void refilter() {
        long started = System.currentTimeMillis();
        Collection<EntryStack> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
        preFilteredList.clear();
        for (EntryStack stack : getStacksList()) {
            if (findFirstOrNullEqualsEntryIgnoreAmount(filteredStacks, stack) == null)
                preFilteredList.add(stack);
        }
        long time = System.currentTimeMillis() - started;
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Refiltered %d entries in %dms.", filteredStacks.size(), time);
    }
    
    public void reset() {
        doingDistinct = false;
        reloadList = Lists.newArrayList();
        queueRegisterEntryStackAfter.clear();
        entries.clear();
        reloadList.clear();
        preFilteredList.clear();
    }
    
    @Override
    public List<ItemStack> appendStacksForItem(Item item) {
        DefaultedList<ItemStack> list = new DefaultedLinkedList<>(Lists.newLinkedList(), null);
        item.appendStacks(item.getGroup(), list);
        if (list.isEmpty())
            list.add(item.getStackForRender());
        return list;
    }
    
    @Override
    public ItemStack[] getAllStacksFromItem(Item item) {
        List<ItemStack> list = appendStacksForItem(item);
        ItemStack[] array = list.toArray(new ItemStack[0]);
        Arrays.sort(array, (a, b) -> ItemStack.areEqual(a, b) ? 0 : 1);
        return array;
    }
    
    @Override
    @Deprecated
    @ApiStatus.Internal
    public void registerEntryAfter(EntryStack afterEntry, EntryStack stack, boolean checkAlreadyContains) {
        if (stack.isEmpty())
            return;
        if (afterEntry == null) {
            getStacksList().add(stack);
        } else {
            int last = getStacksList().size();
            for (int i = last - 1; i >= 0; i--)
                if (getStacksList().get(i).equalsAll(afterEntry)) {
                    last = i + 1;
                    break;
                }
            getStacksList().add(last, stack);
        }
    }
    
    @Override
    public void queueRegisterEntryAfter(EntryStack afterEntry, Collection<? extends EntryStack> stacks) {
        if (RecipeHelper.getInstance().arePluginsLoading()) {
            queueRegisterEntryStackAfter.add(new Pair<>(afterEntry, stacks));
        } else {
            registerEntriesAfter(afterEntry, stacks);
        }
    }
    
    @Override
    public void registerEntriesAfter(EntryStack afterStack, Collection<? extends EntryStack> stacks) {
        if (afterStack != null) {
            int index = getStacksList().size();
            for (int i = index - 1; i >= 0; i--) {
                if (getStacksList().get(i).equalsAll(afterStack)) {
                    index = i + 1;
                    break;
                }
            }
            getStacksList().addAll(index, stacks);
        } else
            getStacksList().addAll(stacks);
    }
    
    @ApiStatus.Internal
    public static class DefaultedLinkedList<E> extends DefaultedList<E> {
        public DefaultedLinkedList(List<E> delegate, @Nullable E initialElement) {
            super(delegate, initialElement);
        }
    }
}
