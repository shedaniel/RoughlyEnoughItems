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

package me.shedaniel.rei.jeicompat.unwrap;

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIIngredientHelper<T> implements IIngredientHelper<T> {
    private final EntryDefinition<T> definition;
    
    public JEIIngredientHelper(EntryDefinition<T> definition) {
        this.definition = definition;
    }
    
    @Override
    public IIngredientType<T> getIngredientType() {
        return definition.jeiType();
    }
    
    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch, UidContext context) {
        ComparisonContext comparisonContext = context.unwrapContext();
        T value = ingredientToMatch.unwrapStack(definition).getValue();
        return CollectionUtils.findFirstOrNull(ingredients, t -> definition.equals(t.unwrapStack(definition).getValue(), value, comparisonContext));
    }
    
    @Override
    public String getDisplayName(T ingredient) {
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        return definition.asFormattedText(entry, entry.getValue(), TooltipContext.of()).getString();
    }
    
    @Override
    public String getUniqueId(T ingredient, UidContext context) {
        ComparisonContext comparisonContext = context.unwrapContext();
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        return String.valueOf(EntryStacks.hash(entry, comparisonContext));
    }
    
    @Override
    public String getModId(T ingredient) {
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        String ns = entry.getContainingNamespace();
        return ns == null ? "minecraft" : ns;
    }
    
    @Override
    public String getResourceId(T ingredient) {
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        ResourceLocation location = entry.getIdentifier();
        return location == null ? "unknown" : location.getPath();
    }
    
    @Override
    public T copyIngredient(T ingredient) {
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        return definition.copy(entry, entry.getValue());
    }
    
    @Override
    public String getErrorInfo(@Nullable T ingredient) {
        try {
            return Objects.toString(getResourceLocation(ingredient));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return throwable.getClass().getName() + ": " + throwable.getLocalizedMessage();
        }
    }
}
