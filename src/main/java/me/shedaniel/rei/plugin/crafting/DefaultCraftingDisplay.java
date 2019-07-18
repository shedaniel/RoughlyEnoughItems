/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.crafting;

import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.crafting.IRecipe;

import java.util.Optional;

public interface DefaultCraftingDisplay extends RecipeDisplay {
    
    @Override
    default Identifier getRecipeCategory() {
        return DefaultPlugin.CRAFTING;
    }
    
    default public int getWidth() {
        return 2;
    }
    
    default public int getHeight() {
        return 2;
    }
    
    Optional<IRecipe> getOptionalRecipe();
    
}
