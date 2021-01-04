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

package me.shedaniel.rei.api.entry;

import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface EntryDefinition<T> {
    @NotNull
    Class<T> getValueType();
    
    @NotNull
    EntryType<T> getType();
    
    @NotNull
    EntryRenderer<T> getRenderer();
    
    @NotNull
    Optional<ResourceLocation> getIdentifier(EntryStack<T> entry, T value);
    
    @NotNull
    Fraction getAmount(EntryStack<T> entry, T value);
    
    void setAmount(EntryStack<T> entry, T value, Fraction amount);
    
    boolean isEmpty(EntryStack<T> entry, T value);
    
    @NotNull
    T copy(EntryStack<T> entry, T value);
    
    int hash(EntryStack<T> entry, T value, ComparisonContext context);
    
    boolean equals(T o1, T o2, ComparisonContext context);
    
    @NotNull
    CompoundTag toTag(EntryStack<T> entry, T value);
    
    @NotNull
    T fromTag(@NotNull CompoundTag tag);
    
    @NotNull
    Component asFormattedText(EntryStack<T> entry, T value);
    
    @NotNull
    Collection<ResourceLocation> getTagsFor(EntryStack<T> entry, T value);
    
    @ApiStatus.NonExtendable
    @NotNull
    default <O> EntryDefinition<O> cast() {
        return (EntryDefinition<O>) this;
    }
}

