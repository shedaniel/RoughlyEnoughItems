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

package me.shedaniel.rei.plugin.campfire;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DefaultCampfireDisplay implements RecipeDisplay {
    
    private List<List<EntryStack>> inputs;
    private List<EntryStack> output;
    private int cookTime;
    private CampfireCookingRecipe display;
    
    public DefaultCampfireDisplay(CampfireCookingRecipe recipe) {
        this(recipe.getIngredients(), recipe.getResultItem(), recipe.getCookingTime());
        this.display = recipe;
    }
    
    public DefaultCampfireDisplay(NonNullList<Ingredient> ingredients, ItemStack output, int cookTime) {
        this.inputs = EntryStack.ofIngredients(ingredients);
        this.output = Collections.singletonList(EntryStack.create(output));
        this.cookTime = cookTime;
    }
    
    public double getCookTime() {
        return cookTime;
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getRecipeLocation() {
        return Optional.ofNullable(display).map(CampfireCookingRecipe::getId);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return inputs;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return inputs;
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.CAMPFIRE;
    }
    
}
