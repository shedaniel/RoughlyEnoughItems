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

package me.shedaniel.rei.api.common.transfer.info.clean;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.simple.DumpHandler;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface InputCleanHandler<T extends AbstractContainerMenu, D extends Display> {
    void clean(MenuInfoContext<T, ? extends ServerPlayer, D> context);
    
    static void error(String translationKey) {
        throw new IllegalStateException(translationKey);
    }
    
    static <T extends AbstractContainerMenu> void returnSlotsToPlayerInventory(MenuInfoContext<T, ?, ?> context, DumpHandler<T, ?> dumpHandler, SlotAccessor slotAccessor) {
        ItemStack stackToReturn = slotAccessor.getItemStack();
        if (!stackToReturn.isEmpty()) {
            for (; !(stackToReturn = slotAccessor.getItemStack()).isEmpty(); slotAccessor.takeStack(1)) {
                ItemStack stackToInsert = stackToReturn.copy();
                stackToInsert.setCount(1);
                if (!dumpGenericsFtw(context, dumpHandler, stackToInsert)) {
                    error("rei.rei.no.slot.in.inv");
                }
            }
        }
    }
    
    @ApiStatus.Internal
    static <T extends AbstractContainerMenu, D extends Display, D2 extends Display> boolean dumpGenericsFtw(MenuInfoContext<T, ?, D2> context, DumpHandler<T, D> dumpHandler, ItemStack stackToInsert) {
        return dumpHandler.dump((MenuInfoContext<T, ?, D>) context, stackToInsert);
    }
}
