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

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
@ApiStatus.Internal
public class FluidSupportProviderImpl implements FluidSupportProvider {
    private final List<FluidProvider> providers = Lists.newCopyOnWriteArrayList();
    
    public void reset() {
        providers.clear();
    }
    
    @Override
    public void registerFluidProvider(@NotNull FluidProvider provider) {
        providers.add(Objects.requireNonNull(provider, "Registered provider is null!"));
    }
    
    @Override
    public @NotNull EntryStack itemToFluid(@NotNull EntryStack itemStack) {
        if (itemStack.isEmpty()) return EntryStack.empty();
        if (itemStack.getType() != EntryStack.Type.ITEM)
            throw new IllegalArgumentException("EntryStack must be item!");
        for (FluidProvider provider : providers) {
            EntryStack stack = Objects.requireNonNull(provider.itemToFluid(itemStack));
            if (!stack.isEmpty()) return stack;
        }
        return EntryStack.empty();
    }
}
