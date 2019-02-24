package me.shedaniel.rei.utils;

import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;

public class RecipeBookUtils {
    
    public static GhostRecipe getGhostRecipe(GuiRecipeBook recipeBook) throws Throwable {
        return ReflectionUtils.getField(recipeBook, GhostRecipe.class, 4).orElseThrow(ReflectionUtils.ReflectionException::new);
    }
    
}
