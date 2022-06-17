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
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeCatalystLookup;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtensionMethod(JEIPluginDetector.class)
public class JEICatalystLookup implements IRecipeCatalystLookup {
    private final CategoryIdentifier<?> category;
    private boolean includeHidden = false;
    
    public JEICatalystLookup(CategoryIdentifier<?> category) {
        this.category = category;
    }
    
    @Override
    public IRecipeCatalystLookup includeHidden() {
        this.includeHidden = true;
        return this;
    }
    
    @Override
    public Stream<ITypedIngredient<?>> get() {
        Stream<EntryStack<?>> stream = CategoryRegistry.getInstance().get(category)
                .getWorkstations()
                .stream()
                .flatMap(Collection::stream);
        if (includeHidden) return stream.map(entry -> entry.typedJeiValue());
        EntryRegistry registry = EntryRegistry.getInstance();
        return EntryRegistry.getInstance().refilterNew(false, stream.collect(Collectors.toList()))
                .stream()
                .map(entry -> entry.typedJeiValue());
    }
    
    @Override
    public <S> Stream<S> get(IIngredientType<S> ingredientType) {
        return get().map(entry -> entry.getIngredient(ingredientType)).flatMap(Optional::stream);
    }
}
