/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.api.client.gui.animator;

import org.jetbrains.annotations.ApiStatus;

/**
 * A value provider is used to provide a value for animation.
 *
 * @param <T> the type of the value
 * @see ValueAnimator
 */
@ApiStatus.Experimental
public interface ValueProvider<T> {
    /**
     * Returns a constant value provider, which always returns the same value.
     *
     * @param value the value to return
     * @param <T>   the type of the value
     * @return the constant value provider
     */
    static <T> ValueProvider<T> constant(T value) {
        return new ConstantValueProvider<>(value);
    }
    
    /**
     * Returns the current value of the provider.
     *
     * @return the current value
     */
    T value();
    
    /**
     * Returns the target value of the provider.
     *
     * @return the target value
     */
    T target();
    
    /**
     * Completes the animation immediately.
     * This will set the current value to the target value.
     */
    void completeImmediately();
    
    /**
     * Updates the current value of the provider by the tick delta.
     *
     * @param delta the tick delta
     */
    void update(double delta);
}
