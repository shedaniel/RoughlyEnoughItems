/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum JEICraftingGridHelper implements ICraftingGridHelper {
    INSTANCE;
    
    @Override
    public <T> void setInputs(@NotNull IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs) {
        int width = inputs.size() > 4 ? 3 : 2;
        for (int i = 0; i < inputs.size(); i++) {
            List<T> stacks = inputs.get(i);
            ingredientGroup.set(DefaultCraftingDisplay.getSlotWithSize(width, i, 3), stacks);
        }
    }
    
    @Override
    public <T> void setInputs(@NotNull IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height) {
        for (int i = 0; i < inputs.size(); i++) {
            List<T> stacks = inputs.get(i);
            ingredientGroup.set(DefaultCraftingDisplay.getSlotWithSize(width, i, 3), stacks);
        }
    }
}
