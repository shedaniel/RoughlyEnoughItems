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

public enum ComparisonContext {
    /**
     * Should only compare the type of the object
     */
    FUZZY(true, true),
    /**
     * Should compare the nbt and the type of the object
     */
    IGNORE_COUNT(true, false),
    /**
     * Should compare the amount and the type of the object
     */
    IGNORE_NBT(true, false),
    /**
     * Should compare the amount, the nbt and the type of the object
     */
    EXACT(false, false);
    
    boolean ignoresCount;
    boolean ignoresNbt;
    
    ComparisonContext(boolean ignoresCount, boolean ignoresNbt) {
        this.ignoresCount = ignoresCount;
        this.ignoresNbt = ignoresNbt;
    }
    
    public boolean isIgnoresCount() {
        return ignoresCount;
    }
    
    public boolean isIgnoresNbt() {
        return ignoresNbt;
    }
}