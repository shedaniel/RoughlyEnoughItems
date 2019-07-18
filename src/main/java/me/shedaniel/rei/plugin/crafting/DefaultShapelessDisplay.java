/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.crafting;

import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.Identifiers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;

import java.util.Arrays;
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
        this.input = recipe.getIngredients().stream().map(i -> Arrays.asList(i.getMatchingStacks())).collect(Collectors.toList());
        this.output = Collections.singletonList(recipe.getRecipeOutput());
    }
    
    @Override
    public Optional<IRecipe> getOptionalRecipe() {
        return Optional.ofNullable(display);
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(display).map(ShapelessRecipe::getId).map(Identifiers::of);
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
