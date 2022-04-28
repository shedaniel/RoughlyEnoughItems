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

package me.shedaniel.rei.jeicompat.imitator;

import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * The following method is licensed with The MIT License (MIT)
 * Copyright (c) 2014-2015 mezz
 * <p>
 * Full license text can be found in the https://github.com/mezz/JustEnoughItems/blob/1.17/LICENSE.txt
 */
public final class CategoryRecipeValidator<T extends Recipe<?>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int INVALID_COUNT = -1;
    private final IRecipeCategory<T> recipeCategory;
    private final int maxInputs;
    
    public CategoryRecipeValidator(IRecipeCategory<T> recipeCategory, int maxInputs) {
        this.recipeCategory = recipeCategory;
        this.maxInputs = maxInputs;
    }
    
    public boolean isRecipeValid(T recipe) {
        return hasValidInputsAndOutputs(recipe);
    }
    
    public boolean isRecipeHandled(T recipe) {
        return this.recipeCategory.isHandled(recipe);
    }
    
    @SuppressWarnings("ConstantConditions")
    private boolean hasValidInputsAndOutputs(T recipe) {
        if (recipe.isSpecial()) {
            return true;
        }
        ItemStack recipeOutput = recipe.getResultItem();
        if (recipeOutput == null || recipeOutput.isEmpty()) {
            LOGGER.error("Recipe has no output. {}", recipe);
            return false;
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients == null) {
            LOGGER.error("Recipe has no input Ingredients. {}", recipe);
            return false;
        }
        int inputCount = getInputCount(ingredients);
        if (inputCount == INVALID_COUNT) {
            return false;
        } else if (inputCount > maxInputs) {
            LOGGER.error("Recipe has too many inputs. {}", recipe);
            return false;
        } else if (inputCount == 0 && maxInputs > 0) {
            LOGGER.error("Recipe has no inputs. {}", recipe);
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("ConstantConditions")
    private static int getInputCount(List<Ingredient> ingredientList) {
        int inputCount = 0;
        for (Ingredient ingredient : ingredientList) {
            ItemStack[] input = ingredient.getItems();
            if (input == null) {
                return INVALID_COUNT;
            } else {
                inputCount++;
            }
        }
        return inputCount;
    }
}