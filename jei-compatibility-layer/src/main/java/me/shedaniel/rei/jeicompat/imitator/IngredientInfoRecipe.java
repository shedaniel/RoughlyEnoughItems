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

import me.shedaniel.rei.api.common.util.CollectionUtils;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Collections;
import java.util.List;

public class IngredientInfoRecipe<T> {
    private final List<Component> description;
    private final List<T> ingredients;
    private final IIngredientType<T> ingredientType;
    
    public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionComponents) {
        return create(ingredients, ingredientType, CollectionUtils.map(descriptionComponents, Component::literal).toArray(new Component[0]));
    }
    
    public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents) {
        return Collections.singletonList(new IngredientInfoRecipe<>(ingredients, ingredientType, CollectionUtils.map(descriptionComponents, Component::copy)));
    }
    
    private IngredientInfoRecipe(List<T> ingredients, IIngredientType<T> ingredientType, List<Component> description) {
        this.description = description;
        this.ingredients = ingredients;
        this.ingredientType = ingredientType;
    }
    
    public List<FormattedText> getDescription() {
        return CollectionUtils.map(description, Component::copy);
    }
    
    public List<Component> getDescriptionREI() {
        return description;
    }
    
    public IIngredientType<T> getIngredientType() {
        return ingredientType;
    }
    
    public List<T> getIngredients() {
        return ingredients;
    }
}