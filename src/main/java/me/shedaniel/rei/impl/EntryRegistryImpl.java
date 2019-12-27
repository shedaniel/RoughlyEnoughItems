/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.annotations.Internal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Deprecated
@Internal
public class EntryRegistryImpl implements EntryRegistry {

    private final CopyOnWriteArrayList<EntryStack> entries = Lists.newCopyOnWriteArrayList();
    private final LinkedList<EntryStack> linkedList = Lists.newLinkedList();

    public void distinct() {
        TreeSet<EntryStack> set = new TreeSet<>((i, j) -> i.equalsAll(j) ? 0 : 1);
        set.addAll(linkedList);
        entries.clear();
        entries.addAll(set);
        linkedList.clear();
    }

    @Override
    public List<EntryStack> getStacksList() {
        return RecipeHelper.getInstance().arePluginsLoading() && !linkedList.isEmpty() ? linkedList : entries;
    }

    public void reset() {
        entries.clear();
        linkedList.clear();
    }

    @Override
    public List<ItemStack> appendStacksForItem(Item item) {
        DefaultedList<ItemStack> list = new DefaultedLinkedList(Lists.newLinkedList(), null);
        item.appendStacks(item.getGroup(), list);
        if (list.isEmpty()) list.add(item.getStackForRender());
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
        if (stack.isEmpty()) return;
        if (afterEntry == null) {
            linkedList.add(stack);
        } else {
            int last = linkedList.size();
            for (int i = last - 1; i >= 0; i++)
                if (linkedList.get(i).equalsAll(afterEntry)) {
                    last = i + 1;
                    break;
                }
            linkedList.add(last, stack);
        }
    }

    @Override
    public void registerEntriesAfter(EntryStack afterStack, Collection<? extends EntryStack> stacks) {
        if (afterStack != null) {
            int index = linkedList.size();
            for (int i = index - 1; i >= 0; i--) {
                if (linkedList.get(i).equalsAll(afterStack)) {
                    index = i + 1;
                    break;
                }
            }
            linkedList.addAll(index, stacks);
        } else linkedList.addAll(stacks);
    }

    private class DefaultedLinkedList<E> extends DefaultedList<E> {
        public DefaultedLinkedList(List<E> delegate, @Nullable E initialElement) {
            super(delegate, initialElement);
        }
    }
}
