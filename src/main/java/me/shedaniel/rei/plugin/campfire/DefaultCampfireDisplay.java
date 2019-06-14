/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.campfire;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultCampfireDisplay implements RecipeDisplay<CampfireCookingRecipe> {
    
    private List<List<ItemStack>> inputs;
    private List<ItemStack> output;
    private int cookTime;
    private CampfireCookingRecipe display;
    
    public DefaultCampfireDisplay(CampfireCookingRecipe recipe) {
        this(recipe.getPreviewInputs(), recipe.getOutput(), recipe.getCookTime());
        this.display = recipe;
    }
    
    public DefaultCampfireDisplay(DefaultedList<Ingredient> ingredients, ItemStack output, int cookTime) {
        this.inputs = ingredients.stream().map(i -> Arrays.asList(i.getStackArray())).collect(Collectors.toList());
        this.output = Collections.singletonList(output);
        this.cookTime = cookTime;
    }
    
    public int getCookTime() {
        return cookTime;
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(display).map(AbstractCookingRecipe::getId);
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
        return DefaultPlugin.CAMPFIRE;
    }
    
    @Override
    public List<List<ItemStack>> getRequiredItems() {
        return getInput();
    }
    
}
