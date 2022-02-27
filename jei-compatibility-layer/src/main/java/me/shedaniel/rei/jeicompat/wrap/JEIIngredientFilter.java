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

import com.google.common.collect.ImmutableList;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ExtensionMethod(JEIPluginDetector.class)
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
        List<EntryStackProvider<?>> filteredStacks = ConfigObject.getInstance().getFilteredStackProviders();
        EntryType<T> type = ingredientType.unwrapType();
        T[] filtered = (T[]) Array.newInstance(ingredientType.getIngredientClass(), filteredStacks.size());
        int i = 0;
        for (EntryStackProvider<?> provider : filteredStacks) {
            EntryStack<?> stack = provider.provide();
            if (stack.getType() == type && !stack.isEmpty()) {
                filtered[i++] = stack.<T>cast().jeiValue();
            }
        }
        return ImmutableList.copyOf(Arrays.copyOf(filtered, i));
    }
    
    @Override
    public <V> boolean isIngredientVisible(V ingredient) {
        return isIngredientVisible(ingredient, null);
    }
    
    @Override
    public <V> boolean isIngredientVisible(V ingredient, @Nullable IIngredientHelper<V> ingredientHelper) {
        EntryStack<?> stack = ingredientHelper == null ? ingredient.unwrapStack() : ingredient.unwrapStack(ingredientHelper.getIngredientType().unwrapDefinition());
        EntryRegistry registry = EntryRegistry.getInstance();
        if (!registry.alreadyContain(stack)) {
            return false;
        }
        Collection<EntryStack<?>> stacks = registry.refilterNew(false, Collections.singletonList(stack));
        return !stacks.isEmpty();
    }
}
