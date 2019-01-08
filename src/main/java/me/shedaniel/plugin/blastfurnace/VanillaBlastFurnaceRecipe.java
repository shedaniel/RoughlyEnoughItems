package me.shedaniel.plugin.blastfurnace;

import me.shedaniel.api.IRecipe;
import net.minecraft.block.BlastFurnaceBlock;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.smelting.BlastingRecipe;
import net.minecraft.recipe.smelting.SmokingRecipe;

import java.util.*;
import java.util.stream.Collectors;

public class VanillaBlastFurnaceRecipe implements IRecipe<ItemStack> {
    private final BlastingRecipe recipe;
    
    @Override
    public String getId() {
        return "blastingfurnace";
    }
    
    public VanillaBlastFurnaceRecipe(BlastingRecipe recipe) {
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
        input.add(BlastFurnaceBlockEntity.createBurnableMap().keySet().stream().map(Item::getDefaultStack).collect(Collectors.toList()));
        return input;
    }
    
    @Override
    public List<List<ItemStack>> getRecipeRequiredInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        for(Ingredient ingredient : recipe.getPreviewInputs())
            Collections.addAll(input, new LinkedList<>(Arrays.asList(ingredient.getStackArray())));
        return input;
    }
    
    public BlastingRecipe getRecipe() {
        return recipe;
    }
    
}
