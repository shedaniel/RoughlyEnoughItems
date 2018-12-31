package me.shedaniel.plugin.furnace;

import me.shedaniel.api.IRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
            List<ItemStack> ingredients = Arrays.asList(ingredient.getMatchingStacks());
            input.add(ingredients);
        }
        input.add(TileEntityFurnace.getBurnTimes().keySet().stream().map(Item::getDefaultInstance).collect(Collectors.toList()));
        return input;
    }
}
