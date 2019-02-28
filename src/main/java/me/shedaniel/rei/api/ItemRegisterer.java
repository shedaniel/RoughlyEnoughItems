package me.shedaniel.rei.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ItemRegisterer {
    
    List<ItemStack> getItemList();
    
    @Deprecated
    List<ItemStack> getModifiableItemList();
    
    ItemStack[] getAllStacksFromItem(Item item);
    
    void registerItemStack(Item afterItem, ItemStack stack);
    
    default void registerItemStack(Item afterItem, ItemStack... stacks) {
        for(ItemStack stack : stacks)
            if (stack != null && !stack.isEmpty())
                registerItemStack(afterItem, stack);
    }
    
    default void registerItemStack(ItemStack... stacks) {
        for(ItemStack stack : stacks)
            if (stack != null && !stack.isEmpty())
                registerItemStack(null, stack);
    }
    
    default boolean alreadyContain(ItemStack stack) {
        return getItemList().stream().anyMatch(stack1 -> ItemStack.areEqual(stack, stack1));
    }
    
}
