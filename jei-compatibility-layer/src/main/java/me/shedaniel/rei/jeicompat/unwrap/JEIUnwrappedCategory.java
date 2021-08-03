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

package me.shedaniel.rei.jeicompat.unwrap;

import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.WILL_NOT_BE_IMPLEMENTED;

public class JEIUnwrappedCategory<T extends Display> implements IRecipeCategory<T> {
    private final DisplayCategory<T> backingCategory;
    
    public JEIUnwrappedCategory(DisplayCategory<T> backingCategory) {
        this.backingCategory = backingCategory;
    }
    
    @Override
    @NotNull
    public ResourceLocation getUid() {
        return backingCategory.getIdentifier();
    }
    
    @Override
    @NotNull
    public Class<? extends T> getRecipeClass() {
        return (Class<? extends T>) Display.class;
    }
    
    @Override
    @NotNull
    public Component getTitle() {
        return backingCategory.getTitle();
    }
    
    @Override
    @NotNull
    public IDrawable getBackground() {
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    @Override
    @NotNull
    public IDrawable getIcon() {
        throw TODO();
    }
    
    @Override
    public void setIngredients(@NotNull T recipe, @NotNull IIngredients ingredients) {
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull T recipe, @NotNull IIngredients ingredients) {
        throw WILL_NOT_BE_IMPLEMENTED();
    }
}
