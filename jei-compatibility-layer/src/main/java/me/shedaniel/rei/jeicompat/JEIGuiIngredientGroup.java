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

package me.shedaniel.rei.jeicompat;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JEIGuiIngredientGroup<T> implements IGuiIngredientGroup<T> {
    private final IIngredientType<T> type;
    
    public JEIGuiIngredientGroup(IIngredientType<T> type) {
        this.type = type;
    }
    
    @Override
    public void set(@NotNull IIngredients ingredients) {
        
    }
    
    @Override
    public void set(int slotIndex, @Nullable List<T> ingredients) {
        
    }
    
    @Override
    public void set(int slotIndex, @Nullable T ingredient) {
        
    }
    
    @Override
    public void setBackground(int slotIndex, @NotNull IDrawable background) {
        
    }
    
    @Override
    public void addTooltipCallback(@NotNull ITooltipCallback<T> tooltipCallback) {
        
    }
    
    @Override
    @NotNull
    public Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients() {
        return null;
    }
    
    @Override
    public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
        
    }
    
    @Override
    public void init(int slotIndex, boolean input, @NotNull IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xPadding, int yPadding) {
        
    }
    
    @Override
    public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
        
    }
}
