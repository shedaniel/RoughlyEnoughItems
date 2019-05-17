/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultShapelessDisplay implements DefaultCraftingDisplay {
    
    private ShapelessRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultShapelessDisplay(ShapelessRecipe recipe) {
        this.display = recipe;
        this.input = Lists.newArrayList(recipe.getPreviewInputs().stream().map(i -> Lists.newArrayList(i.getStackArray())).collect(Collectors.toList()));
        this.output = Collections.singletonList(recipe.getOutput());
    }
    
    @Override
    public Optional<ShapelessRecipe> getRecipe() {
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
    public int getWidth() {
        if (display.getPreviewInputs().size() > 4)
            return 3;
        return 2;
    }
    
    @Override
    public int getHeight() {
        if (display.getPreviewInputs().size() > 4)
            return 3;
        return 2;
    }
    
}
