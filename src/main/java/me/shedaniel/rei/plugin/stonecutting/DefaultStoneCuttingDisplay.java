/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.stonecutting;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultStoneCuttingDisplay implements RecipeDisplay<StonecuttingRecipe> {
    
    private List<List<ItemStack>> inputs;
    private List<ItemStack> output;
    private StonecuttingRecipe display;
    
    public DefaultStoneCuttingDisplay(StonecuttingRecipe recipe) {
        this(recipe.getPreviewInputs(), recipe.getOutput());
        this.display = recipe;
    }
    
    public DefaultStoneCuttingDisplay(DefaultedList<Ingredient> ingredients, ItemStack output) {
        this.inputs = ingredients.stream().map(i -> Arrays.asList(i.getStackArray())).collect(Collectors.toList());
        this.output = Collections.singletonList(output);
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(display).map(CuttingRecipe::getId);
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return inputs;
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return this.output;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.STONE_CUTTING;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return getInput();
    }
    
}
