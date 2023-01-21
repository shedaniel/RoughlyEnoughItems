/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.common.entry.type.types;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ApiStatus.Internal
public class BuiltinEntryDefinition<T> implements EntryDefinition<T>, EntrySerializer<T> {
    private final Class<T> clazz;
    private final EntryType<T> type;
    private final boolean empty;
    private final Supplier<T> defaultValue;
    @Environment(EnvType.CLIENT)
    private EntryRenderer<T> renderer;
    
    protected BuiltinEntryDefinition(Class<T> clazz, EntryType<T> type, boolean empty, Supplier<T> defaultValue, Supplier<Supplier<EntryRenderer<T>>> renderer) {
        this.clazz = clazz;
        this.type = type;
        this.empty = empty;
        this.defaultValue = defaultValue;
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> this.renderer = renderer.get().get());
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
    @Environment(EnvType.CLIENT)
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
    public T wildcard(EntryStack<T> entry, T value) {
        return value;
    }
    
    @Override
    public long hash(EntryStack<T> entry, T value, ComparisonContext context) {
        return empty ? 0 : Objects.hash(value.getClass().getName(), value);
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
        return Component.empty();
    }
    
    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<T> entry, T value) {
        return Stream.empty();
    }
    
    @Override
    public boolean supportReading() {
        return empty;
    }
    
    @Override
    public boolean supportSaving() {
        return empty;
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
