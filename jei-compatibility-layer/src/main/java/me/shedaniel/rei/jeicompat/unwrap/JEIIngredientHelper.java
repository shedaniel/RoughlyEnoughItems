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
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
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
    
    @Override
    public String getDisplayName(T ingredient) {
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        return definition.asFormattedText(entry, entry.getValue()).getString();
    }
    
    @Override
    public String getUniqueId(T ingredient, UidContext context) {
        ComparisonContext comparisonContext = context.unwrapContext();
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        return String.valueOf(EntryStacks.hash(entry, comparisonContext));
    }
    
    @Override
    public ResourceLocation getResourceLocation(T ingredient) {
        EntryStack<T> entry = ingredient.unwrapStack(definition);
        ResourceLocation location = entry.getIdentifier();
        return location == null ? new ResourceLocation("unknown") : location;
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
