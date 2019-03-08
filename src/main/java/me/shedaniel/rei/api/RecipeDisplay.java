package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Optional;

public interface RecipeDisplay<T extends IRecipe> {
    
    Optional<T> getRecipe();
    
    List<List<ItemStack>> getInput();
    
    List<ItemStack> getOutput();
    
    default List<List<ItemStack>> getRequiredItems() {
        return Lists.newArrayList();
    }
    
    ResourceLocation getRecipeCategory();
    
}
