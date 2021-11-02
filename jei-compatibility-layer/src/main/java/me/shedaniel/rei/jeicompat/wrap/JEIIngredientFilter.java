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

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.unwrap;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapEntryType;

public enum JEIIngredientFilter implements IIngredientFilter {
    INSTANCE;
    
    @Override
    public void setFilterText(@NotNull String filterText) {
        REIRuntime.getInstance().getSearchTextField().setText(filterText);
    }
    
    @Override
    @NotNull
    public String getFilterText() {
        return REIRuntime.getInstance().getSearchTextField().getText();
    }
    
    @Override
    public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
        List<EntryStack<?>> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
        EntryType<T> type = wrapEntryType(ingredientType);
        T[] filtered = (T[]) Array.newInstance(ingredientType.getIngredientClass(), filteredStacks.size());
        int i = 0;
        for (EntryStack<?> stack : filteredStacks) {
            if (stack.getType() == type) {
                filtered[i++] = unwrap(stack.cast());
            }
        }
        return ImmutableList.copyOf(Arrays.copyOf(filtered, i));
    }
}
