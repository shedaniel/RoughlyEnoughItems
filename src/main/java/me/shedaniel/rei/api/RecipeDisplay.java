/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface RecipeDisplay {
    
    /**
     * @return a list of items
     */
    List<List<ItemStack>> getInput();
    
    /**
     * @return a list of outputs
     */
    List<ItemStack> getOutput();
    
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
