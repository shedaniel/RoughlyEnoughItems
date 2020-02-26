/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
