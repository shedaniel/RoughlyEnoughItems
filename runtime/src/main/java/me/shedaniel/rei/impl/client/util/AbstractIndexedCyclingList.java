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

package me.shedaniel.rei.impl.client.util;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
abstract class AbstractIndexedCyclingList<T> implements CyclingList<T> {
    private int position = 0;
    
    protected abstract T get(int index);
    
    protected abstract T empty();
    
    @Override
    public T peek() {
        int size = size();
        
        if (size == 0) {
            return empty();
        } else {
            return get(Math.floorMod(position, size));
        }
    }
    
    @Override
    public T next() {
        int size = size();
        
        if (size == 0) {
            return empty();
        } else {
            int tmp = position;
            position = Math.floorMod(++tmp, size);
            return get(Math.floorMod(tmp, size));
        }
    }
    
    @Override
    public int currentIndex() {
        return position;
    }
    
    @Override
    public int nextIndex() {
        return Math.floorMod(position + 1, size());
    }
    
    @Override
    public T previous() {
        int size = size();
        
        if (size == 0) {
            return empty();
        } else {
            position = Math.floorMod(--position, size);
            return get(position);
        }
    }
    
    @Override
    public int previousIndex() {
        return Math.floorMod(position - 1, size());
    }
    
    @Override
    public void resetToStart() {
        position = 0;
    }
    
    public static abstract class Mutable<T> extends AbstractIndexedCyclingList<T> implements CyclingList.Mutable<T> {
    }
}
