/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ItemRegistry {
    
    /**
     * Gets the current unmodifiable item list
     *
     * @return an unmodifiable item list
     */
    List<ItemStack> getItemList();
    
    /**
     * Gets the current modifiable item list
     *
     * @return an modifiable item list
     */
    @Deprecated
    List<ItemStack> getModifiableItemList();
    
    ItemStack[] getAllStacksFromItem(Item item);
    
    /**
     * Registers an new stack to the item list
     *
     * @param afterItem
     * @param stack     the stack to register
     */
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
    
    /**
     * Checks if a stack is already registered
     *
     * @param stack the stack to check
     * @return whether the stack has been registered
     */
    default boolean alreadyContain(ItemStack stack) {
        return getItemList().stream().anyMatch(stack1 -> ItemStack.areEqual(stack, stack1));
    }
    
}
