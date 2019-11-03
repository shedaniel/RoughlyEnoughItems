/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.api.annotations.ToBeRemoved;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface RecipeDisplay {
    
    /**
     * @return a list of items
     * @see RecipeDisplay#getInputStacks()
     */
    @ToBeRemoved
    @Deprecated
    default List<List<ItemStack>> getInput() {
        return Collections.emptyList();
    }
    
    /**
     * @return a list of inputs
     */
    default List<List<EntryStack>> getInputEntries() {
        List<List<ItemStack>> input = getInput();
        if (input.isEmpty())
            return Collections.emptyList();
        List<List<EntryStack>> list = new ArrayList<>();
        for (List<ItemStack> stacks : input) {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : stacks) {
                entries.add(EntryStack.create(stack));
            }
            list.add(entries);
        }
        return list;
    }
    
    /**
     * @return a list of outputs
     */
    @ToBeRemoved
    @Deprecated
    default List<ItemStack> getOutput() {
        return Collections.emptyList();
    }
    
    /**
     * @return a list of outputs
     */
    default List<EntryStack> getOutputEntries() {
        List<ItemStack> input = getOutput();
        if (input.isEmpty())
            return Collections.emptyList();
        List<EntryStack> entries = new ArrayList<>();
        for (ItemStack stack : input) {
            entries.add(EntryStack.create(stack));
        }
        return entries;
    }
    
    /**
     * Gets the required items used in craftable filters
     *
     * @return the list of required items
     */
    default List<List<EntryStack>> getRequiredEntries() {
        List<List<ItemStack>> input = getRequiredItems();
        if (input.isEmpty())
            return Collections.emptyList();
        List<List<EntryStack>> list = new ArrayList<>();
        for (List<ItemStack> stacks : input) {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : stacks) {
                entries.add(EntryStack.create(stack));
            }
            list.add(entries);
        }
        return list;
    }
    
    @ToBeRemoved
    @Deprecated
    default List<List<ItemStack>> getRequiredItems() {
        return Collections.emptyList();
    }
    
    /**
     * Gets the recipe display category identifier
     *
     * @return the identifier of the category
     */
    Identifier getRecipeCategory();
    
    /**
     * Gets the recipe location from datapack
     *
     * @return the recipe location
     */
    default Optional<Identifier> getRecipeLocation() {
        return Optional.empty();
    }
    
}
