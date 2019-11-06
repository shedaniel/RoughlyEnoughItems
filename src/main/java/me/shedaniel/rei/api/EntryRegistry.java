/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.annotations.ToBeRemoved;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface EntryRegistry {
    
    static EntryRegistry getInstance() {
        return RoughlyEnoughItemsCore.getEntryRegistry();
    }
    
    /**
     * Gets the current modifiable stacks list
     *
     * @return a stacks list
     */
    List<EntryStack> getStacksList();
    
    /**
     * Gets all possible stacks from an item
     *
     * @param item the item to find
     * @return the array of possible stacks
     */
    ItemStack[] getAllStacksFromItem(Item item);
    
    default void registerEntry(EntryStack stack) {
        registerEntryAfter(null, stack);
    }
    
    /**
     * Registers an new stack to the entry list
     *
     * @param afterEntry the stack to put after
     * @param stack      the stack to register
     */
    void registerEntryAfter(EntryStack afterEntry, EntryStack stack);
    
    /**
     * Registers multiple stacks to the item list
     *
     * @param afterStack the stack to put after
     * @param stacks     the stacks to register
     */
    default void registerEntriesAfter(EntryStack afterStack, EntryStack... stacks) {
        for (int i = stacks.length - 1; i >= 0; i--) {
            EntryStack stack = stacks[i];
            if (stack != null && !stack.isEmpty())
                registerEntryAfter(afterStack, stack);
        }
    }
    
    /**
     * Registers multiple stacks to the item list
     *
     * @param stacks the stacks to register
     */
    default void registerEntries(EntryStack... stacks) {
        registerEntriesAfter(null, stacks);
    }
    
    /**
     * Checks if a stack is already registered
     *
     * @param stack the stack to check
     * @return whether the stack has been registered
     */
    default boolean alreadyContain(EntryStack stack) {
        return CollectionUtils.anyMatchEqualsAll(getStacksList(), stack);
    }
    
}
