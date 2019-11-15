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

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Deprecated
@Internal
public class EntryRegistryImpl implements EntryRegistry {
    
    private final CopyOnWriteArrayList<EntryStack> entries = Lists.newCopyOnWriteArrayList();
    
    @Override
    public List<EntryStack> getStacksList() {
        return entries;
    }
    
    @Override
    public ItemStack[] getAllStacksFromItem(Item item) {
        DefaultedList<ItemStack> list = DefaultedList.of();
        list.add(item.getStackForRender());
        item.appendStacks(item.getGroup(), list);
        TreeSet<ItemStack> stackSet = list.stream().collect(Collectors.toCollection(() -> new TreeSet<ItemStack>((p1, p2) -> ItemStack.areEqualIgnoreDamage(p1, p2) ? 0 : 1)));
        return Lists.newArrayList(stackSet).toArray(new ItemStack[0]);
    }
    
    @Override
    public void registerEntryAfter(EntryStack afterEntry, EntryStack stack) {
        if (!stack.isEmpty() && !alreadyContain(stack))
            if (afterEntry == null || afterEntry.isEmpty())
                entries.add(stack);
            else {
                int last = entries.size();
                for (int i = 0; i < entries.size(); i++)
                    if (entries.get(i).equalsAll(afterEntry))
                        last = i + 1;
                entries.add(last, stack);
            }
    }
    
}
