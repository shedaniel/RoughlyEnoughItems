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

package me.shedaniel.rei.api.common.entry.comparison;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.ToLongFunction;

/**
 * Hasher implementation for {@link ItemStack}.
 */
@FunctionalInterface
public interface ItemComparator {
    static ItemComparator noop() {
        return stack -> 1;
    }
    
    static ItemComparator itemNbt() {
        ToLongFunction<Tag> nbtHasher = nbtHasher("Count");
        return stack -> {
            CompoundTag tag = stack.getTag();
            return tag == null ? 0L : nbtHasher.applyAsLong(tag);
        };
    }
    
    static ToLongFunction<Tag> nbtHasher(String... ignoredKeys) {
        return Internals.getNbtHasher(ignoredKeys);
    }
    
    long hash(ItemStack stack);
    
    default ItemComparator then(ItemComparator other) {
        Objects.requireNonNull(other);
        
        return stack -> {
            long hash = 1L;
            hash = hash * 31 + hash(stack);
            hash = hash * 31 + other.hash(stack);
            return hash;
        };
    }
}
