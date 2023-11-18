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

package me.shedaniel.rei.impl.client;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.CodepointMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public class CodepointMapWrapper<T> extends CodepointMap<T> {
    private final CodepointMap<T> delegate;
    protected transient IntSet keys;
    
    public CodepointMapWrapper(CodepointMap<T> delegate) {
        super(delegate.blockConstructor, i -> delegate.blockMap);
        this.empty = delegate.empty;
        this.blockMap = delegate.blockMap;
        this.delegate = delegate;
    }
    
    @Override
    public void clear() {
        synchronized (this) {
            delegate.clear();
        }
    }
    
    @Nullable
    @Override
    public T put(int i, T object) {
        synchronized (this) {
            return delegate.put(i, object);
        }
    }
    
    @Nullable
    @Override
    public T get(int i) {
        synchronized (this) {
            return delegate.get(i);
        }
    }
    
    @Override
    public T computeIfAbsent(int i, IntFunction<T> intFunction) {
        synchronized (this) {
            return delegate.computeIfAbsent(i, intFunction);
        }
    }
    
    @Nullable
    @Override
    public T remove(int i) {
        synchronized (this) {
            return delegate.remove(i);
        }
    }
    
    @Override
    public void forEach(Output<T> arg) {
        synchronized (this) {
            delegate.forEach(arg);
        }
    }
    
    @Override
    public IntSet keySet() {
        return delegate.keySet();
    }
}
