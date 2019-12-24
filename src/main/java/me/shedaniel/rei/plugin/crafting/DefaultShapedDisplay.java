/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.crafting;

import me.shedaniel.rei.api.EntryStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultShapedDisplay implements DefaultCraftingDisplay {

    private ShapedRecipe display;
    private List<List<EntryStack>> input;
    private List<EntryStack> output;

    public DefaultShapedDisplay(ShapedRecipe recipe) {
        this.display = recipe;
        this.input = recipe.getPreviewInputs().stream().map(i -> {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : i.getMatchingStacksClient()) {
                entries.add(EntryStack.create(stack));
            }
            return entries;
        }).collect(Collectors.toList());
        this.output = Collections.singletonList(EntryStack.create(recipe.getOutput()));
    }

    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(display).map(ShapedRecipe::getId);
    }

    @Override
    public List<List<EntryStack>> getInputEntries() {
        return input;
    }

    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }

    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return input;
    }

    @Override
    public int getHeight() {
        return display.getHeight();
    }

    @Override
    public Optional<Recipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(display);
    }

    @Override
    public int getWidth() {
        return display.getWidth();
    }

}
