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

package me.shedaniel.rei.impl.common.entry.type;

import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.WeakReference;

@ApiStatus.Internal
public class EntryTypeDeferred<T> implements EntryType<T> {
    private final ResourceLocation id;
    private final int hashCode;
    private WeakReference<EntryDefinition<T>> reference;
    
    public EntryTypeDeferred(ResourceLocation id) {
        this.id = id;
        this.hashCode = id.hashCode();
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
    
    @Override
    public EntryDefinition<T> getDefinition() {
        if (reference != null) {
            EntryDefinition<T> definition = reference.get();
            if (definition != null) {
                return definition;
            }
        }
        EntryDefinition<?> d = EntryTypeRegistry.getInstance().get(id);
        if (d == null) {
            throw new NullPointerException("Entry type " + id + " doesn't exist!");
        }
        EntryDefinition<T> definition = d.cast();
        reference = new WeakReference<>(definition);
        return definition;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntryTypeDeferred<?> that)) return false;
        return hashCode == that.hashCode && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
}
