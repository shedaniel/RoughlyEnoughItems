package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public interface DefaultCraftingDisplay<T> extends RecipeDisplay<Recipe> {
    
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
