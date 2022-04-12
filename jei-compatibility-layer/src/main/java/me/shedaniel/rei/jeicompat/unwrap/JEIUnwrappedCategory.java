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

package me.shedaniel.rei.jeicompat.unwrap;

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.wrap.JEIWrappedCategory;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.WILL_NOT_BE_IMPLEMENTED;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIUnwrappedCategory<T, D extends Display> implements IRecipeCategory<T> {
    private final DisplayCategory<D> backingCategory;
    
    public JEIUnwrappedCategory(DisplayCategory<D> backingCategory) {
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
        if (backingCategory instanceof JEIWrappedCategory) {
            return ((JEIWrappedCategory<T>) backingCategory).getBackingCategory().getRecipeClass();
        }
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
        if (backingCategory instanceof JEIWrappedCategory) {
            return ((JEIWrappedCategory<?>) backingCategory).getBackingCategory().getBackground();
        }
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    @Override
    @NotNull
    public IDrawable getIcon() {
        if (backingCategory instanceof JEIWrappedCategory) {
            return ((JEIWrappedCategory<?>) backingCategory).getBackingCategory().getIcon();
        }
        throw TODO();
    }
    
    @Override
    public void setIngredients(@NotNull T recipe, @NotNull IIngredients ingredients) {
        if (backingCategory instanceof JEIWrappedCategory) {
            ((JEIWrappedCategory<T>) backingCategory).getBackingCategory().setIngredients(recipe, ingredients);
            return;
        }
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        if (backingCategory instanceof JEIWrappedCategory) {
            ((JEIWrappedCategory<T>) backingCategory).getBackingCategory().setRecipe(builder, recipe, focuses);
            return;
        }
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, List<? extends IFocus<?>> focuses) {
        if (backingCategory instanceof JEIWrappedCategory) {
            ((JEIWrappedCategory<T>) backingCategory).getBackingCategory().setRecipe(builder, recipe, focuses);
            return;
        }
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull T recipe, @NotNull IIngredients ingredients) {
        if (backingCategory instanceof JEIWrappedCategory) {
            ((JEIWrappedCategory<T>) backingCategory).getBackingCategory().setRecipe(recipeLayout, recipe, ingredients);
            return;
        }
        throw WILL_NOT_BE_IMPLEMENTED();
    }
    
    public DisplayCategory<D> getBackingCategory() {
        return backingCategory;
    }
}
