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

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import org.jetbrains.annotations.NotNull;

public class JEIFocus<T> implements IFocus<T> {
    private final Mode mode;
    private final T value;
    
    public JEIFocus(IFocus<T> focus) {
        this(focus.getMode(), focus.getValue());
    }
    
    public JEIFocus(Mode mode, T value) {
        this.mode = mode;
        this.value = value;
    }
    
    public static <T> IFocus<T> cast(IFocus<?> focus, IIngredientType<T> type) {
        if (focus != null) {
            if (type.getIngredientClass().isInstance(focus.getValue())) {
                return (IFocus<T>) focus;
            }
        }
        
        return null;
    }
    
    @Override
    @NotNull
    public Mode getMode() {
        return mode;
    }
    
    @Override
    @NotNull
    public T getValue() {
        return value;
    }
    
    public <R> JEIFocus<R> wrap() {
        return (JEIFocus<R>) this;
    }
}
