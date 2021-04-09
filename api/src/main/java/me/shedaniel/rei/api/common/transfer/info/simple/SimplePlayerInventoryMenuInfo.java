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

package me.shedaniel.rei.api.common.transfer.info.simple;

import com.google.common.base.MoreObjects;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler;
import me.shedaniel.rei.api.common.transfer.info.stack.StackAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A simple implementation of {@link MenuInfo} that provides {@link StackAccessor} by {@link Inventory} of the player.
 * <p>
 * Provides default implementation for {@link SimplePlayerInventoryMenuInfo#getInputCleanHandler()}, which dumps slots from
 * {@link SimplePlayerInventoryMenuInfo#getInputStacks(MenuInfoContext)} to the {@link SimplePlayerInventoryMenuInfo#getDumpHandler()}.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 */
public interface SimplePlayerInventoryMenuInfo<T extends AbstractContainerMenu, D extends Display> extends MenuInfo<T, D> {
    default InputCleanHandler<T, D> getInputCleanHandler() {
        return context -> {
            T container = context.getMenu();
            for (StackAccessor gridStack : getInputStacks(context)) {
                InputCleanHandler.returnSlotsToPlayerInventory(context, getDumpHandler(), gridStack);
            }
            
            clearInputSlots(container);
        };
    }
    
    default DumpHandler<T, D> getDumpHandler() {
        return (context, stackToDump) -> {
            Iterable<StackAccessor> inventoryStacks = getInventoryStacks(context);
            
            StackAccessor nextSlot = MoreObjects.firstNonNull(
                    DumpHandler.getOccupiedSlotWithRoomForStack(stackToDump, inventoryStacks),
                    DumpHandler.getEmptySlot(inventoryStacks)
            );
            if (nextSlot == null) {
                return false;
            }
            
            ItemStack stack = stackToDump.copy();
            stack.setCount(nextSlot.getItemStack().getCount() + stack.getCount());
            nextSlot.setItemStack(stack);
            return true;
        };
    }
    
    default Iterable<StackAccessor> getInventoryStacks(MenuInfoContext<T, ?, D> context) {
        Inventory inventory = context.getPlayerEntity().getInventory();
        return IntStream.range(0, inventory.items.size())
                .mapToObj(index -> StackAccessor.fromContainer(inventory, index))
                .collect(Collectors.toList());
    }
    
    default void clearInputSlots(T menu) {}
}
