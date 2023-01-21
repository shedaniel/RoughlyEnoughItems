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

package me.shedaniel.rei.api.common.entry.comparison;

import me.shedaniel.rei.api.common.plugins.PluginManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * {@inheritDoc}
 *
 * @see me.shedaniel.rei.api.common.plugins.REIPlugin#registerItemComparators(ItemComparatorRegistry)
 */
public interface ItemComparatorRegistry extends EntryComparatorRegistry<ItemStack, Item> {
    /**
     * @return the instance of {@link ItemComparatorRegistry}
     */
    static ItemComparatorRegistry getInstance() {
        return PluginManager.getInstance().get(ItemComparatorRegistry.class);
    }
    
    /**
     * Registers an item to compare via its nbt.
     *
     * @param item the item to compare
     */
    default void registerNbt(Item item) {
        register(EntryComparator.itemNbt(), item);
    }
    
    /**
     * Registers items to compare via their nbt.
     *
     * @param items the items to compare
     */
    default void registerNbt(Item... items) {
        register(EntryComparator.itemNbt(), items);
    }
}
