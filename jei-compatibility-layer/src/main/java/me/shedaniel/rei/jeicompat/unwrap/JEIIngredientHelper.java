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

package me.shedaniel.rei.jeicompat.unwrap;

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
        return CollectionUtils.findFirstOrNull(ingredients, t -> definition.equals(t, ingredientToMatch, comparisonContext));
    }
    
    @Override
    public String getDisplayName(T ingredient) {
        return definition.asFormattedText(wrap(definition, ingredient), ingredient).getString();
    }
    
    @Override
    public String getUniqueId(T ingredient) {
        return getUniqueId(ingredient, UidContext.Ingredient);
    }
    
    @Override
    public String getUniqueId(T ingredient, UidContext context) {
        ComparisonContext comparisonContext = wrapContext(context);
        return String.valueOf(EntryStacks.hash(wrap(definition, ingredient), comparisonContext));
    }
    
    @Override
    public String getModId(T ingredient) {
        ResourceLocation location = definition.getIdentifier(wrap(definition, ingredient), ingredient);
        return location == null ? "minecraft" : location.getNamespace();
    }
    
    @Override
    public String getResourceId(T ingredient) {
        ResourceLocation location = definition.getIdentifier(wrap(definition, ingredient), ingredient);
        return location == null ? "minecraft:unknown" : location.toString();
    }
    
    @Override
    public T copyIngredient(T ingredient) {
        return definition.copy(wrap(definition, ingredient), ingredient);
    }
    
    @Override
    public String getErrorInfo(@Nullable T ingredient) {
        throw TODO();
    }
}
