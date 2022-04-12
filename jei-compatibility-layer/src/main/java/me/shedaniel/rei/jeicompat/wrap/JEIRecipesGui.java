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

import lombok.experimental.ExtensionMethod;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEIRecipesGui implements IRecipesGui {
    INSTANCE;
    
    @Override
    public <V> void show(IFocus<V> focus) {
        ViewSearchBuilder builder = ViewSearchBuilder.builder();
        if (focus != null) {
            EntryStack<?> stack = focus.getValue().unwrapStack();
            
            if (stack != null && !stack.isEmpty()) {
                if (focus.getMode() == IFocus.Mode.INPUT) {
                    builder.addUsagesFor(stack);
                } else {
                    builder.addRecipesFor(stack);
                }
            }
        }
        builder.open();
    }
    
    @Override
    public void show(List<IFocus<?>> focuses) {
        ViewSearchBuilder builder = ViewSearchBuilder.builder();
        for (IFocus<?> focus : focuses) {
            EntryStack<?> stack = focus.getTypedValue().unwrapStack();
            if (focus.getMode() == IFocus.Mode.INPUT) {
                builder.addUsagesFor(stack);
            } else {
                builder.addRecipesFor(stack);
            }
        }
        builder.open();
    }
    
    @Override
    public void showTypes(List<RecipeType<?>> recipeTypes) {
        ViewSearchBuilder.builder().addCategories(CollectionUtils.map(recipeTypes, JEIPluginDetector::categoryId)).open();
    }
    
    @Override
    public void showCategories(List<ResourceLocation> recipeCategoryUids) {
        ViewSearchBuilder.builder().addCategories(CollectionUtils.map(recipeCategoryUids, CategoryIdentifier::of)).open();
    }
    
    @Override
    @Nullable
    public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
        T ingredient = JEIJeiRuntime.INSTANCE.getIngredientListOverlay().getIngredientUnderMouse(ingredientType);
        if (ingredient != null) return ingredient;
        ingredient = JEIJeiRuntime.INSTANCE.getBookmarkOverlay().getIngredientUnderMouse(ingredientType);
        if (ingredient != null) return ingredient;
        EntryStack<?> focusedStack = ScreenRegistry.getInstance().getFocusedStack(Minecraft.getInstance().screen, PointHelper.ofMouse());
        if (focusedStack == null) return null;
        if (focusedStack.getType() != ingredientType.unwrapType()) return null;
        return focusedStack.<T>cast().jeiValue();
    }
}
