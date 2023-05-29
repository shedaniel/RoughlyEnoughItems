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

package me.shedaniel.rei.api.common.util;

import me.shedaniel.clothconfig2.impl.EasingMethod;
import net.minecraft.Util;
import org.jetbrains.annotations.ApiStatus;

/**
 * @see me.shedaniel.rei.api.client.gui.animator.ValueAnimator
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public final class Animator extends Number {
    private double amount;
    private double target;
    private long start;
    private long duration;
    
    public Animator() {
    }
    
    public Animator(double amount) {
        setAs(amount);
    }
    
    public void setAs(double value) {
        this.set(value, false, 0);
    }
    
    public void setTo(double value, long duration) {
        if (target != value)
            this.set(value, true, duration);
    }
    
    private void set(double value, boolean animated, long duration) {
        this.target = value;
        this.start = Util.getMillis();
        
        if (animated) {
            this.duration = duration;
        } else {
            this.duration = 0;
            this.amount = this.target;
        }
    }
    
    public void update(double delta) {
        if (duration != 0) {
            if (amount < target) {
                this.amount = Math.min(ease(amount, target + (target - amount), Math.min(((double) Util.getMillis() - start) / duration * delta * 3.0D, 1.0D), EasingMethod.EasingMethodImpl.LINEAR), target);
            } else if (amount > target) {
                this.amount = Math.max(ease(amount, target - (amount - target), Math.min(((double) Util.getMillis() - start) / duration * delta * 3.0D, 1.0D), EasingMethod.EasingMethodImpl.LINEAR), target);
            }
        }
    }
    
    private static double ease(double start, double end, double amount, EasingMethod easingMethod) {
        return start + (end - start) * easingMethod.apply(amount);
    }
    
    @Override
    public int intValue() {
        return (int) amount;
    }
    
    @Override
    public long longValue() {
        return (long) amount;
    }
    
    @Override
    public float floatValue() {
        return (float) amount;
    }
    
    @Override
    public double doubleValue() {
        return amount;
    }
    
    public double target() {
        return target;
    }
}
