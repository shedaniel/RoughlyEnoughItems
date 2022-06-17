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

package me.shedaniel.rei.api.common.entry;

import net.minecraft.nbt.CompoundTag;

/**
 * A serializer for {@link EntryStack}, optionally supporting saving and reading.
 *
 * @param <T> the type of object to serialize
 */
public interface EntrySerializer<T> {
    /**
     * Whether this serializer supports saving.
     *
     * @return whether this serializer supports saving
     */
    boolean supportSaving();
    
    /**
     * Whether this serializer supports reading.
     *
     * @return whether this serializer supports reading
     */
    boolean supportReading();
    
    /**
     * Serializes the given object into a {@link CompoundTag}.
     *
     * @param entry the entry stack
     * @param value the value to serialize
     * @return the serialized tag
     */
    CompoundTag save(EntryStack<T> entry, T value);
    
    /**
     * Deserializes the given tag into an object.
     *
     * @param tag the tag to deserialize
     * @return the deserialized object
     */
    T read(CompoundTag tag);
}
