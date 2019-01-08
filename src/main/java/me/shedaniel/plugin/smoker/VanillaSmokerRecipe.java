package me.shedaniel.plugin.smoker;

import me.shedaniel.api.IRecipe;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.smelting.SmokingRecipe;

import java.util.*;
import java.util.stream.Collectors;

public class VanillaSmokerRecipe implements IRecipe<ItemStack> {
    private final SmokingRecipe recipe;
    
    @Override
    public String getId() {
        return "smoker";
    }
    
    public VanillaSmokerRecipe(SmokingRecipe recipe) {
        this.recipe = recipe;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        List<ItemStack> output = new LinkedList<>();
        output.add(recipe.getOutput().copy());
        return output;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        for(Ingredient ingredient : recipe.getPreviewInputs()) {
            List<ItemStack> ingredients = Arrays.asList(ingredient.getStackArray());
            input.add(ingredients);
        }
        input.add(SmokerBlockEntity.createBurnableMap().keySet().stream().map(Item::getDefaultStack).collect(Collectors.toList()));
        return input;
    }
    
    @Override
    public List<List<ItemStack>> getRecipeRequiredInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        for(Ingredient ingredient : recipe.getPreviewInputs())
            Collections.addAll(input, new LinkedList<>(Arrays.asList(ingredient.getStackArray())));
        return input;
    }
    
    public SmokingRecipe getRecipe() {
        return recipe;
    }
    
}
