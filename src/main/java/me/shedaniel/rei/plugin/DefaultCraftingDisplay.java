package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.IRecipeDisplay;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.Identifier;

public interface DefaultCraftingDisplay<T> extends IRecipeDisplay<Recipe> {
    
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
    
}
