package me.shedaniel.rei.api.registry;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

public interface RecipeManagerContext extends Reloadable {
    /**
     * @return a list of sorted recipes
     */
    List<Recipe<?>> getAllSortedRecipes();
    
    /**
     * Gets the vanilla recipe manager
     *
     * @return the recipe manager
     */
    RecipeManager getRecipeManager();
}
