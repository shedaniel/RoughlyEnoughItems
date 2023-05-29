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

package me.shedaniel.rei.impl.common.util;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class HashedEntryStackWrapper {
    private final EntryStack<?> stack;
    private final long hash;
    private final int hashInt;
    
    public HashedEntryStackWrapper(EntryStack<?> stack) {
        this.stack = Objects.requireNonNull(stack);
        this.hash = EntryStacks.hashExact(stack);
        this.hashInt = Long.hashCode(this.hash);
    }
    
    public HashedEntryStackWrapper(EntryStack<?> stack, long hash) {
        this.stack = Objects.requireNonNull(stack);
        this.hash = hash;
        this.hashInt = Long.hashCode(this.hash);
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof HashedEntryStackWrapper && hash == ((HashedEntryStackWrapper) o).hash;
    }
    
    @Override
    public int hashCode() {
        return hashInt;
    }
    
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    public EntryStack<?> unwrap() {
        return stack;
    }
    
    public long hashExact() {
        return hash;
    }
    
    public HashedEntryStackWrapper normalize() {
        EntryStack<?> normalized = stack.normalize();
        long hashExact = EntryStacks.hashExact(normalized);
        if (hashExact == hash)
            return this;
        return new HashedEntryStackWrapper(normalized, hashExact);
    }
}