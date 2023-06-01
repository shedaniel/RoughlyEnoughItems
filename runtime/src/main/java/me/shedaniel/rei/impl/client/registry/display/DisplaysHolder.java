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

package me.shedaniel.rei.impl.client.registry.display;

import com.google.common.collect.Iterables;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.view.ViewsImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DisplaysHolder {
    boolean doesCache();
    
    void add(Display display, @Nullable Object origin);
    
    int size();
    
    Map<CategoryIdentifier<?>, List<Display>> get();
    
    @Nullable
    Object getDisplayOrigin(Display display);
    
    void endReload();
    
    boolean isCached(Display display);
    
    Set<Display> getDisplaysNotCached();
    
    Set<Display> getDisplaysByInput(EntryStack<?> stack);
    
    Set<Display> getDisplaysByOutput(EntryStack<?> stack);
    
    default Iterable<Display> getAllDisplaysByInputs(List<EntryStack<?>> stacks) {
        if (stacks.isEmpty()) return List.of();
        Iterable<Display> inputCached = null;
        if (doesCache()) {
            for (EntryStack<?> stack : stacks) {
                Set<Display> set = getDisplaysByInput(stack);
                inputCached = inputCached == null ? set : Iterables.concat(inputCached, set);
            }
            if (stacks.size() > 1) inputCached = CollectionUtils.distinctReferenceOf(inputCached);
        }
        Collection<Display> notCached = this.getDisplaysNotCached();
        if (notCached.isEmpty()) return inputCached == null ? List.of() : inputCached;
        Iterable<Display> filteredNotCached = Iterables.filter(notCached, display ->
                ViewsImpl.isUsagesFor(null, stacks, display));
        if (inputCached == null) return filteredNotCached;
        return Iterables.concat(inputCached, filteredNotCached);
    }
    
    default Iterable<Display> getAllDisplaysByOutputs(List<EntryStack<?>> stacks) {
        if (stacks.isEmpty()) return List.of();
        Iterable<Display> outputCached = null;
        if (doesCache()) {
            for (EntryStack<?> stack : stacks) {
                Set<Display> set = getDisplaysByOutput(stack);
                outputCached = outputCached == null ? set : Iterables.concat(outputCached, set);
            }
            if (stacks.size() > 1) outputCached = CollectionUtils.distinctReferenceOf(outputCached);
        }
        Collection<Display> notCached = this.getDisplaysNotCached();
        if (notCached.isEmpty()) return outputCached == null ? List.of() : outputCached;
        Iterable<Display> filteredNotCached = Iterables.filter(notCached, display ->
                ViewsImpl.isRecipesFor(null, stacks, display));
        if (outputCached == null) return filteredNotCached;
        return Iterables.concat(outputCached, filteredNotCached);
    }
}
