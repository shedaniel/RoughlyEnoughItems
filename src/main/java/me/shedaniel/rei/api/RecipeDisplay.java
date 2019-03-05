package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public interface RecipeDisplay<T extends Recipe> {
    
    Optional<T> getRecipe();
    
    List<List<ItemStack>> getInput();
    
    List<ItemStack> getOutput();
    
    default List<List<ItemStack>> getRequiredItems() {
        return Lists.newArrayList();
    }
    
    Identifier getRecipeCategory();
    
}
