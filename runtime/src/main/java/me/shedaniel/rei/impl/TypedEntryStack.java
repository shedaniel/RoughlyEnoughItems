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

package me.shedaniel.rei.impl;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.ingredient.entry.type.BuiltinEntryTypes;
import me.shedaniel.rei.api.ingredient.entry.type.EntryDefinition;
import me.shedaniel.rei.api.ingredient.entry.type.EntryType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class TypedEntryStack<T> extends AbstractEntryStack<T> {
    public static final EntryStack<Unit> EMPTY = new TypedEntryStack<>(BuiltinEntryTypes.EMPTY, Unit.INSTANCE);
    
    private final EntryDefinition<T> definition;
    private T value;
    
    public TypedEntryStack(EntryType<T> type, T value) {
        this(type.getDefinition(), value);
    }
    
    public TypedEntryStack(EntryDefinition<T> definition, T value) {
        this.definition = definition;
        this.value = value;
    }
    
    @Override
    @NotNull
    public EntryDefinition<T> getDefinition() {
        return definition;
    }
    
    @Override
    public T getValue() {
        return value;
    }
    
    @Override
    @Nullable
    public ResourceLocation getIdentifier() {
        return getDefinition().getIdentifier(this, value);
    }
    
    @Override
    public boolean isEmpty() {
        return getDefinition().isEmpty(this, value);
    }
    
    @Override
    public EntryStack<T> copy() {
        TypedEntryStack<T> stack = new TypedEntryStack<>(definition, getDefinition().copy(this, value));
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public EntryStack<T> rewrap() {
        TypedEntryStack<T> stack = new TypedEntryStack<>(definition, value);
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public EntryStack<T> normalize() {
        TypedEntryStack<T> stack = new TypedEntryStack<>(definition, getDefinition().normalize(this, value));
        for (Short2ObjectMap.Entry<Object> entry : getSettings().short2ObjectEntrySet()) {
            stack.setting(EntryStack.Settings.getById(entry.getShortKey()), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public int hash(ComparisonContext context) {
        return getDefinition().hash(this, value, context);
    }
    
    @Override
    public @NotNull Component asFormattedText() {
        return getDefinition().asFormattedText(this, value);
    }
}
