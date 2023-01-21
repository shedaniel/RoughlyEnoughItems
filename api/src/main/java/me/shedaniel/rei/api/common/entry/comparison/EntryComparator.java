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

package me.shedaniel.rei.api.common.entry.comparison;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Hasher implementation for {@link T}.
 */
@FunctionalInterface
public interface EntryComparator<T> {
    /**
     * Creates an {@link EntryComparator} that does not compare anything.
     *
     * @param <T> the type of the entry
     * @return an {@link EntryComparator} that does not compare anything
     */
    static <T> EntryComparator<T> noop() {
        return (context, stack) -> 1;
    }
    
    /**
     * Creates an {@link EntryComparator} that compares the {@link ItemStack}'s NBT.
     *
     * @return an {@link EntryComparator} that compares the {@link ItemStack}'s NBT
     */
    static EntryComparator<ItemStack> itemNbt() {
        EntryComparator<Tag> nbtHasher = nbt("Count");
        return (context, stack) -> {
            CompoundTag tag = stack.getTag();
            return tag == null ? 0L : nbtHasher.hash(context, tag);
        };
    }
    
    /**
     * Creates an {@link EntryComparator} that compares the {@link FluidStack}'s NBT.
     *
     * @return an {@link EntryComparator} that compares the {@link FluidStack}'s NBT
     */
    static EntryComparator<FluidStack> fluidNbt() {
        EntryComparator<Tag> nbtHasher = nbt("Amount");
        return (context, stack) -> {
            CompoundTag tag = stack.getTag();
            return tag == null ? 0L : nbtHasher.hash(context, tag);
        };
    }
    
    /**
     * Creates an {@link EntryComparator} that compares the nbt, but
     * ignoring some given keys.
     *
     * @param ignoredKeys the keys to ignore
     * @return an {@link EntryComparator} that compares the nbt
     */
    static EntryComparator<Tag> nbt(String... ignoredKeys) {
        return Internals.getNbtHasher(ignoredKeys);
    }
    
    /**
     * Returns hash code of the {@link T} stack.
     *
     * @param context the context to use
     * @param stack   the stack to hash code
     * @return the hash code of the {@code context} context
     */
    long hash(ComparisonContext context, T stack);
    
    default EntryComparator<T> onlyExact() {
        EntryComparator<T> self = this;
        
        return (context, stack) -> {
            return context.isExact() ? self.hash(context, stack) : 1;
        };
    }
    
    default EntryComparator<T> then(EntryComparator<T> other) {
        Objects.requireNonNull(other);
        EntryComparator<T> self = this;
        
        return (context, stack) -> {
            long hash = 1L;
            hash = hash * 31 + self.hash(context, stack);
            hash = hash * 31 + other.hash(context, stack);
            return hash;
        };
    }
}
