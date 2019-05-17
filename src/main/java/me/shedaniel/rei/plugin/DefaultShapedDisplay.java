/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultShapedDisplay implements DefaultCraftingDisplay<ShapedRecipe> {
    
    private ShapedRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultShapedDisplay(ShapedRecipe recipe) {
        this.display = recipe;
        this.input = Lists.newArrayList(recipe.getPreviewInputs().stream().map(i -> Lists.newArrayList(i.getStackArray())).collect(Collectors.toList()));
        this.output = Collections.singletonList(recipe.getOutput());
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
