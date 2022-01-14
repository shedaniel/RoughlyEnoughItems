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

package me.shedaniel.rei.api.common.transfer.info.stack;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Simple implementation of {@link SlotAccessor} that wraps around a slot in {@link Container}.
 */
public class ContainerSlotAccessor implements SlotAccessor {
    protected Container container;
    protected int index;
    
    public ContainerSlotAccessor(Container container, int index) {
        this.container = container;
        this.index = index;
    }
    
    @Override
    public ItemStack getItemStack() {
        return container.getItem(index);
    }
    
    @Override
    public void setItemStack(ItemStack stack) {
        this.container.setItem(index, stack);
    }
    
    @Override
    public ItemStack takeStack(int amount) {
        return this.container.removeItem(index, amount);
    }
}
