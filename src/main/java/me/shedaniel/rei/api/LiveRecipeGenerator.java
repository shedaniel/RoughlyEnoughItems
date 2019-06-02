/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public interface LiveRecipeGenerator<T extends RecipeDisplay> {
    
    Identifier getCategoryIdentifier();
    
    Optional<List<T>> getRecipeFor(ItemStack stack);
    
    Optional<List<T>> getUsageFor(ItemStack stack);
    
}
