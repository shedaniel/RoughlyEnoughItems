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

package me.shedaniel.rei.api;

import me.shedaniel.rei.impl.ObjectHolderImpl;
import org.jetbrains.annotations.ApiStatus;

public interface ObjectHolder<T> {
    @SuppressWarnings("deprecation")
    static <T> ObjectHolder<T> of(T o) {
        return new ObjectHolderImpl<>(o);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default int intValue() {
        return (int) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default long longValue() {
        return (long) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default boolean booleanValue() {
        return (boolean) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default float floatValue() {
        return (float) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default double doubleValue() {
        return (double) (Object) value();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default String stringValue() {
        return (String) value();
    }
    
    T value();
}