package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;

import java.util.Arrays;
import java.util.List;

public class DefaultShapedDisplay implements DefaultCraftingDisplay<ShapedRecipe> {
    
    private ShapedRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultShapedDisplay(ShapedRecipe recipe) {
        this.display = recipe;
        this.input = Lists.newArrayList();
        recipe.getIngredients().forEach(ingredient -> {
            input.add(Arrays.asList(ingredient.getMatchingStacks()));
        });
        this.output = Arrays.asList(recipe.getRecipeOutput());
    }
    
    @Override
    public ShapedRecipe getRecipe() {
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
    public int getHeight() {
        return display.getHeight();
    }
    
    @Override
    public int getWidth() {
        return display.getWidth();
    }
    
}
