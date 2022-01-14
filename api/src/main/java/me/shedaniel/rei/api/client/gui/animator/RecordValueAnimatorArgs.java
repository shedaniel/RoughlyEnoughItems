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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
class RecordValueAnimatorArgs {
    @FunctionalInterface
    public interface Setter<T> {
        void set(T value);
    }
    
    public static class Arg1<A1, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final Op<A1, T> op;
        private final Up<A1, T> up;
        
        public Arg1(ValueAnimator<A1> a1, Op<A1, T> op, Up<A1, T> up) {
            this.a1 = a1;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, T> {
            T construct(A1 arg1);
        }
        
        @FunctionalInterface
        public interface Up<A1, T> {
            void update(T value, Setter<A1> arg1);
        }
    }
    
    public static class Arg2<A1, A2, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final Op<A1, A2, T> op;
        private final Up<A1, A2, T> up;
        
        public Arg2(ValueAnimator<A1> a1, ValueAnimator<A2> a2, Op<A1, A2, T> op, Up<A1, A2, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, T> {
            T construct(A1 arg1, A2 arg2);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2);
        }
    }
    
    public static class Arg3<A1, A2, A3, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final Op<A1, A2, A3, T> op;
        private final Up<A1, A2, A3, T> up;
        
        public Arg3(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, Op<A1, A2, A3, T> op, Up<A1, A2, A3, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3);
        }
    }
    
    public static class Arg4<A1, A2, A3, A4, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final Op<A1, A2, A3, A4, T> op;
        private final Up<A1, A2, A3, A4, T> up;
        
        public Arg4(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, Op<A1, A2, A3, A4, T> op, Up<A1, A2, A3, A4, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4);
        }
    }
    
    public static class Arg5<A1, A2, A3, A4, A5, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final Op<A1, A2, A3, A4, A5, T> op;
        private final Up<A1, A2, A3, A4, A5, T> up;
        
        public Arg5(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, Op<A1, A2, A3, A4, A5, T> op, Up<A1, A2, A3, A4, A5, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5);
        }
    }
    
    public static class Arg6<A1, A2, A3, A4, A5, A6, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final Op<A1, A2, A3, A4, A5, A6, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, T> up;
        
        public Arg6(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, Op<A1, A2, A3, A4, A5, A6, T> op, Up<A1, A2, A3, A4, A5, A6, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6);
        }
    }
    
    public static class Arg7<A1, A2, A3, A4, A5, A6, A7, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final Op<A1, A2, A3, A4, A5, A6, A7, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, T> up;
        
        public Arg7(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, Op<A1, A2, A3, A4, A5, A6, A7, T> op, Up<A1, A2, A3, A4, A5, A6, A7, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7);
        }
    }
    
    public static class Arg8<A1, A2, A3, A4, A5, A6, A7, A8, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, T> up;
        
        public Arg8(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, Op<A1, A2, A3, A4, A5, A6, A7, A8, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8);
        }
    }
    
    public static class Arg9<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> up;
        
        public Arg9(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9);
        }
    }
    
    public static class Arg10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> up;
        
        public Arg10(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10);
        }
    }
    
    public static class Arg11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> up;
        
        public Arg11(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11);
        }
    }
    
    public static class Arg12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> up;
        
        public Arg12(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12);
        }
    }
    
    public static class Arg13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> up;
        
        public Arg13(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13);
        }
    }
    
    public static class Arg14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> up;
        
        public Arg14(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14);
        }
    }
    
    public static class Arg15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final ValueAnimator<A15> a15;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> up;
        
        public Arg15(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.a15 = a15;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .add(a15)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration), v15 -> a15.setTo(v15, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target(), a15.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value(), a15.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14, Setter<A15> arg15);
        }
    }
    
    public static class Arg16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final ValueAnimator<A15> a15;
        private final ValueAnimator<A16> a16;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> up;
        
        public Arg16(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.a15 = a15;
            this.a16 = a16;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .add(a15)
                    .add(a16)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration), v15 -> a15.setTo(v15, duration), v16 -> a16.setTo(v16, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target(), a15.target(), a16.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value(), a15.value(), a16.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15, A16 arg16);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14, Setter<A15> arg15, Setter<A16> arg16);
        }
    }
    
    public static class Arg17<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final ValueAnimator<A15> a15;
        private final ValueAnimator<A16> a16;
        private final ValueAnimator<A17> a17;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> up;
        
        public Arg17(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.a15 = a15;
            this.a16 = a16;
            this.a17 = a17;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .add(a15)
                    .add(a16)
                    .add(a17)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration), v15 -> a15.setTo(v15, duration), v16 -> a16.setTo(v16, duration), v17 -> a17.setTo(v17, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target(), a15.target(), a16.target(), a17.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value(), a15.value(), a16.value(), a17.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15, A16 arg16, A17 arg17);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14, Setter<A15> arg15, Setter<A16> arg16, Setter<A17> arg17);
        }
    }
    
    public static class Arg18<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final ValueAnimator<A15> a15;
        private final ValueAnimator<A16> a16;
        private final ValueAnimator<A17> a17;
        private final ValueAnimator<A18> a18;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> up;
        
        public Arg18(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, ValueAnimator<A18> a18, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.a15 = a15;
            this.a16 = a16;
            this.a17 = a17;
            this.a18 = a18;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .add(a15)
                    .add(a16)
                    .add(a17)
                    .add(a18)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration), v15 -> a15.setTo(v15, duration), v16 -> a16.setTo(v16, duration), v17 -> a17.setTo(v17, duration), v18 -> a18.setTo(v18, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target(), a15.target(), a16.target(), a17.target(), a18.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value(), a15.value(), a16.value(), a17.value(), a18.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15, A16 arg16, A17 arg17, A18 arg18);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14, Setter<A15> arg15, Setter<A16> arg16, Setter<A17> arg17, Setter<A18> arg18);
        }
    }
    
    public static class Arg19<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final ValueAnimator<A15> a15;
        private final ValueAnimator<A16> a16;
        private final ValueAnimator<A17> a17;
        private final ValueAnimator<A18> a18;
        private final ValueAnimator<A19> a19;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> up;
        
        public Arg19(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, ValueAnimator<A18> a18, ValueAnimator<A19> a19, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.a15 = a15;
            this.a16 = a16;
            this.a17 = a17;
            this.a18 = a18;
            this.a19 = a19;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .add(a15)
                    .add(a16)
                    .add(a17)
                    .add(a18)
                    .add(a19)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration), v15 -> a15.setTo(v15, duration), v16 -> a16.setTo(v16, duration), v17 -> a17.setTo(v17, duration), v18 -> a18.setTo(v18, duration), v19 -> a19.setTo(v19, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target(), a15.target(), a16.target(), a17.target(), a18.target(), a19.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value(), a15.value(), a16.value(), a17.value(), a18.value(), a19.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15, A16 arg16, A17 arg17, A18 arg18, A19 arg19);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14, Setter<A15> arg15, Setter<A16> arg16, Setter<A17> arg17, Setter<A18> arg18, Setter<A19> arg19);
        }
    }
    
    public static class Arg20<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> implements RecordValueAnimator.Arg<T> {
        private final ValueAnimator<A1> a1;
        private final ValueAnimator<A2> a2;
        private final ValueAnimator<A3> a3;
        private final ValueAnimator<A4> a4;
        private final ValueAnimator<A5> a5;
        private final ValueAnimator<A6> a6;
        private final ValueAnimator<A7> a7;
        private final ValueAnimator<A8> a8;
        private final ValueAnimator<A9> a9;
        private final ValueAnimator<A10> a10;
        private final ValueAnimator<A11> a11;
        private final ValueAnimator<A12> a12;
        private final ValueAnimator<A13> a13;
        private final ValueAnimator<A14> a14;
        private final ValueAnimator<A15> a15;
        private final ValueAnimator<A16> a16;
        private final ValueAnimator<A17> a17;
        private final ValueAnimator<A18> a18;
        private final ValueAnimator<A19> a19;
        private final ValueAnimator<A20> a20;
        private final Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> op;
        private final Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> up;
        
        public Arg20(ValueAnimator<A1> a1, ValueAnimator<A2> a2, ValueAnimator<A3> a3, ValueAnimator<A4> a4, ValueAnimator<A5> a5, ValueAnimator<A6> a6, ValueAnimator<A7> a7, ValueAnimator<A8> a8, ValueAnimator<A9> a9, ValueAnimator<A10> a10, ValueAnimator<A11> a11, ValueAnimator<A12> a12, ValueAnimator<A13> a13, ValueAnimator<A14> a14, ValueAnimator<A15> a15, ValueAnimator<A16> a16, ValueAnimator<A17> a17, ValueAnimator<A18> a18, ValueAnimator<A19> a19, ValueAnimator<A20> a20, Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> op, Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> up) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
            this.a4 = a4;
            this.a5 = a5;
            this.a6 = a6;
            this.a7 = a7;
            this.a8 = a8;
            this.a9 = a9;
            this.a10 = a10;
            this.a11 = a11;
            this.a12 = a12;
            this.a13 = a13;
            this.a14 = a14;
            this.a15 = a15;
            this.a16 = a16;
            this.a17 = a17;
            this.a18 = a18;
            this.a19 = a19;
            this.a20 = a20;
            this.op = op;
            this.up = up;
        }
        
        @Override
        public List<ValueAnimator<?>> dependencies() {
            return ImmutableList.<ValueAnimator<?>>builder()
                    .add(a1)
                    .add(a2)
                    .add(a3)
                    .add(a4)
                    .add(a5)
                    .add(a6)
                    .add(a7)
                    .add(a8)
                    .add(a9)
                    .add(a10)
                    .add(a11)
                    .add(a12)
                    .add(a13)
                    .add(a14)
                    .add(a15)
                    .add(a16)
                    .add(a17)
                    .add(a18)
                    .add(a19)
                    .add(a20)
                    .build();
        }
        
        @Override
        public void set(T value, long duration) {
            up.update(value, v1 -> a1.setTo(v1, duration), v2 -> a2.setTo(v2, duration), v3 -> a3.setTo(v3, duration), v4 -> a4.setTo(v4, duration), v5 -> a5.setTo(v5, duration), v6 -> a6.setTo(v6, duration), v7 -> a7.setTo(v7, duration), v8 -> a8.setTo(v8, duration), v9 -> a9.setTo(v9, duration), v10 -> a10.setTo(v10, duration), v11 -> a11.setTo(v11, duration), v12 -> a12.setTo(v12, duration), v13 -> a13.setTo(v13, duration), v14 -> a14.setTo(v14, duration), v15 -> a15.setTo(v15, duration), v16 -> a16.setTo(v16, duration), v17 -> a17.setTo(v17, duration), v18 -> a18.setTo(v18, duration), v19 -> a19.setTo(v19, duration), v20 -> a20.setTo(v20, duration));
        }
        
        @Override
        public T target() {
            return op.construct(a1.target(), a2.target(), a3.target(), a4.target(), a5.target(), a6.target(), a7.target(), a8.target(), a9.target(), a10.target(), a11.target(), a12.target(), a13.target(), a14.target(), a15.target(), a16.target(), a17.target(), a18.target(), a19.target(), a20.target());
        }
        
        @Override
        public T value() {
            return op.construct(a1.value(), a2.value(), a3.value(), a4.value(), a5.value(), a6.value(), a7.value(), a8.value(), a9.value(), a10.value(), a11.value(), a12.value(), a13.value(), a14.value(), a15.value(), a16.value(), a17.value(), a18.value(), a19.value(), a20.value());
        }
        
        @FunctionalInterface
        public interface Op<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> {
            T construct(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6, A7 arg7, A8 arg8, A9 arg9, A10 arg10, A11 arg11, A12 arg12, A13 arg13, A14 arg14, A15 arg15, A16 arg16, A17 arg17, A18 arg18, A19 arg19, A20 arg20);
        }
        
        @FunctionalInterface
        public interface Up<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, T> {
            void update(T value, Setter<A1> arg1, Setter<A2> arg2, Setter<A3> arg3, Setter<A4> arg4, Setter<A5> arg5, Setter<A6> arg6, Setter<A7> arg7, Setter<A8> arg8, Setter<A9> arg9, Setter<A10> arg10, Setter<A11> arg11, Setter<A12> arg12, Setter<A13> arg13, Setter<A14> arg14, Setter<A15> arg15, Setter<A16> arg16, Setter<A17> arg17, Setter<A18> arg18, Setter<A19> arg19, Setter<A20> arg20);
        }
    }
}
