/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ApiStatus.Internal
public class EntryRegistryImpl implements EntryRegistry {
    
    private final CopyOnWriteArrayList<EntryStack> entries = Lists.newCopyOnWriteArrayList();
    private final Queue<Pair<EntryStack, Collection<? extends EntryStack>>> queueRegisterEntryStackAfter = Queues.newConcurrentLinkedQueue();
    private List<EntryStack> reloadList;
    
    public void distinct() {
        TreeSet<EntryStack> set = new TreeSet<>((i, j) -> i.equalsAll(j) ? 0 : 1);
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
    }
    
    @Override
    public List<EntryStack> getStacksList() {
        return RecipeHelper.getInstance().arePluginsLoading() ? reloadList : entries;
    }
    
    public void reset() {
        reloadList = Lists.newArrayList();
        queueRegisterEntryStackAfter.clear();
        entries.clear();
        reloadList.clear();
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
        Arrays.sort(array, (a, b) -> ItemStack.areEqualIgnoreDamage(a, b) ? 0 : 1);
        return array;
    }
    
    @Override
    @Deprecated
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
        } else
            registerEntriesAfter(afterEntry, stacks);
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
    
    private static class DefaultedLinkedList<E> extends DefaultedList<E> {
        public DefaultedLinkedList(List<E> delegate, @Nullable E initialElement) {
            super(delegate, initialElement);
        }
    }
}
