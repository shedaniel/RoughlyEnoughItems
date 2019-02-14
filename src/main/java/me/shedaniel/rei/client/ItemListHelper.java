package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IItemRegisterer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DefaultedList;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ItemListHelper implements IItemRegisterer {
    
    private final List<ItemStack> itemList = Lists.newLinkedList();
    
    @Override
    public List<ItemStack> getItemList() {
        return Collections.unmodifiableList(itemList);
    }
    
    @Deprecated
    @Override
    public List<ItemStack> getModifiableItemList() {
        return itemList;
    }
    
    @Override
    public ItemStack[] getAllStacksFromItem(Item item) {
        DefaultedList<ItemStack> list = DefaultedList.create();
        list.add(item.getDefaultStack());
        item.addStacksForDisplay(item.getItemGroup(), list);
        TreeSet<ItemStack> stackSet = list.stream().collect(Collectors.toCollection(() -> new TreeSet<ItemStack>((p1, p2) -> ItemStack.areEqual(p1, p2) ? 0 : 1)));
        return Lists.newArrayList(stackSet).toArray(new ItemStack[0]);
    }
    
    @Override
    public void registerItemStack(Item afterItem, ItemStack stack) {
        if (!stack.isEmpty() && !alreadyContain(stack))
            if (afterItem == null || afterItem.equals(Items.AIR))
                itemList.add(stack);
            else {
                int last = itemList.size();
                for(int i = 0; i < itemList.size(); i++)
                    if (itemList.get(i).getItem().equals(afterItem))
                        last = i + 1;
                itemList.add(last, stack);
            }
    }
    
}
