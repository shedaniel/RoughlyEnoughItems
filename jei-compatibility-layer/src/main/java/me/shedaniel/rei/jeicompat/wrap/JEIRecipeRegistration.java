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

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public enum JEIRecipeRegistration implements IRecipeRegistration {
    INSTANCE;
    
    @Override
    @NotNull
    public IJeiHelpers getJeiHelpers() {
        return JEIJeiHelpers.INSTANCE;
    }
    
    @Override
    @NotNull
    public IIngredientManager getIngredientManager() {
        return JEIIngredientManager.INSTANCE;
    }
    
    @Override
    @NotNull
    public IVanillaRecipeFactory getVanillaRecipeFactory() {
        return JEIVanillaRecipeFactory.INSTANCE;
    }
    
    @Override
    public void addRecipes(@NotNull Collection<?> recipes, @NotNull ResourceLocation categoryId) {
        CategoryRegistry.CategoryConfiguration<Display> config = CategoryRegistry.getInstance().get(CategoryIdentifier.of(categoryId));
        DisplayCategory<?> category = config.getCategory();
        if (category instanceof JEIWrappedCategory) {
            for (Object recipe : recipes) {
                DisplayRegistry.getInstance().add(new JEIWrappedDisplay<>((JEIWrappedCategory) config.getCategory(), recipe));
            }
        } else {
            for (Object recipe : recipes) {
                Collection<Display> displays = createDisplayFrom(recipe);
                for (Display display : displays) {
                    DisplayRegistry.getInstance().add(display);
                }
            }
        }
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull T ingredient, @NotNull IIngredientType<T> ingredientType, @NotNull String @NotNull ... descriptionKeys) {
        EntryStack<T> stack = wrap(ingredientType, ingredient);
        BuiltinClientPlugin.getInstance().registerInformation(stack, stack.asFormattedText(), components -> {
            for (String key : descriptionKeys) {
                components.add(new TranslatableComponent(key));
            }
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull T ingredient, @NotNull IIngredientType<T> ingredientType, @NotNull Component @NotNull ... descriptionComponents) {
        EntryStack<T> stack = wrap(ingredientType, ingredient);
        BuiltinClientPlugin.getInstance().registerInformation(stack, stack.asFormattedText(), components -> {
            Collections.addAll(components, descriptionComponents);
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull List<T> ingredients, @NotNull IIngredientType<T> ingredientType, @NotNull String @NotNull ... descriptionKeys) {
        EntryIngredient ingredient = wrapList(ingredientType, ingredients);
        BuiltinClientPlugin.getInstance().registerInformation(ingredient, ImmutableTextComponent.EMPTY, components -> {
            for (String key : descriptionKeys) {
                components.add(new TranslatableComponent(key));
            }
            return components;
        });
    }
    
    @Override
    public <T> void addIngredientInfo(@NotNull List<T> ingredients, @NotNull IIngredientType<T> ingredientType, @NotNull Component @NotNull ... descriptionComponents) {
        EntryIngredient ingredient = wrapList(ingredientType, ingredients);
        BuiltinClientPlugin.getInstance().registerInformation(ingredient, ImmutableTextComponent.EMPTY, components -> {
            Collections.addAll(components, descriptionComponents);
            return components;
        });
    }
}
