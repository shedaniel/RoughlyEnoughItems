package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapedRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DefaultShapedDisplay implements DefaultCraftingDisplay<ShapedRecipe> {
    
    private ShapedRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultShapedDisplay(ShapedRecipe recipe) {
        this.display = recipe;
        this.input = Lists.newArrayList();
        recipe.getPreviewInputs().forEach(ingredient -> {
            input.add(Arrays.asList(ingredient.getStackArray()));
        });
        this.output = Arrays.asList(recipe.getOutput());
    }
    
    @Override
    public Optional<Recipe> getRecipe() {
        return Optional.ofNullable(display);
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
