/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.smelting;

import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.Identifiers;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultSmeltingDisplay implements RecipeDisplay {
    
    private FurnaceRecipe display;
    private List<List<ItemStack>> input;
    private List<ItemStack> output;
    
    public DefaultSmeltingDisplay(FurnaceRecipe recipe) {
        this.display = recipe;
        this.input = recipe.getIngredients().stream().map(i -> Arrays.asList(i.getMatchingStacks())).collect(Collectors.toList());
        this.input.add(TileEntityFurnace.getBurnTimes().keySet().stream().map(Item::getDefaultInstance).collect(Collectors.toList()));
        this.output = Collections.singletonList(recipe.getRecipeOutput());
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(display).map(FurnaceRecipe::getId).map(Identifiers::of);
    }
    
    @Override
    public List<List<ItemStack>> getInput() {
        return input;
    }
    
    public List<ItemStack> getFuel() {
        return input.get(1);
    }
    
    @Override
    public List<ItemStack> getOutput() {
        return output;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMELTING;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return input;
    }
    
    public Optional<FurnaceRecipe> getOptionalRecipe() {
        return Optional.ofNullable(display);
    }
}
