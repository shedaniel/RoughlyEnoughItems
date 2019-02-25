package me.shedaniel.rei.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;
import java.util.Optional;

public interface IItemRegisterer {
    
    public List<ItemStack> getItemList();
    
    @Deprecated
    public List<ItemStack> getModifiableItemList();
    
    public Optional<NonNullList<ItemStack>> getAlterativeStacks(Item item);
    
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
        return false && getItemList().stream().anyMatch(stack1 -> ItemStack.areItemStacksEqual(stack, stack1));
    }
    
}
