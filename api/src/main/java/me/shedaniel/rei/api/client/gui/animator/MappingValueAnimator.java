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

import java.util.function.Function;

@ApiStatus.Internal
final class MappingValueAnimator<T, R> implements ValueAnimator<R> {
    private final ValueAnimator<T> parent;
    private final Function<T, R> converter;
    private final Function<R, T> backwardsConverter;
    
    MappingValueAnimator(ValueAnimator<T> parent, Function<T, R> converter, Function<R, T> backwardsConverter) {
        this.parent = parent;
        this.converter = converter;
        this.backwardsConverter = backwardsConverter;
    }
    
    @Override
    public ValueAnimator<R> setTo(R value, long duration) {
        parent.setTo(backwardsConverter.apply(value), duration);
        return this;
    }
    
    @Override
    public R target() {
        return converter.apply(parent.target());
    }
    
    @Override
    public R value() {
        return converter.apply(parent.value());
    }
    
    @Override
    public void update(double delta) {
        parent.update(delta);
    }
}
