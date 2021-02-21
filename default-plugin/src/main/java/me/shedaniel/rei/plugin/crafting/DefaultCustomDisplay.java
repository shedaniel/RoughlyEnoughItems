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

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryIngredients;
import me.shedaniel.rei.api.util.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DefaultCustomDisplay implements DefaultCraftingDisplay {
    private List<EntryIngredient> input;
    private EntryIngredient output;
    private Recipe<?> possibleRecipe;
    private int width, height;
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output, Recipe<?> possibleRecipe) {
        this(possibleRecipe, CollectionUtils.map(input, EntryIngredients::ofItemStacks), EntryIngredients.ofItemStacks(output));
    }
    
    public DefaultCustomDisplay(Recipe<?> possibleRecipe, List<EntryIngredient> input, EntryIngredient output) {
        this.input = ImmutableList.copyOf(input);
        this.output = output;
        this.possibleRecipe = possibleRecipe;
        BitSet row = new BitSet(3);
        BitSet column = new BitSet(3);
        for (int i = 0; i < 9; i++)
            if (i < this.input.size()) {
                List<? extends EntryStack<?>> stacks = this.input.get(i);
                if (stacks.stream().anyMatch(stack -> !stack.isEmpty())) {
                    row.set((i - (i % 3)) / 3);
                    column.set(i % 3);
                }
            }
        this.width = row.cardinality();
        this.height = column.cardinality();
    }
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output) {
        this(input, output, null);
    }
    
    protected Optional<Recipe<?>> getRecipe() {
        return Optional.ofNullable(possibleRecipe);
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getDisplayLocation() {
        return getRecipe().map(Recipe::getId);
    }
    
    @Override
    public @NotNull List<EntryIngredient> getInputEntries() {
        return input;
    }
    
    @Override
    public @NotNull List<EntryIngredient> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public Optional<Recipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(possibleRecipe);
    }
    
}
