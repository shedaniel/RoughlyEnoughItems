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

package me.shedaniel.rei.impl.common.entry.comparison;

import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemComparatorRegistryImpl extends EntryComparatorRegistryImpl<ItemStack, Item> implements ItemComparatorRegistry {
    private final EntryComparator<ItemStack> itemNbt = EntryComparator.itemNbt();
    private final EntryComparator<ItemStack> defaultComparator = (context, stack) -> {
        if (context.isExact()) {
            return itemNbt.hash(context, stack);
        } else {
            return 1;
        }
    };
    
    @Override
    public Item getEntry(ItemStack stack) {
        return stack.getItem();
    }
    
    @Override
    public EntryComparator<ItemStack> defaultComparator() {
        return defaultComparator;
    }
    
    @Override
    public void acceptPlugin(REIPlugin<?> plugin) {
        plugin.registerItemComparators(this);
    }
}
