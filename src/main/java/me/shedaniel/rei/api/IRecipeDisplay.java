package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IRecipeDisplay<T extends IRecipe> {
    
    public abstract T getRecipe();
    
    public List<List<ItemStack>> getInput();
    
    public List<ItemStack> getOutput();
    
    default public List<List<ItemStack>> getRequiredItems() {
        return Lists.newArrayList();
    }
    
    public ResourceLocation getRecipeCategory();
    
}
