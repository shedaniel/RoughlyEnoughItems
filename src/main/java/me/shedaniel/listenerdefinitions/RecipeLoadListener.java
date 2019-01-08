package me.shedaniel.listenerdefinitions;

import net.minecraft.recipe.RecipeManager;

public interface RecipeLoadListener extends IEvent {
    
    public void recipesLoaded(RecipeManager recipeManager);
    
}
