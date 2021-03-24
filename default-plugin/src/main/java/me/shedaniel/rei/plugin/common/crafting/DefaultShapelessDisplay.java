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

package me.shedaniel.rei.plugin.common.crafting;

import me.shedaniel.rei.api.common.ingredient.EntryIngredient;
import me.shedaniel.rei.api.common.ingredient.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DefaultShapelessDisplay extends DefaultCraftingDisplay {
    private ShapelessRecipe display;
    private List<EntryIngredient> input;
    private EntryIngredient output;
    
    public DefaultShapelessDisplay(ShapelessRecipe recipe) {
        this.display = recipe;
        this.input = EntryIngredients.ofIngredients(recipe.getIngredients());
        this.output = EntryIngredients.of(recipe.getResultItem());
    }
    
    @Override
    public Optional<Recipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(display);
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.ofNullable(display).map(ShapelessRecipe::getId);
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return input;
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public int getWidth() {
        return display.getIngredients().size() > 4 ? 3 : 2;
    }
    
    @Override
    public int getHeight() {
        return display.getIngredients().size() > 4 ? 3 : 2;
    }
}
