package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipe;

import java.util.Arrays;
import java.util.List;

public class DefaultShapelessDisplay implements DefaultCraftingDisplay {
    
    private ShapelessRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultShapelessDisplay(ShapelessRecipe recipe) {
        this.display = recipe;
        this.input = Lists.newArrayList();
        recipe.getIngredients().forEach(ingredient -> {
            input.add(Arrays.asList(ingredient.getMatchingStacks()));
        });
        this.output = Arrays.asList(recipe.getRecipeOutput());
    }
    
    @Override
    public ShapelessRecipe getRecipe() {
        return display;
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return input;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return output;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return input;
    }
    
    @Override
    public int getWidth() {
        if (display.getIngredients().size() > 4)
            return 3;
        return 2;
    }
    
    @Override
    public int getHeight() {
        if (display.getIngredients().size() > 4)
            return 3;
        return 2;
    }
    
}
