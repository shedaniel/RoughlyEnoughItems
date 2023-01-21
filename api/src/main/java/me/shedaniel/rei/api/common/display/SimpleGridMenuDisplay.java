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

package me.shedaniel.rei.api.common.display;

/**
 * A display to be used alongside {@link me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo},
 * to provide a {@code width} and {@code height} for the grid of the recipe.
 *
 * @see me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo
 */
public interface SimpleGridMenuDisplay extends Display {
    /**
     * Returns the functional width of the grid.
     *
     * @return the functional width of the grid
     */
    int getWidth();
    
    /**
     * Returns the functional height of the grid.
     *
     * @return the functional height of the grid
     */
    int getHeight();
    
    /**
     * Returns the input width of the input entries.
     *
     * @return the input width of the input entries
     * @deprecated use {@link #getInputWidth(int, int)} instead
     */
    @Deprecated(forRemoval = true)
    default int getInputWidth() {
        return getWidth();
    }
    
    /**
     * Returns the input height of the input entries.
     *
     * @return the input height of the input entries
     * @deprecated use {@link #getInputHeight(int, int)} instead
     */
    @Deprecated(forRemoval = true)
    default int getInputHeight() {
        return getHeight();
    }
    
    /**
     * Returns the input width of the input entries.
     *
     * @return the input width of the input entries
     */
    default int getInputWidth(int craftingWidth, int craftingHeight) {
        return getInputWidth();
    }
    
    /**
     * Returns the input height of the input entries.
     *
     * @return the input height of the input entries
     */
    default int getInputHeight(int craftingWidth, int craftingHeight) {
        return getInputHeight();
    }
}
