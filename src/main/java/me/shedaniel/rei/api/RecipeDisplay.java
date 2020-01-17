/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface RecipeDisplay {
    
    /**
     * @return a list of inputs
     */
    List<List<EntryStack>> getInputEntries();
    
    /**
     * @return a list of outputs
     */
    List<EntryStack> getOutputEntries();
    
    /**
     * Gets the required items used in craftable filters
     *
     * @return the list of required items
     */
    default List<List<EntryStack>> getRequiredEntries() {
        return Collections.emptyList();
    }
    
    /**
     * Gets the recipe display category identifier
     *
     * @return the identifier of the category
     */
    Identifier getRecipeCategory();
    
    /**
     * Gets the recipe location from datapack.
     *
     * @return the recipe location
     */
    default Optional<Identifier> getRecipeLocation() {
        return Optional.empty();
    }
    
}
