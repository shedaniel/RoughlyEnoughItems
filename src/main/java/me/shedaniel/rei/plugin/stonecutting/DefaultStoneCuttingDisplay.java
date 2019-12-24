/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.stonecutting;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultStoneCuttingDisplay implements RecipeDisplay {

    private List<List<EntryStack>> inputs;
    private List<EntryStack> output;
    private StonecuttingRecipe display;

    public DefaultStoneCuttingDisplay(StonecuttingRecipe recipe) {
        this(recipe.getPreviewInputs(), recipe.getOutput());
        this.display = recipe;
    }

    public DefaultStoneCuttingDisplay(DefaultedList<Ingredient> ingredients, ItemStack output) {
        this.inputs = ingredients.stream().map(i -> {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : i.getMatchingStacksClient()) {
                entries.add(EntryStack.create(stack));
            }
            return entries;
        }).collect(Collectors.toList());
        this.output = Collections.singletonList(EntryStack.create(output));
    }

    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(display).map(CuttingRecipe::getId);
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
    public Identifier getRecipeCategory() {
        return DefaultPlugin.STONE_CUTTING;
    }

    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }
}
