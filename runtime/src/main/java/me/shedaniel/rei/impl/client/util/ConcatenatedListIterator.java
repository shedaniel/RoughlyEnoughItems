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

package me.shedaniel.rei.impl.client.util;

import me.shedaniel.rei.api.common.util.CollectionUtils;
import org.spongepowered.include.com.google.common.collect.Iterators;

import java.util.List;

public abstract class ConcatenatedListIterator<T> implements CyclingList<T> {
    private static final int HEAD_FIRST = -1, HEAD_LAST = -2, TAIL_FIRST = -3, TAIL_LAST = -4;
    private final List<T> listView;
    private final CyclingList<T> head, tail;
    private int position = HEAD_FIRST;
    
    public ConcatenatedListIterator(CyclingList<T> head, CyclingList<T> tail) {
        this.listView = CollectionUtils.concatUnmodifiable(() -> Iterators.forArray(head.get(), tail.get()));
        this.head = head;
        this.tail = tail;
        this.head.resetToStart();
        this.tail.resetToStart();
    }
    
    protected abstract T empty();
    
    @Override
    public T peek() {
        int p = currentIndex();
        int neededHeadPos = Math.min(p, head.size() - 1);
        int neededTailPos = Math.max(p - head.size(), 0);
        while (head.currentIndex() != neededHeadPos) {
            head.next();
        }
        while (tail.currentIndex() != neededTailPos) {
            tail.next();
        }
        return p < head.size() ? head.peek() : tail.peek();
    }
    
    @Override
    public T next() {
        position = nextIndex();
        int neededHeadPos = Math.min(position, head.size() - 1);
        int neededTailPos = Math.max(position - head.size(), 0);
        while (head.currentIndex() != neededHeadPos) {
            head.next();
        }
        while (tail.currentIndex() != neededTailPos) {
            tail.next();
        }
        T t = position < head.size() ? head.peek() : tail.peek();
        position = normalizeIndex(position);
        return t;
    }
    
    @Override
    public T previous() {
        position = previousIndex();
        int neededHeadPos = Math.min(position, head.size() - 1);
        int neededTailPos = Math.max(position - head.size(), 0);
        while (head.currentIndex() != neededHeadPos) {
            head.previous();
        }
        while (tail.currentIndex() != neededTailPos) {
            tail.previous();
        }
        T t = position < head.size() ? head.peek() : tail.peek();
        position = normalizeIndex(position);
        return t;
    }
    
    private int normalizeIndex(int index) {
        if (index == 0) return HEAD_FIRST;
        int hSize = head.size();
        if (index == hSize - 1) return HEAD_LAST;
        if (index == hSize) return TAIL_FIRST;
        if (index == hSize + tail.size() - 1) return TAIL_LAST;
        return index;
    }
    
    @Override
    public int currentIndex() {
        int size = size();
        int tmp = switch (position) {
            case HEAD_FIRST -> 0;
            case HEAD_LAST -> head.size() - 1;
            case TAIL_FIRST -> tail.size() > 0 ? head.size() : 0;
            case TAIL_LAST -> size - 1;
            default -> position;
        };
        return Math.floorMod(tmp, size);
    }
    
    @Override
    public int nextIndex() {
        int size = size();
        int tmp = switch (position) {
            case HEAD_FIRST -> 1;
            case HEAD_LAST -> {
                if (tail.size() > 0) {
                    yield head.size();
                } else if (head.size() > 0) {
                    yield 0;
                } else {
                    yield 1;
                }
            }
            case TAIL_FIRST -> {
                if (tail.size() > 0) {
                    yield head.size() + 1;
                } else if (head.size() > 0) {
                    yield 0;
                } else {
                    yield 1;
                }
            }
            case TAIL_LAST -> size > 0 ? 0 : 1;
            default -> position + 1;
        };
        return Math.floorMod(tmp, size);
    }
    
    @Override
    public int previousIndex() {
        int size = size();
        int tmp = switch (position) {
            case HEAD_FIRST -> size - 1;
            case HEAD_LAST -> {
                if (head.size() > 0) {
                    yield head.size() - 2;
                } else if (tail.size() > 0) {
                    yield tail.size() - 1;
                } else {
                    yield -1;
                }
            }
            case TAIL_FIRST -> head.size() - 1;
            case TAIL_LAST -> {
                if (size > 0) {
                    yield size - 2;
                } else {
                    yield -1;
                }
            }
            default -> position - 1;
        };
        return Math.floorMod(tmp, size);
    }
    
    @Override
    public List<T> get() {
        if (tail.size() == 0) return head.get();
        if (head.size() == 0) return tail.get();
        return listView;
    }
    
    @Override
    public int size() {
        return head.size() + tail.size();
    }
    
    @Override
    public void resetToStart() {
        head.resetToStart();
        tail.resetToStart();
        position = 0;
    }
}