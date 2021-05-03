/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Registry for registering custom methods for identifying variants of {@link net.minecraft.world.item.ItemStack}.
 * The default comparator is {@link ItemComparator#noop()}, which does not compare the NBT of the items.
 * <p>
 * This comparator is used when the comparison context is {@link ComparisonContext#EXACT}.
 */
public interface ItemComparatorRegistry extends Reloadable<REIPlugin<?>> {
    /**
     * @return the instance of {@link ItemComparatorRegistry}
     */
    static ItemComparatorRegistry getInstance() {
        return PluginManager.getInstance().get(ItemComparatorRegistry.class);
    }
    
    void register(ItemComparator comparator, Item item);
    
    default void register(ItemComparator comparator, Item... items) {
        for (Item item : items) {
            register(comparator, item);
        }
    }
    
    default void registerNbt(Item item) {
        register(ItemComparator.itemNbt(), item);
    }
    
    default void registerNbt(Item... items) {
        register(ItemComparator.itemNbt(), items);
    }
    
    long hashOf(ComparisonContext context, ItemStack stack);
    
    boolean containsComparator(Item item);
    
    int comparatorSize();
}
