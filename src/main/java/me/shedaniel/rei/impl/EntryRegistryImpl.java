/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.annotations.Internal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Deprecated
@Internal
public class EntryRegistryImpl implements EntryRegistry {

    private final CopyOnWriteArrayList<EntryStack> entries = Lists.newCopyOnWriteArrayList();
    private final Set<EntryStack> entrySet = ObjectSets.synchronize(new ObjectOpenCustomHashSet<>(new Hash.Strategy<EntryStack>() {
        @Override
        public int hashCode(EntryStack entry) {
            return entry == null ? 0 : entry.hashOfAll();
        }

        @Override
        public boolean equals(EntryStack a, EntryStack b) {
            if (a == null || b == null) {
                return a == b;
            } else {
                boolean result = a.equalsAll(b);
                assert result == b.equalsAll(a) : "a.equalsAll(b) != b.equalsAll(a); (a = " + a + ", b = " + b + ")";
                return result;
            }
        }
    }));

    @Override
    public List<EntryStack> getStacksList() {
        return entries;
    }

    @Override
    public ItemStack[] getAllStacksFromItem(Item item) {
        DefaultedList<ItemStack> list = DefaultedList.of();
        list.add(item.getStackForRender());
        item.appendStacks(item.getGroup(), list);
        ItemStack[] array = list.toArray(new ItemStack[0]);
        Arrays.sort(array, (a, b) -> ItemStack.areEqualIgnoreDamage(a, b) ? 0 : 1);
        return array;
    }

    @Override
    @Deprecated
    public void registerEntryAfter(EntryStack afterEntry, EntryStack stack, boolean checkAlreadyContains) {
        if (stack.isEmpty()) return;
        boolean isNew = entrySet.add(stack);
        if (checkAlreadyContains && !isNew) {
            return;
        }
        if (afterEntry == null) {
            entries.add(stack);
        } else {
            int last = entries.size();
            for (int i = 0; i < entries.size(); i++)
                if (entries.get(i).equalsAll(afterEntry))
                    last = i + 1;
            entries.add(last, stack);
        }
    }

    @Override
    public void registerEntriesAfter(EntryStack afterStack, Collection<? extends EntryStack> stacks) {
        List<EntryStack> nonDuplicates = new ArrayList<>();
        for (EntryStack stack : stacks) {
            if (entrySet.add(stack)) {
                nonDuplicates.add(stack);
            }
        }
        int index = entries.size();
        if (afterStack != null) {
            for (int i = index - 1; i >= 0; i--) {
                if (entries.get(i).equalsAll(afterStack)) {
                    index = i + 1;
                    break;
                }
            }
        }
        entries.addAll(index, nonDuplicates);
    }

    @Override
    public void registerEntriesAfter(EntryStack afterStack, EntryStack... stacks) {
        registerEntriesAfter(afterStack, Arrays.asList(stacks));
    }
}
