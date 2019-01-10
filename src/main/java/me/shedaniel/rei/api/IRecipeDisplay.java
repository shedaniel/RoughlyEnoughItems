package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.List;

public interface IRecipeDisplay<T extends Recipe> {
    
    public abstract T getRecipe();
    
    public List<List<ItemStack>> getInput();
    
    public List<ItemStack> getOutput();
    
    default public List<List<ItemStack>> getRequiredItems() {
        return Lists.newArrayList();
    }
    
    public Identifier getRecipeCategory();
    
}
