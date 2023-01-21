/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.plugin.common.displays.crafting;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DefaultCustomShapedDisplay extends DefaultCraftingDisplay<Recipe<?>> {
    private int width;
    private int height;
    
    public DefaultCustomShapedDisplay(@Nullable Recipe<?> possibleRecipe, List<EntryIngredient> input, List<EntryIngredient> output, int width, int height) {
        this(null, possibleRecipe, input, output, width, height);
    }
    
    public DefaultCustomShapedDisplay(@Nullable ResourceLocation location, @Nullable Recipe<?> possibleRecipe, List<EntryIngredient> input, List<EntryIngredient> output, int width, int height) {
        super(input, output, Optional.ofNullable(location == null && possibleRecipe != null ? possibleRecipe.getId() : location), Optional.ofNullable(possibleRecipe));
        this.width = width;
        this.height = height;
    }
    
    public static DefaultCustomShapedDisplay simple(List<EntryIngredient> input, List<EntryIngredient> output, int width, int height, Optional<ResourceLocation> location) {
        Recipe<?> optionalRecipe = location.flatMap(resourceLocation -> RecipeManagerContext.getInstance().getRecipeManager().byKey(resourceLocation))
                .orElse(null);
        return new DefaultCustomShapedDisplay(location.orElse(null), optionalRecipe, input, output, width, height);
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
}
