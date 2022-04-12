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
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class JEIFocus<T> implements IFocus<T>, IFocusGroup {
    private final RecipeIngredientRole role;
    private final ITypedIngredient<T> value;
    
    public JEIFocus(IFocus<T> focus) {
        this(focus.getRole(), focus.getTypedValue());
    }
    
    public JEIFocus(RecipeIngredientRole role, ITypedIngredient<T> value) {
        this.role = role;
        this.value = value;
    }
    
    @Override
    public RecipeIngredientRole getRole() {
        return role;
    }
    
    @Override
    @NotNull
    public Mode getMode() {
        return switch (role) {
            case INPUT, CATALYST -> IFocus.Mode.INPUT;
            case OUTPUT, RENDER_ONLY -> IFocus.Mode.OUTPUT;
        };
    }
    
    @Override
    public ITypedIngredient<T> getTypedValue() {
        return value;
    }
    
    @Override
    public <T1> Optional<IFocus<T1>> checkedCast(IIngredientType<T1> ingredientType) {
        if (Objects.equals(value.getType(), ingredientType)) {
            return Optional.of((IFocus<T1>) this);
        } else {
            return Optional.empty();
        }
    }
    
    public <R> JEIFocus<R> wrap() {
        return (JEIFocus<R>) this;
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public List<IFocus<?>> getAllFocuses() {
        return Collections.singletonList(this);
    }
    
    @Override
    public Stream<IFocus<?>> getFocuses(RecipeIngredientRole role) {
        if (getRole() == role) {
            return Stream.of(this);
        } else {
            return Stream.empty();
        }
    }
    
    @Override
    public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType) {
        if (Objects.equals(value.getType(), ingredientType)) {
            return Stream.of((IFocus<T>) this);
        } else {
            return Stream.empty();
        }
    }
    
    @Override
    public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
        if (Objects.equals(value.getType(), ingredientType) && getRole() == role) {
            return Stream.of((IFocus<T>) this);
        } else {
            return Stream.empty();
        }
    }
}
