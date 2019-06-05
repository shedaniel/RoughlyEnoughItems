/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public interface RecipeDisplay<T extends Recipe> {
    
    /**
     * @return the optional recipe
     */
    Optional<? extends Recipe> getRecipe();
    
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
    
}
