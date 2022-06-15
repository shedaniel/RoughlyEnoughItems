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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.jeicompat.unwrap.JEIIngredientHelper;
import me.shedaniel.rei.jeicompat.unwrap.JEIIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEIIngredientManager implements IIngredientManager {
    INSTANCE;
    
    @Override
    @NotNull
    public <V> Collection<V> getAllIngredients(@NotNull IIngredientType<V> ingredientType) {
        EntryType<V> definition = ingredientType.unwrapType();
        return EntryRegistry.getInstance().getEntryStacks()
                .filter(stack -> Objects.equals(stack.getType(), definition))
                .<EntryStack<V>>map(EntryStack::cast)
                .map(JEIPluginDetector::jeiValue)
                .collect(Collectors.toList());
    }
    
    @Override
    @NotNull
    public <V> IIngredientHelper<V> getIngredientHelper(@NotNull V ingredient) {
        return new JEIIngredientHelper<>(ingredient.unwrapDefinition().cast());
    }
    
    @Override
    @NotNull
    public <V> IIngredientHelper<V> getIngredientHelper(@NotNull IIngredientType<V> ingredientType) {
        return new JEIIngredientHelper<>(ingredientType.unwrapDefinition().cast());
    }
    
    @Override
    @NotNull
    public <V> IIngredientRenderer<V> getIngredientRenderer(@NotNull V ingredient) {
        return getIngredientRenderer(ingredient.unwrapDefinition().<V>cast().jeiType());
    }
    
    @Override
    @NotNull
    public <V> IIngredientRenderer<V> getIngredientRenderer(@NotNull IIngredientType<V> ingredientType) {
        return new JEIIngredientRenderer<>(ingredientType, ingredientType.unwrapDefinition().<V>cast().getRenderer());
    }
    
    @Override
    @NotNull
    public Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
        return CollectionUtils.map(EntryTypeRegistry.getInstance().values(), definition -> definition.jeiType());
    }
    
    @Override
    public <V> void addIngredientsAtRuntime(@NotNull IIngredientType<V> ingredientType, @NotNull Collection<V> ingredients) {
        EntryRegistry.getInstance().addEntries(CollectionUtils.map(ingredients, v -> v.unwrapStack(ingredientType)));
    }
    
    @Override
    public <V> void removeIngredientsAtRuntime(@NotNull IIngredientType<V> ingredientType, @NotNull Collection<V> ingredients) {
        LongSet hash = new LongOpenHashSet();
        for (V ingredient : ingredients) {
            hash.add(EntryStacks.hashExact(ingredient.unwrapStack(ingredientType)));
        }
        EntryRegistry.getInstance().removeEntryExactHashIf(hash::contains);
    }
    
    @Override
    @NotNull
    public <V> IIngredientType<V> getIngredientType(@NotNull V ingredient) {
        return ingredient.unwrapStack().getDefinition().<V>cast().jeiType();
    }
    
    @Override
    @NotNull
    public <V> IIngredientType<V> getIngredientType(@NotNull Class<? extends V> ingredientClass) {
        return (IIngredientType<V>) ingredientClass.jeiType();
    }
}
