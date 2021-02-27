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

import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.util.EntryIngredients;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DefaultStoneCuttingDisplay implements Display {
    private List<EntryIngredient> inputs;
    private EntryIngredient output;
    private StonecutterRecipe display;
    
    public DefaultStoneCuttingDisplay(StonecutterRecipe recipe) {
        this(recipe.getIngredients(), recipe.getResultItem());
        this.display = recipe;
    }
    
    public DefaultStoneCuttingDisplay(NonNullList<Ingredient> ingredients, ItemStack output) {
        this.inputs = EntryIngredients.ofIngredients(ingredients);
        this.output = EntryIngredients.of(output);
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.ofNullable(display).map(SingleItemRecipe::getId);
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }
    
    @Override
    public List<EntryIngredient> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public ResourceLocation getCategoryIdentifier() {
        return DefaultPlugin.STONE_CUTTING;
    }
}
