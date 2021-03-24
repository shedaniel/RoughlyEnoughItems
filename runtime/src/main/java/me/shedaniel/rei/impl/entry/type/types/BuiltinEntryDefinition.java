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

package me.shedaniel.rei.impl.entry.type.types;

import me.shedaniel.rei.api.client.ingredient.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.ingredient.EntryStack;
import me.shedaniel.rei.api.common.ingredient.entry.EntrySerializer;
import me.shedaniel.rei.api.common.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.ingredient.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.ingredient.entry.type.EntryType;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public class BuiltinEntryDefinition<T> implements EntryDefinition<T>, EntrySerializer<T> {
    private final Class<T> clazz;
    private final EntryType<T> type;
    private final boolean empty;
    private final Supplier<T> defaultValue;
    private final EntryRenderer<T> renderer;
    
    protected BuiltinEntryDefinition(Class<T> clazz, EntryType<T> type, boolean empty, Supplier<T> defaultValue, EntryRenderer<T> renderer) {
        this.clazz = clazz;
        this.type = type;
        this.empty = empty;
        this.defaultValue = defaultValue;
        this.renderer = renderer;
    }
    
    @Override
    public Class<T> getValueType() {
        return clazz;
    }
    
    @Override
    public EntryType<T> getType() {
        return type;
    }
    
    @Override
    public EntryRenderer<T> getRenderer() {
        return renderer;
    }
    
    @Override
    @Nullable
    public ResourceLocation getIdentifier(EntryStack<T> entry, T value) {
        return null;
    }
    
    @Override
    public boolean isEmpty(EntryStack<T> entry, T value) {
        return empty;
    }
    
    @Override
    public T copy(EntryStack<T> entry, T value) {
        return value;
    }
    
    @Override
    public T normalize(EntryStack<T> entry, T value) {
        return value;
    }
    
    @Override
    public int hash(EntryStack<T> entry, T value, ComparisonContext context) {
        return empty ? 0 : Objects.hashCode(value);
    }
    
    @Override
    public boolean equals(T o1, T o2, ComparisonContext context) {
        return empty || Objects.equals(o1, o2);
    }
    
    @Override
    @Nullable
    public EntrySerializer<T> getSerializer() {
        return this;
    }
    
    @Override
    public Component asFormattedText(EntryStack<T> entry, T value) {
        return ImmutableTextComponent.EMPTY;
    }
    
    @Override
    public Collection<ResourceLocation> getTagsFor(EntryStack<T> entry, T value) {
        return Collections.emptyList();
    }
    
    @Override
    public boolean supportReading() {
        return true;
    }
    
    @Override
    public boolean supportSaving() {
        return true;
    }
    
    @Override
    public CompoundTag save(EntryStack<T> entry, T value) {
        return new CompoundTag();
    }
    
    @Override
    public T read(CompoundTag tag) {
        return defaultValue.get();
    }
}
