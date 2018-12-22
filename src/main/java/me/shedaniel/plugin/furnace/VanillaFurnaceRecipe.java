package me.shedaniel.plugin.furnace;

import me.shedaniel.api.IRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.LinkedList;
import java.util.List;

public class VanillaFurnaceRecipe implements IRecipe<ItemStack> {
    private final FurnaceRecipe recipe;
    
    @Override
    public String getId() {
        return "furnace";
    }
    
    public VanillaFurnaceRecipe(FurnaceRecipe recipe) {
        this.recipe = recipe;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        List<ItemStack> output = new LinkedList<>();
        output.add(recipe.getRecipeOutput().copy());
        return output;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        for(Ingredient ingredient : recipe.getIngredients()) {
            List<ItemStack> ingredients = new LinkedList<>();
            for(ItemStack matchingStack : ingredient.getMatchingStacks()) {
                ingredients.add(matchingStack);
            }
            input.add(ingredients);
        }
        return input;
    }
}
