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

package me.shedaniel.rei.impl.entry;

import me.shedaniel.rei.api.ingredient.entry.comparison.ItemComparator;
import me.shedaniel.rei.api.ingredient.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.plugins.REIPlugin;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

@ApiStatus.Internal
public class ItemComparatorRegistryImpl implements ItemComparatorRegistry {
    private static final Logger LOGGER = LogManager.getLogger(ItemComparatorRegistryImpl.class);
    private final Map<Item, ItemComparator> comparators = new IdentityHashMap<>();
    
    @Override
    public void register(ItemComparator comparator, Item item) {
        ItemComparator put = this.comparators.put(item, comparator);
        if (put != null) {
            LOGGER.warn("[REI] Overriding " + put + "item comparator with " + comparator + "for " + Registry.ITEM.getKey(item) + "! This may result in unwanted comparisons!");
        }
    }
    
    @Override
    public void startReload() {
        comparators.clear();
    }
    
    @Override
    public void acceptPlugin(REIPlugin plugin) {
        plugin.registerItemComparators(this);
    }
    
    @Override
    public long hashOf(ItemStack stack) {
        ItemComparator comparator = comparators.get(stack.getItem());
        if (comparator != null) {
            return comparator.hash(stack);
        }
        return 1;
    }
}
