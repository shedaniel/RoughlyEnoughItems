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

import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DefaultShapedDisplay implements DefaultCraftingDisplay {
    private ShapedRecipe display;
    private List<? extends List<? extends EntryStack<?>>> input;
    private List<? extends EntryStack<?>> output;
    
    public DefaultShapedDisplay(ShapedRecipe recipe) {
        this.display = recipe;
        this.input = EntryStacks.ofIngredients(recipe.getIngredients());
        this.output = Collections.singletonList(EntryStacks.of(recipe.getResultItem()));
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getDisplayLocation() {
        return Optional.ofNullable(display).map(ShapedRecipe::getId);
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getInputEntries() {
        return input;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getResultingEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getRequiredEntries() {
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
