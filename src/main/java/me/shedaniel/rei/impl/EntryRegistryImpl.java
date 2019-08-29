/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.Entry;
import me.shedaniel.rei.api.EntryRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class EntryRegistryImpl implements EntryRegistry {
    
    private final CopyOnWriteArrayList<Entry> entries = Lists.newCopyOnWriteArrayList();
    
    @Override
    public List<Entry> getEntryList() {
        return Collections.unmodifiableList(entries);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public List<Entry> getModifiableEntryList() {
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
    public void registerItemStack(Item afterItem, ItemStack stack) {
        if (!stack.isEmpty() && !alreadyContain(stack))
            if (afterItem == null || afterItem.equals(Items.AIR))
                entries.add(Entry.create(stack));
            else {
                int last = entries.size();
                for (int i = 0; i < entries.size(); i++)
                    if (entries.get(i).getEntryType() == Entry.Type.ITEM && entries.get(i).getItemStack().getItem().equals(afterItem))
                        last = i + 1;
                entries.add(last, Entry.create(stack));
            }
    }
    
    @Override
    public void registerFluid(Fluid fluid) {
        entries.add(Entry.create(fluid));
    }
    
}
