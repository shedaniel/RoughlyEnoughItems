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
    
    @Deprecated
    default Optional<List<T>> getRecipeFor(ItemStack stack) {
        return Optional.empty();
    }
    
    default Optional<List<T>> getRecipeFor(EntryStack entry) {
        return Optional.empty();
    }
    
    @Deprecated
    default Optional<List<T>> getUsageFor(ItemStack stack) {
        return Optional.empty();
    }
    
    default Optional<List<T>> getUsageFor(EntryStack entry) {
        return Optional.empty();
    }
    
}
