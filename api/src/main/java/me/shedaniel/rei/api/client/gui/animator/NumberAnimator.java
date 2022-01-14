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

import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class NumberAnimator<T extends Number> extends Number implements ValueAnimator<T> {
    public NumberAnimator<Double> asDouble() {
        return new NumberAnimatorWrapped<>(this, Number::doubleValue);
    }
    
    public NumberAnimator<Float> asFloat() {
        return new NumberAnimatorWrapped<>(this, Number::floatValue);
    }
    
    public NumberAnimator<Integer> asInt() {
        return new NumberAnimatorWrapped<>(this, d -> (int) Math.round(d.doubleValue()));
    }
    
    public NumberAnimator<Long> asLong() {
        return new NumberAnimatorWrapped<>(this, d -> Math.round(d.doubleValue()));
    }
    
    @Override
    public NumberAnimator<T> setAs(T value) {
        ValueAnimator.super.setAs(value);
        return this;
    }
    
    public NumberAnimator<T> setAs(int value) {
        setAsNumber(value);
        return this;
    }
    
    public NumberAnimator<T> setAs(long value) {
        setAsNumber(value);
        return this;
    }
    
    public NumberAnimator<T> setAs(float value) {
        setAsNumber(value);
        return this;
    }
    
    public NumberAnimator<T> setAs(double value) {
        setAsNumber(value);
        return this;
    }
    
    @Override
    public NumberAnimator<T> setTo(T value, long duration) {
        setToNumber(value, duration);
        return this;
    }
    
    public NumberAnimator<T> setTo(int value, long duration) {
        setToNumber(value, duration);
        return this;
    }
    
    public NumberAnimator<T> setTo(long value, long duration) {
        setToNumber(value, duration);
        return this;
    }
    
    public NumberAnimator<T> setTo(float value, long duration) {
        setToNumber(value, duration);
        return this;
    }
    
    public NumberAnimator<T> setTo(double value, long duration) {
        setToNumber(value, duration);
        return this;
    }
    
    public NumberAnimator<T> setAsNumber(Number value) {
        return setToNumber(value, -1);
    }
    
    public abstract NumberAnimator<T> setToNumber(Number value, long duration);
    
    @Override
    public NumberAnimator<T> withConvention(Supplier<T> convention, long duration) {
        ValueAnimator<T> parentConvention = ValueAnimator.super.withConvention(convention, duration);
        return new ValueAnimatorAsNumberAnimator<T>(parentConvention) {
            @Override
            public NumberAnimator<T> setToNumber(Number value, long duration) {
                return NumberAnimator.this.setToNumber(value, duration);
            }
        };
    }
}
