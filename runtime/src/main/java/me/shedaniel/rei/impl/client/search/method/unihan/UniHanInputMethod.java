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

package me.shedaniel.rei.impl.client.search.method.unihan;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod.ExpendedChar;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.common.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class UniHanInputMethod implements InputMethod<IntList> {
    protected final UniHanManager manager;
    protected Int2ObjectMap<List<ExpendedChar>> dataMap = new Int2ObjectOpenHashMap<>();
    
    public UniHanInputMethod(UniHanManager manager) {
        this.manager = manager;
    }
    
    protected abstract String getFieldKey();
    
    protected abstract String getFieldDelimiter();
    
    @Override
    public CompletableFuture<Void> prepare(Executor executor) {
        return this.prepare(executor, p -> {});
    }
    
    @Override
    public CompletableFuture<Void> prepare(Executor executor, ProgressCallback progressCallback) {
        return dispose(executor)
                .thenRunAsync(() -> manager.download(p -> progressCallback.onProgress(p * 0.99)), executor)
                .thenRunAsync(this::load, executor)
                .whenComplete((aVoid, throwable) -> progressCallback.onProgress(1.0));
    }
    
    public void load() {
        try {
            manager.load((codepoint, fieldKey, data) -> {
                if (fieldKey.equals(getFieldKey())) {
                    String[] strings = data.split(getFieldDelimiter());
                    List<ExpendedChar> sequences = dataMap.computeIfAbsent(codepoint, value -> new ArrayList<>(strings.length));
                    for (String string : strings) {
                        sequences.addAll(asExpendedChars(string));
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected List<ExpendedChar> asExpendedChars(String string) {
        return List.of(new ExpendedChar(CollectionUtils.map(IntList.of(string.codePoints().toArray()), IntList::of)));
    }
    
    @Override
    public CompletableFuture<Void> dispose(Executor executor) {
        return CompletableFuture.runAsync(dataMap::clear, executor);
    }
}
