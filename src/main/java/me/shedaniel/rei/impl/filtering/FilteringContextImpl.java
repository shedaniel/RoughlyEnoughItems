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

import com.google.common.collect.Maps;
import me.shedaniel.rei.api.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;

@Environment(EnvType.CLIENT)
public class FilteringContextImpl implements FilteringContext {
    private final Map<FilteringContextType, Set<EntryStack>> stacks;
    
    public FilteringContextImpl(List<EntryStack> allStacks) {
        this(Maps.newHashMap());
        getUnsetStacks().addAll(allStacks);
    }
    
    public FilteringContextImpl(Map<FilteringContextType, Set<EntryStack>> stacks) {
        this.stacks = stacks;
        for (FilteringContextType type : FilteringContextType.values()) {
            this.stacks.computeIfAbsent(type, t -> new TreeSet<>(Comparator.comparing(EntryStack::hashIgnoreAmount)));
        }
    }
    
    @Override
    public Map<FilteringContextType, Set<EntryStack>> getStacks() {
        return stacks;
    }
    
    public void handleResult(FilteringResult result) {
        getUnsetStacks().removeAll(result.getHiddenStacks());
        getShownStacks().removeAll(result.getHiddenStacks());
        getHiddenStacks().addAll(result.getHiddenStacks());
        
        getHiddenStacks().removeAll(result.getShownStacks());
        getUnsetStacks().removeAll(result.getShownStacks());
        getShownStacks().addAll(result.getShownStacks());
    }
}
