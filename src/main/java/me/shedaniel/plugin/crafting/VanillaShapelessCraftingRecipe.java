package me.shedaniel.plugin.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.crafting.ShapelessRecipe;

import java.util.LinkedList;
import java.util.List;

public class VanillaShapelessCraftingRecipe extends VanillaCraftingRecipe {
    
    private final ShapelessRecipe recipe;
    
    public VanillaShapelessCraftingRecipe(ShapelessRecipe recipe) {
        
        this.recipe = recipe;
    }
    
    @Override
    public String getId() {
        return "vanilla";
    }
    
    @Override
    public List<ItemStack> getOutput() {
        List<ItemStack> output = new LinkedList<>();
        output.add(recipe.getOutput());
        return output;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        List<List<ItemStack>> input = new LinkedList<>();
        for(Ingredient ingredient : recipe.getPreviewInputs()) {
            List<ItemStack> ingList = new LinkedList<>();
            for(ItemStack matchingStack : ingredient.getStackArray()) {
                ingList.add(matchingStack);
            }
            input.add(ingList);
        }
        return input;
    }
    
    @Override
    public int getWidth() {
        if (recipe.getPreviewInputs().size() > 4)
            return 3;
        return 2;
    }
    
    @Override
    public int getHeight() {
        if (recipe.getPreviewInputs().size() > 4)
            return 3;
        return 2;
    }
}
