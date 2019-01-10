package me.shedaniel.rei.listeners;

import net.minecraft.recipe.RecipeManager;

public interface RecipeSync extends IListener {
    
    public void recipesLoaded(RecipeManager recipeManager);
    
}
