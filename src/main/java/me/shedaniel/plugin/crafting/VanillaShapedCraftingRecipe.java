package me.shedaniel.plugin.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;

import java.util.LinkedList;
import java.util.List;

public class VanillaShapedCraftingRecipe extends VanillaCraftingRecipe {
    
    private final ShapedRecipe recipe;
    
    public VanillaShapedCraftingRecipe(ShapedRecipe recipe) {
        this.recipe = recipe;
    }
    
    @Override
    public ShapedRecipe getRecipe() {
        return recipe;
    }
    
    @Override
    public int getWidth() {
        return recipe.getWidth();
    }
    
    @Override
    public int getHeight() {
        return recipe.getHeight();
    }
    
    @Override
    public String getId() {
        return "vanilla";
    }
    
    @Override
    public List<ItemStack> getOutput() {
        List<ItemStack> output = new LinkedList<>();
        output.add(recipe.getRecipeOutput());
        return output;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        int count = 0;
        for(Ingredient ingredient : recipe.getIngredients()) {
            List<ItemStack> ingList = new LinkedList<>();
            for(ItemStack matchingStack : ingredient.getMatchingStacks()) {
                ingList.add(matchingStack);
            }
            input.add(ingList);
            count++;
        }
        return input;
    }
}
