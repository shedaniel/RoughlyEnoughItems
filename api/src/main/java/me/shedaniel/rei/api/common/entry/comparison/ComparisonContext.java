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

public enum ComparisonContext {
    /**
     * Should only compare the type of the object.
     * <p>
     * The fuzzy context type denotes that the equivalent stacks should be <b>primarily</b> the same.
     * <p>
     * For example, enchantment books of different enchantments should be different within this context,
     * while tools with different damage values and different enchantments should be treated as the same within this context.
     * Skulker boxes with different content should be different within this context.
     */
    FUZZY(false),
    /**
     * Should compare the nbt and the type of the object.
     * <p>
     * The exact context type denotes that the equivalent stacks should be <b>functionally</b> the same.
     * <p>
     * For example, tools with different damage values and different enchantments should be treated as different within this context.
     */
    EXACT(true);
    
    private final boolean exact;
    
    ComparisonContext(boolean exact) {
        this.exact = exact;
    }
    
    public boolean isExact() {
        return exact;
    }
    
    public boolean isFuzzy() {
        return !exact;
    }
}