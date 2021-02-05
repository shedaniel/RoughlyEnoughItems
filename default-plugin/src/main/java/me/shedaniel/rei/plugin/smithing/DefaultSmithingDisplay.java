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

package me.shedaniel.rei.plugin.smithing;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.Display;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DefaultSmithingDisplay implements Display {
    @NotNull
    private List<? extends List<? extends EntryStack<?>>> input;
    @NotNull
    private List<? extends List<? extends EntryStack<?>>> output;
    @Nullable
    private ResourceLocation location;
    
    public DefaultSmithingDisplay(@NotNull UpgradeRecipe recipe) {
        this(
                Lists.newArrayList(
                        EntryStacks.ofIngredient(recipe.base),
                        EntryStacks.ofIngredient(recipe.addition)
                ),
                Collections.singletonList(EntryStacks.of(recipe.getResultItem())),
                recipe.getId()
        );
    }
    
    public DefaultSmithingDisplay(@NotNull List<? extends List<? extends EntryStack<?>>> input, @NotNull List<EntryStack<?>> output, @Nullable ResourceLocation location) {
        this.input = input;
        if (this.input.size() != 2) throw new IllegalArgumentException("input must have 2 entries.");
        this.output = Collections.singletonList(output);
        this.location = location;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getInputEntries() {
        return input;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getResultingEntries() {
        return output;
    }
    
    @Override
    public @NotNull List<? extends List<? extends EntryStack<?>>> getRequiredEntries() {
        return getInputEntries();
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.SMITHING;
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getRecipeLocation() {
        return Optional.ofNullable(location);
    }
}
