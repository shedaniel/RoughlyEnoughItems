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

package me.shedaniel.rei.impl.filtering;

import me.shedaniel.rei.api.EntryStack;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FilteringResultImpl implements FilteringResult {
    private final Set<EntryStack> hiddenStacks, shownStacks;
    
    public FilteringResultImpl(List<EntryStack> hiddenStacks, List<EntryStack> shownStacks) {
        this.hiddenStacks = new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount));
        this.shownStacks = new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount));
        this.hiddenStacks.addAll(hiddenStacks);
        this.shownStacks.addAll(shownStacks);
    }
    
    @Override
    public Set<EntryStack> getHiddenStacks() {
        return hiddenStacks;
    }
    
    @Override
    public Set<EntryStack> getShownStacks() {
        return shownStacks;
    }
}
