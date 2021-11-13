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

import java.util.List;

@ApiStatus.Internal
final class RecordValueAnimator<T, A extends RecordValueAnimator.Arg<T>> implements ValueAnimator<T> {
    private final A arg;
    
    RecordValueAnimator(A arg) {
        this.arg = arg;
    }
    
    @Override
    public ValueAnimator<T> setTo(T value, long duration) {
        arg.set(value, duration);
        return this;
    }
    
    @Override
    public T target() {
        return arg.target();
    }
    
    @Override
    public T value() {
        return arg.value();
    }
    
    @Override
    public void update(double delta) {
        for (ValueAnimator<?> dependency : arg.dependencies()) {
            dependency.update(delta);
        }
    }
    
    @FunctionalInterface
    public interface Setter<T> {
        void set(T value);
    }
    
    public interface Arg<T> {
        List<ValueAnimator<?>> dependencies();
        
        void set(T value, long duration);
        
        T target();
        
        T value();
    }
    
    public static <A1, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg1<A1, T>> of(ValueAnimator<A1> a1, RecordValueAnimatorArgs.Arg1.Op<A1, T> op, RecordValueAnimatorArgs.Arg1.Up<A1, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg1<>(a1, op, up));
    }
    
    public static <A1, A2, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg2<A1, A2, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, RecordValueAnimatorArgs.Arg2.Op<A1, A2, T> op, RecordValueAnimatorArgs.Arg2.Up<A1, A2, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg2<>(a1, a2, op, up));
    }
    
    public static <A1, A2, A3, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg3<A1, A2, A3, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, RecordValueAnimatorArgs.Arg3.Op<A1, A2, A3, T> op, RecordValueAnimatorArgs.Arg3.Up<A1, A2, A3, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg3<>(a1, a2, a3, op, up));
    }
    
    public static <A1, A2, A3, A4, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg4<A1, A2, A3, A4, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, RecordValueAnimatorArgs.Arg4.Op<A1, A2, A3, A4, T> op, RecordValueAnimatorArgs.Arg4.Up<A1, A2, A3, A4, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg4<>(a1, a2, a3, a4, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg5<A1, A2, A3, A4, A5, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, RecordValueAnimatorArgs.Arg5.Op<A1, A2, A3, A4, A5, T> op, RecordValueAnimatorArgs.Arg5.Up<A1, A2, A3, A4, A5, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg5<>(a1, a2, a3, a4, a5, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg6<A1, A2, A3, A4, A5, A6, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, RecordValueAnimatorArgs.Arg6.Op<A1, A2, A3, A4, A5, A6, T> op, RecordValueAnimatorArgs.Arg6.Up<A1, A2, A3, A4, A5, A6, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg6<>(a1, a2, a3, a4, a5, a6, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg7<A1, A2, A3, A4, A5, A6, A7, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, RecordValueAnimatorArgs.Arg7.Op<A1, A2, A3, A4, A5, A6, A7, T> op, RecordValueAnimatorArgs.Arg7.Up<A1, A2, A3, A4, A5, A6, A7, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg7<>(a1, a2, a3, a4, a5, a6, a7, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg8<A1, A2, A3, A4, A5, A6, A7, A8, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, RecordValueAnimatorArgs.Arg8.Op<A1, A2, A3, A4, A5, A6, A7, A8, T> op, RecordValueAnimatorArgs.Arg8.Up<A1, A2, A3, A4, A5, A6, A7, A8, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg8<>(a1, a2, a3, a4, a5, a6, a7, a8, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg9<A1, A2, A3, A4, A5, A6, A7, A8, A9, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, RecordValueAnimatorArgs.Arg9.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> op, RecordValueAnimatorArgs.Arg9.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg9<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, RecordValueAnimatorArgs.Arg10.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> op, RecordValueAnimatorArgs.Arg10.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg10<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, RecordValueAnimatorArgs.Arg11.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> op, RecordValueAnimatorArgs.Arg11.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg11<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, RecordValueAnimatorArgs.Arg12.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> op, RecordValueAnimatorArgs.Arg12.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg12<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, RecordValueAnimatorArgs.Arg13.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> op, RecordValueAnimatorArgs.Arg13.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg13<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, RecordValueAnimatorArgs.Arg14.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> op, RecordValueAnimatorArgs.Arg14.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg14<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, RecordValueAnimatorArgs.Arg15.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> op, RecordValueAnimatorArgs.Arg15.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg15<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, RecordValueAnimatorArgs.Arg16.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> op, RecordValueAnimatorArgs.Arg16.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg16<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg17<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, RecordValueAnimatorArgs.Arg17.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> op, RecordValueAnimatorArgs.Arg17.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg17<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg18<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, ValueAnimator<A18> a18, RecordValueAnimatorArgs.Arg18.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> op, RecordValueAnimatorArgs.Arg18.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg18<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg19<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, ValueAnimator<A18> a18, ValueAnimator<A19> a19, RecordValueAnimatorArgs.Arg19.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> op, RecordValueAnimatorArgs.Arg19.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg19<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, op, up));
    }
    
    public static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> RecordValueAnimator<T, RecordValueAnimatorArgs.Arg20<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T>> of(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, ValueAnimator<A18> a18, ValueAnimator<A19> a19, ValueAnimator<A20> a20, RecordValueAnimatorArgs.Arg20.Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> op, RecordValueAnimatorArgs.Arg20.Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> up) {
        return new RecordValueAnimator<>(new RecordValueAnimatorArgs.Arg20<>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, op, up));
    }
}
