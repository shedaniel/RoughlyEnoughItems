package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

public interface DefaultCraftingDisplay<T> extends RecipeDisplay<IRecipe> {
    
    @Override
    default ResourceLocation getRecipeCategory() {
        return DefaultPlugin.CRAFTING;
    }
    
    default public int getWidth() {
        return 2;
    }
    
    default public int getHeight() {
        return 2;
    }
    
}
