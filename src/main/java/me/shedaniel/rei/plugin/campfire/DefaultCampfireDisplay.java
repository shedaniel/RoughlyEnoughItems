/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.campfire;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultCampfireDisplay implements RecipeDisplay {
    
    private List<List<EntryStack>> inputs;
    private List<EntryStack> output;
    private int cookTime;
    private CampfireCookingRecipe display;
    
    public DefaultCampfireDisplay(CampfireCookingRecipe recipe) {
        this(recipe.getPreviewInputs(), recipe.getOutput(), recipe.getCookTime());
        this.display = recipe;
    }
    
    public DefaultCampfireDisplay(DefaultedList<Ingredient> ingredients, ItemStack output, int cookTime) {
        this.inputs = ingredients.stream().map(i -> {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : i.getMatchingStacksClient()) {
                entries.add(EntryStack.create(stack));
            }
            return entries;
        }).collect(Collectors.toList());
        this.output = Collections.singletonList(EntryStack.create(output));
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
    public List<List<EntryStack>> getInputEntries() {
        return inputs;
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return inputs;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.CAMPFIRE;
    }
    
}
