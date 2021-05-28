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

package me.shedaniel.rei.jeicompat.unwrap;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public class JEIIngredientHelper<T> implements IIngredientHelper<T> {
    private final EntryDefinition<T> definition;
    
    public JEIIngredientHelper(EntryDefinition<T> definition) {
        this.definition = definition;
    }
    
    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        return getMatch(ingredients, ingredientToMatch, UidContext.Ingredient);
    }
    
    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch, UidContext context) {
        ComparisonContext comparisonContext = wrapContext(context);
        T value = wrap(definition, ingredientToMatch).getValue();
        return CollectionUtils.findFirstOrNull(ingredients, t -> definition.equals(wrap(definition, t).getValue(), value, comparisonContext));
    }
    
    @Override
    public String getDisplayName(T ingredient) {
        EntryStack<T> entry = wrap(definition, ingredient);
        return definition.asFormattedText(entry, entry.getValue()).getString();
    }
    
    @Override
    public String getUniqueId(T ingredient) {
        return getUniqueId(ingredient, UidContext.Ingredient);
    }
    
    @Override
    public String getUniqueId(T ingredient, UidContext context) {
        ComparisonContext comparisonContext = wrapContext(context);
        EntryStack<T> entry = wrap(definition, ingredient);
        return String.valueOf(EntryStacks.hash(entry, comparisonContext));
    }
    
    @Override
    public String getModId(T ingredient) {
        EntryStack<T> entry = wrap(definition, ingredient);
        ResourceLocation location = definition.getIdentifier(entry, entry.getValue());
        return location == null ? "minecraft" : location.getNamespace();
    }
    
    @Override
    public String getResourceId(T ingredient) {
        EntryStack<T> entry = wrap(definition, ingredient);
        ResourceLocation location = definition.getIdentifier(entry, entry.getValue());
        return location == null ? "minecraft:unknown" : location.toString();
    }
    
    @Override
    public T copyIngredient(T ingredient) {
        EntryStack<T> entry = wrap(definition, ingredient);
        return definition.copy(entry, entry.getValue());
    }
    
    @Override
    public String getErrorInfo(@Nullable T ingredient) {
        try {
            return getResourceId(ingredient);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return throwable.getClass().getName() + ": " + throwable.getLocalizedMessage();
        }
    }
}
