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

import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.*;
import org.jetbrains.annotations.NotNull;

public enum JEIJeiRuntime implements IJeiRuntime {
    INSTANCE;
    
    @Override
    @NotNull
    public IRecipeManager getRecipeManager() {
        return JEIRecipeManager.INSTANCE;
    }
    
    @Override
    @NotNull
    public IRecipesGui getRecipesGui() {
        return JEIRecipesGui.INSTANCE;
    }
    
    @Override
    @NotNull
    public IIngredientFilter getIngredientFilter() {
        return JEIIngredientFilter.INSTANCE;
    }
    
    @Override
    @NotNull
    public IIngredientListOverlay getIngredientListOverlay() {
        return JEIIngredientListOverlay.INSTANCE;
    }
    
    @Override
    @NotNull
    public IBookmarkOverlay getBookmarkOverlay() {
        return JEIBookmarkOverlay.INSTANCE;
    }
    
    @Override
    @NotNull
    public IIngredientManager getIngredientManager() {
        return JEIIngredientManager.INSTANCE;
    }
}
