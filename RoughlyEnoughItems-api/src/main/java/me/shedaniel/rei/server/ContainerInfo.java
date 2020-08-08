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

package me.shedaniel.rei.server;

import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface ContainerInfo<T extends Container> {
    Class<? extends Container> getContainerClass();
    
    default StackAccessor getStack(ContainerContext<T> context, int slotIndex) {
        return new SlotStackAccessor(context.getContainer().getSlot(slotIndex));
    }
    
    default GridCleanHandler<T> getGridCleanHandler() {
        return context -> {
            T container = context.getContainer();
            for (StackAccessor gridStack : getGridStacks(context)) {
                GridCleanHandler.returnSlotToPlayerInventory(context, gridStack);
            }

            clearCraftingSlots(container);
        };
    }
    
    default DumpHandler<T> getDumpHandler() {
        return (context, stackToInsert) -> {
            List<StackAccessor> inventoryStacks = context.getContainerInfo().getInventoryStacks(context);
            
            StackAccessor nextSlot = DumpHandler.getOccupiedSlotWithRoomForStack(stackToInsert, inventoryStacks);
            if (nextSlot == null) {
                nextSlot = DumpHandler.getEmptySlot(inventoryStacks);
            }
            if (nextSlot == null) {
                return false;
            }
            
            ItemStack stack = stackToInsert.copy();
            stack.setCount(nextSlot.getItemStack().getCount() + stack.getCount());
            nextSlot.setItemStack(stack);
            return true;
        };
    }
    
    default RecipeFinderPopulator<T> getRecipeFinderPopulator() {
        return context -> recipeFinder -> {
            for (StackAccessor inventoryStack : getInventoryStacks(context)) {
                recipeFinder.addNormalItem(inventoryStack.getItemStack());
            }
            populateRecipeFinder(context.getContainer(), recipeFinder);
        };
    }
    
    default List<StackAccessor> getGridStacks(ContainerContext<T> context) {
        return IntStream.range(0, getCraftingWidth(context.getContainer()) * getCraftingHeight(context.getContainer()) + 1)
                .filter(value -> value != getCraftingResultSlotIndex(context.getContainer()))
                .mapToObj(context::getStack)
                .collect(Collectors.toList());
    }
    
    default List<StackAccessor> getInventoryStacks(ContainerContext<T> context) {
        PlayerInventory inventory = context.getPlayerEntity().inventory;
        return IntStream.range(0, inventory.main.size())
                .mapToObj(index -> (StackAccessor) new InventoryStackAccessor(inventory, index))
                .collect(Collectors.toList());
    }
    
    default void markDirty(ContainerContext<T> context) {
        context.getPlayerEntity().inventory.markDirty();
        context.getContainer().sendContentUpdates();
        
        DefaultedList<ItemStack> defaultedList = DefaultedList.of();
        for (Slot slot : context.getPlayerEntity().container.slots) {
            defaultedList.add(slot.getStack());
        }
        
        ((ServerPlayerEntity) context.getPlayerEntity()).onContainerRegistered(context.getPlayerEntity().container, defaultedList);
    }
    
    int getCraftingResultSlotIndex(T container);
    
    int getCraftingWidth(T container);
    
    int getCraftingHeight(T container);
    
    default void clearCraftingSlots(T container) {}
    
    default void populateRecipeFinder(T container, RecipeFinder var1) {}
}
