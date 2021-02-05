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

package me.shedaniel.rei.api.server;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface GridCleanHandler<T extends AbstractContainerMenu> {
    void clean(ContainerContext<T> context);
    
    static void error(String translationKey) {
        throw new IllegalStateException(translationKey);
    }
    
    static <T extends AbstractContainerMenu> void returnSlotToPlayerInventory(ContainerContext<T> context, StackAccessor stackAccessor) {
        DumpHandler<T> dumpHandler = context.getContainerInfo().getDumpHandler();
        ItemStack stackToReturn = stackAccessor.getItemStack();
        if (!stackToReturn.isEmpty()) {
            for (; stackToReturn.getCount() > 0; stackAccessor.takeStack(1)) {
                ItemStack stackToInsert = stackToReturn.copy();
                stackToInsert.setCount(1);
                if (!dumpHandler.dump(context, stackToInsert)) {
                    error("rei.rei.no.slot.in.inv");
                }
            }
        }
    }
}
