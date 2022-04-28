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

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.Optional;

public class JEITypedIngredient<T> implements ITypedIngredient<T> {
    private final IIngredientType<T> type;
    private final T ingredient;
    
    public JEITypedIngredient(IIngredientType<T> type, T ingredient) {
        this.type = type;
        this.ingredient = ingredient;
    }
    
    @Override
    public IIngredientType<T> getType() {
        return type;
    }
    
    @Override
    public T getIngredient() {
        return ingredient;
    }
    
    @Override
    public <V> Optional<V> getIngredient(IIngredientType<V> ingredientType) {
        if (ingredient == null) return Optional.empty();
        if (ingredientType.getIngredientClass().isInstance(ingredient)) {
            return Optional.ofNullable((V) ingredient);
        }
        return Optional.empty();
    }
}
