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

package me.shedaniel.rei.impl.client.search.argument;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/**
 * MIT License
 * <p>
 * Copyright (c) 2019 Juntong Liu
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class IndexSet {
    public static final IndexSet ZERO = new IndexSet(0x1);
    public static final IndexSet ONE = new IndexSet(0x2);
    public static final IndexSet NONE = new IndexSet(0x0);

    int value = 0x0;

    public IndexSet() {
    }

    public IndexSet(IndexSet set) {
        value = set.value;
    }

    public IndexSet(int value) {
        this.value = value;
    }

    public void set(int index) {
        int i = 0x1 << index;
        value |= i;
    }

    public boolean get(int index) {
        int i = 0x1 << index;
        return (value & i) != 0;
    }

    public void merge(IndexSet s) {
        value = value == 0x1 ? s.value : (value |= s.value);
    }

    public boolean traverse(IntPredicate p) {
        int v = value;
        for (int i = 0; i < 7; i++) {
            if ((v & 0x1) == 0x1 && !p.test(i)) return false;
            else if (v == 0) return true;
            v >>= 1;
        }
        return true;
    }

    public void foreach(IntConsumer c) {
        int v = value;
        for (int i = 0; i < 7; i++) {
            if ((v & 0x1) == 0x1) c.accept(i);
            else if (v == 0) return;
            v >>= 1;
        }
    }

    public void offset(int i) {
        value <<= i;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        traverse(i -> {
            builder.append(i);
            builder.append(", ");
            return true;
        });
        if (builder.length() != 0) {
            builder.delete(builder.length() - 2, builder.length());
            return builder.toString();
        } else return "0";
    }

    public boolean isEmpty() {
        return value == 0x0;
    }

    public IndexSet copy() {
        return new IndexSet(value);
    }

    static class Immutable extends IndexSet {
        @Override
        public void set(int index) {
            throw new UnsupportedOperationException("Immutable collection");
        }

        @Override
        public void merge(IndexSet s) {
            throw new UnsupportedOperationException("Immutable collection");
        }

        @Override
        public void offset(int i) {
            throw new UnsupportedOperationException("Immutable collection");
        }
    }

    static class Storage {
        IndexSet tmp = new Immutable();
        int[] data = new int[16];

        public void set(IndexSet is, int index) {
            if (index >= data.length) {
                int size = index;
                size |= size >> 1;
                size |= size >> 2;
                size |= size >> 4;
                size |= size >> 8;
                size |= size >> 16;
                int[] replace = new int[size + 1];
                System.arraycopy(data, 0, replace, 0, data.length);
                data = replace;
            }
            data[index] = is.value + 1;
        }

        public IndexSet get(int index) {
            if (index >= data.length) return null;
            int ret = data[index];
            if (ret == 0) return null;
            tmp.value = ret - 1;
            return tmp;
        }
    }
}