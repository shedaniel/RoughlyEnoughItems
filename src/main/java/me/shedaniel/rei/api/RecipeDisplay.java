/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
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
     * @see RecipeDisplay#getInputEntries()
     */
    @ToBeRemoved
    @Deprecated
    default List<List<ItemStack>> getInput() {
        return Collections.emptyList();
    }
    
    /**
     * @return a list of inputs
     */
    default List<List<Entry>> getInputEntries() {
        List<List<ItemStack>> input = getInput();
        if (input.isEmpty())
            return Collections.emptyList();
        List<List<Entry>> list = new ArrayList<>();
        for (List<ItemStack> stacks : input) {
            List<Entry> entries = new ArrayList<>();
            for (ItemStack stack : stacks) {
                entries.add(Entry.create(stack));
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
    default List<Entry> getOutputEntry() {
        List<ItemStack> input = getOutput();
        if (input.isEmpty())
            return Collections.emptyList();
        List<Entry> entries = new ArrayList<>();
        for (ItemStack stack : input) {
            entries.add(Entry.create(stack));
        }
        return entries;
    }
    
    /**
     * Gets the required items used in craftable filters
     *
     * @return the list of required items
     */
    default List<List<ItemStack>> getRequiredItems() {
        return Lists.newArrayList();
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
