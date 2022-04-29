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

package me.shedaniel.rei.impl.common.entry;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.BuiltinEntryTypes;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import org.checkerframework.checker.units.qual.A;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public enum EntryStackProviderImpl implements Internals.EntryStackProvider {
    INSTANCE;
    
    @Override
    public EntryStack<Unit> empty() {
        return EmptyEntryStack.EMPTY;
    }
    
    @Override
    public <T> EntryStack<T> of(EntryDefinition<T> definition, T value) {
        if (Objects.equals(definition.getType().getId(), BuiltinEntryTypes.EMPTY_ID)) {
            return empty().cast();
        }
        
        return new TypedEntryStack<>(definition, value);
    }

    @Override
    public <T, B> EntryStack<T> ofCondensedEntry(EntryDefinition<T> definition, T value, ResourceLocation condensedEntryId, ResourceKey<Registry<B>> registryKey, Predicate<B> predicate, Function<B, T> defaultStackMethod, Function<T, B> entryFromStack) {
        CondensedEntryStack<T, B> entryStack = new CondensedEntryStack<T, B>(condensedEntryId, definition, value, false);

        entryStack.setupChildSet(registryKey, predicate, defaultStackMethod);
        entryStack.setEntryFromStackFunction(entryFromStack);

        return entryStack;
    }

    @Override
    public <T, B> EntryStack<T> ofCondensedEntry(EntryDefinition<T> definition, T value, ResourceLocation condensedEntryId, TagKey<B> entryTag, Function<B, T> defaultStackMethod, Function<T, B> entryFromStack) {
        CondensedEntryStack<T, B> entryStack = new CondensedEntryStack<T, B>(condensedEntryId, definition, value, false);

        entryStack.setupChildSet(entryTag, defaultStackMethod);
        entryStack.setEntryFromStackFunction(entryFromStack);

        return entryStack;
    }

    @Override
    public <T, B> EntryStack<T> ofCondensedEntry(EntryDefinition<T> definition, T value, ResourceLocation condensedEntryId, ResourceKey<Registry<B>> registryKey, Collection<B> collection, Function<B, T> defaultStackMethod, Function<T, B> entryFromStack) {
        CondensedEntryStack<T, B> entryStack = new CondensedEntryStack<T, B>(condensedEntryId, definition, value, false);

        entryStack.setupChildSet(registryKey, collection, defaultStackMethod);
        entryStack.setEntryFromStackFunction(entryFromStack);

        return entryStack;
    }
}
