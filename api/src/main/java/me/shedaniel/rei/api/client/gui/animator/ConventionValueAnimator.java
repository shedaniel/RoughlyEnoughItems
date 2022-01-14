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

package me.shedaniel.rei.api.client.gui.animator;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
final class ConventionValueAnimator<T> implements ValueAnimator<T> {
    private final ValueAnimator<T> parent;
    private final Supplier<T> convention;
    private final long duration;
    
    ConventionValueAnimator(ValueAnimator<T> parent, Supplier<T> convention, long duration) {
        this.parent = parent;
        this.convention = convention;
        this.duration = duration;
        setAs(convention.get());
    }
    
    @Override
    public ValueAnimator<T> setTo(T value, long duration) {
        return parent.setTo(value, duration);
    }
    
    @Override
    public T target() {
        return convention.get();
    }
    
    @Override
    public T value() {
        return parent.value();
    }
    
    @Override
    public void update(double delta) {
        parent.update(delta);
        T target = target();
        if (!Objects.equals(parent.target(), target)) {
            setTo(target, duration);
        }
    }
}
