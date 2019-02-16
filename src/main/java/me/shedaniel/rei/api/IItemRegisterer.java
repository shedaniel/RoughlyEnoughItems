package me.shedaniel.rei.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IItemRegisterer {
    
    public List<ItemStack> getItemList();
    
    @Deprecated
    public List<ItemStack> getModifiableItemList();
    
    public ItemStack[] getAllStacksFromItem(Item item);
    
    public void registerItemStack(Item afterItem, ItemStack stack);
    
    default public void registerItemStack(Item afterItem, ItemStack... stacks) {
        for(ItemStack stack : stacks)
            if (stack != null && !stack.isEmpty())
                registerItemStack(afterItem, stack);
    }
    
    default public void registerItemStack(ItemStack... stacks) {
        for(ItemStack stack : stacks)
            if (stack != null && !stack.isEmpty())
                registerItemStack(null, stack);
    }
    
    default boolean alreadyContain(ItemStack stack) {
        return getItemList().stream().anyMatch(stack1 -> ItemStack.areItemStacksEqual(stack, stack1));
    }
    
}
