package me.shedaniel.plugin.crafting;

import me.shedaniel.api.IRecipe;
import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class VanillaCraftingRecipe implements IRecipe<ItemStack> {
    
    public int getWidth() {
        return 2;
    }
    
    public int getHeight() {
        return 2;
    }
    
    public abstract net.minecraft.item.crafting.IRecipe getRecipe();
    
    @Override
    public List<List<ItemStack>> getRecipeRequiredInput() {
        return getInput();
    }
    
}
