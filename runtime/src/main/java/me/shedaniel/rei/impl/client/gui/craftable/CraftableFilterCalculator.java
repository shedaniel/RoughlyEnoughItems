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

package me.shedaniel.rei.impl.client.gui.craftable;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.registry.display.DisplayCache;
import me.shedaniel.rei.impl.client.registry.display.DisplayRegistryImpl;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CraftableFilterCalculator implements Predicate<HashedEntryStackWrapper> {
    private final Supplier<DisplayCache> displayCache = Suppliers.memoize(() -> ((DisplayRegistryImpl) DisplayRegistry.getInstance()).cache());
    private Set<Display> checkedCraftableDisplays = Collections.synchronizedSet(new ReferenceOpenHashSet<>());
    private Set<Display> checkedUncraftableDisplays = Collections.synchronizedSet(new ReferenceOpenHashSet<>());
    private LongSet checkedCraftableEntries = LongSets.synchronize(new LongOpenHashSet());
    
    @Override
    public boolean test(HashedEntryStackWrapper wrapper) {
        EntryStack<?> stack = wrapper.unwrap();
        if (stack.getType() != VanillaEntryTypes.ITEM || stack.isEmpty()) return false;
        if (checkedCraftableEntries.contains(wrapper.hashExact())) return true;
        DisplayCache cache = this.displayCache.get();
        for (Display display : cache.getAllDisplaysByOutputs(List.of(stack))) {
            if (checkCraftableCachedByResult(display)) return true;
        }
        return false;
    }
    
    private boolean checkCraftableCachedByDisplay(Display display) {
        if (checkedCraftableDisplays.contains(display)) return true;
        if (checkedUncraftableDisplays.contains(display)) return false;
        boolean checkCraftable = checkCraftable(display);
        if (checkCraftable) {
            checkedCraftableDisplays.add(display);
        } else {
            checkedUncraftableDisplays.add(display);
        }
        return checkCraftable;
    }
    
    private boolean checkCraftableCachedByResult(Display display) {
        boolean checkCraftable = checkCraftableCachedByDisplay(display);
        if (checkCraftable) {
            for (EntryIngredient ingredient : display.getOutputEntries()) {
                for (EntryStack<?> stack : ingredient) {
                    if (stack.getType() != VanillaEntryTypes.ITEM || stack.isEmpty()) continue;
                    checkedCraftableEntries.add(EntryStacks.hashExact(stack));
                }
            }
        }
        return checkCraftable;
    }
    
    private boolean checkCraftable(Display display) {
        return AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, null).successful;
    }

    static Long2LongMap copyIngredients(Long2LongMap ingredients) {
        Long2LongMap copy = new Long2LongOpenHashMap();
        ingredients.forEach((hash, amount) -> copy.put(hash, amount));
        return copy;
    }
}
