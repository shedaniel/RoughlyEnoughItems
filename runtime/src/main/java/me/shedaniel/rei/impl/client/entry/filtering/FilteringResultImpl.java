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

package me.shedaniel.rei.impl.client.entry.filtering;

import com.google.common.collect.Sets;
import me.shedaniel.rei.api.client.entry.filtering.FilteringResult;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class FilteringResultImpl implements FilteringResult {
    public final Set<HashedEntryStackWrapper> hiddenStacks, shownStacks;
    
    public FilteringResultImpl(List<? extends EntryStack<?>> hiddenStacks, List<? extends EntryStack<?>> shownStacks) {
        this.hiddenStacks = Sets.newHashSetWithExpectedSize(hiddenStacks.size());
        this.shownStacks = Sets.newHashSetWithExpectedSize(shownStacks.size());
        hide(hiddenStacks);
        show(shownStacks);
    }
    
    @Override
    public FilteringResult hide(EntryStack<?> stack) {
        this.hiddenStacks.add(new HashedEntryStackWrapper(stack));
        return this;
    }
    
    @Override
    public FilteringResult hide(Collection<? extends EntryStack<?>> stacks) {
        this.hiddenStacks.addAll(CollectionUtils.map(stacks, HashedEntryStackWrapper::new));
        return this;
    }
    
    @Override
    public FilteringResult show(EntryStack<?> stack) {
        this.shownStacks.add(new HashedEntryStackWrapper(stack));
        return this;
    }
    
    @Override
    public FilteringResult show(Collection<? extends EntryStack<?>> stacks) {
        this.shownStacks.addAll(CollectionUtils.map(stacks, HashedEntryStackWrapper::new));
        return this;
    }
}
