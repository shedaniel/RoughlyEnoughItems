package me.shedaniel.plugin.blastfurnace;

import me.shedaniel.api.IRecipe;
import net.minecraft.block.BlastFurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.smelting.BlastingRecipe;
import net.minecraft.recipe.smelting.SmokingRecipe;

import java.util.LinkedList;
import java.util.List;

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
            List<ItemStack> ingredients = new LinkedList<>();
            for(ItemStack matchingStack : ingredient.getStackArray()) {
                ingredients.add(matchingStack);
            }
            input.add(ingredients);
        }
        return input;
    }
}
