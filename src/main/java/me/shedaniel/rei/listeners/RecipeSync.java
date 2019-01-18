package me.shedaniel.rei.listeners;

import net.minecraft.item.crafting.RecipeManager;

public interface RecipeSync extends IListener {
    
    public void recipesLoaded(RecipeManager recipeManager);
    
}
