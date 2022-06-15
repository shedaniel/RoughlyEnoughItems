/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.jeicompat.wrap;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The following method is licensed with The MIT License (MIT)
 * Copyright (c) 2014-2015 mezz
 * <p>
 * Full license text can be found in the https://github.com/mezz/JustEnoughItems/blob/1.17/LICENSE.txt
 */
public enum JEICraftingGridHelper implements ICraftingGridHelper {
    INSTANCE;
    
    @Override
    public <T> void setInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height) {
        List<IRecipeSlotBuilder> slotBuilders = new ArrayList<>();
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                slotBuilders.add(builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1));
            }
        }
        
        setInputs(slotBuilders, ingredientType, inputs, width, height);
    }
    
    @Override
    public <T> void setInputs(List<IRecipeSlotBuilder> slotBuilders, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height) {
        if (width <= 0 || height <= 0) {
            width = height = getShapelessSize(inputs.size());
        }
        if (slotBuilders.size() < width * height) {
            throw new IllegalArgumentException(String.format("There are not enough slots (%s) to hold a recipe of this size. (%sx%s)", slotBuilders.size(), width, height));
        }
        
        for (int i = 0; i < inputs.size(); i++) {
            int index = getCraftingIndex(i, width, height);
            IRecipeSlotBuilder slot = slotBuilders.get(index);
            
            @Nullable List<@Nullable T> ingredients = inputs.get(i);
            if (ingredients != null) {
                slot.addIngredients(ingredientType, ingredients);
            }
        }
    }
    
    @Override
    public <T> void setOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, @Nullable List<@Nullable T> outputs) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addIngredients(ingredientType, outputs);
    }
    
    private static int getShapelessSize(int total) {
        if (total > 4) {
            return 3;
        } else if (total > 1) {
            return 2;
        } else {
            return 1;
        }
    }
    
    private static int getCraftingIndex(int i, int width, int height) {
        int index;
        if (width == 1) {
            if (height == 3) {
                index = (i * 3) + 1;
            } else if (height == 2) {
                index = (i * 3) + 1;
            } else {
                index = 4;
            }
        } else if (height == 1) {
            index = i + 3;
        } else if (width == 2) {
            index = i;
            if (i > 1) {
                index++;
                if (i > 3) {
                    index++;
                }
            }
        } else if (height == 2) {
            index = i + 3;
        } else {
            index = i;
        }
        return index;
    }
}
