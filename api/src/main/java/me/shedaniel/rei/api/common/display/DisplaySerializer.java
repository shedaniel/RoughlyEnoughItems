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

package me.shedaniel.rei.api.common.display;

import net.minecraft.nbt.CompoundTag;

/**
 * The display serializer used for display serialization, useful for persistent displays across reloads,
 * and server-client communication.
 *
 * @see SimpleDisplaySerializer
 * @see DisplaySerializerRegistry
 */
public interface DisplaySerializer<D extends Display> {
    /**
     * Serializes the display into a tag.
     *
     * @param tag     the tag to serialize into
     * @param display the display to serialize
     * @return the tag
     */
    CompoundTag save(CompoundTag tag, D display);
    
    /**
     * Deserializes the display from a tag.
     *
     * @param tag the tag to deserialize from
     * @return the display
     */
    D read(CompoundTag tag);
    
    /**
     * Returns whether the serialized output is persistent across differing reboots, thus enabling serialization support for saving to disk.
     *
     * @return whether the serialized output is persistent across differing reboots, thus enabling serialization support for saving to disk.
     */
    default boolean isPersistent() {
        return true;
    }
}
