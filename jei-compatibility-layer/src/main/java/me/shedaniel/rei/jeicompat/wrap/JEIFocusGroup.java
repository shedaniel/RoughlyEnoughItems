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
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class JEIFocusGroup implements IFocusGroup {
    public static final IFocusGroup EMPTY = new JEIFocusGroup(Collections.emptyList());
    private List<IFocus<?>> foci;
    
    public JEIFocusGroup(List<IFocus<?>> foci) {
        this.foci = foci;
    }
    
    @Override
    public boolean isEmpty() {
        return foci.isEmpty();
    }
    
    @Override
    public List<IFocus<?>> getAllFocuses() {
        return Collections.unmodifiableList(foci);
    }
    
    @Override
    public Stream<IFocus<?>> getFocuses(RecipeIngredientRole role) {
        return foci.stream()
                .filter(focus -> focus.getRole() == role);
    }
    
    @Override
    public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType) {
        return foci.stream()
                .flatMap(focus -> {
                    if (focus instanceof IFocusGroup group) {
                        return group.getFocuses(ingredientType);
                    } else if (Objects.equals(focus.getTypedValue().getType(), ingredientType)) {
                        return Stream.of((IFocus<T>) focus);
                    } else {
                        return Stream.empty();
                    }
                });
    }
    
    @Override
    public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
        return getFocuses(ingredientType)
                .filter(focus -> focus.getRole() == role);
    }
}
