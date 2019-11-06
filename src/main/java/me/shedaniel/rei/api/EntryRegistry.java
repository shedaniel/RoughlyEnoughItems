/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.annotations.ToBeRemoved;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public interface EntryRegistry {
    
    static EntryRegistry getInstance() {
        return RoughlyEnoughItemsCore.getEntryRegistry();
    }
    
    /**
     * Gets the current unmodifiable item list
     *
     * @return an unmodifiable item list
     */
    @Deprecated
    default List<Entry> getEntryList() {
        return Collections.unmodifiableList(getModifiableEntryList());
    }
    
    /**
     * Gets the current modifiable stacks list
     *
     * @return a stacks list
     */
    List<EntryStack> getStacksList();
    
    /**
     * Gets the current modifiable item list
     *
     * @return an modifiable item list
     */
    @Deprecated
    default List<Entry> getModifiableEntryList() {
        return CollectionUtils.map(getStacksList(), EntryStack::toEntry);
    }
    
    /**
     * Gets all possible stacks from an item
     *
     * @param item the item to find
     * @return the array of possible stacks
     */
    ItemStack[] getAllStacksFromItem(Item item);
    
    /**
     * Registers an new stack to the item list
     *
     * @param afterItem the stack to put after
     * @param stack     the stack to register
     */
    @Deprecated
    default void registerItemStack(Item afterItem, ItemStack stack) {
        registerEntryAfter(EntryStack.create(afterItem), EntryStack.create(stack));
    }
    
    @Deprecated
    default void registerFluid(Fluid fluid) {
        registerEntry(EntryStack.create(fluid));
    }
    
    default void registerEntry(EntryStack stack) {
        registerEntryAfter(null, stack);
    }
    
    void registerEntryAfter(EntryStack afterEntry, EntryStack stack);
    
    @ToBeRemoved
    @Deprecated
    default void registerItemStack(Item afterItem, ItemStack... stacks) {
        EntryStack afterStack = EntryStack.create(afterItem);
        for (int i = stacks.length - 1; i >= 0; i--) {
            ItemStack stack = stacks[i];
            if (stack != null && !stack.isEmpty())
                registerEntryAfter(afterStack, EntryStack.create(stack));
        }
    }
    
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
    
    @ToBeRemoved
    @Deprecated
    default void registerItemStack(ItemStack... stacks) {
        registerItemStack(null, stacks);
    }
    
    /**
     * Registers multiple stacks to the item list
     *
     * @param stacks the stacks to register
     */
    default void registerEntries(EntryStack... stacks) {
        registerEntriesAfter(null, stacks);
    }
    
    @ToBeRemoved
    @Deprecated
    default boolean alreadyContain(ItemStack stack) {
        return alreadyContain(EntryStack.create(stack));
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
