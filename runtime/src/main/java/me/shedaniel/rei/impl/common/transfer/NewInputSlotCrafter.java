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

package me.shedaniel.rei.impl.common.transfer;

import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class NewInputSlotCrafter<T extends AbstractContainerMenu, C extends Container> extends InputSlotCrafter<T, C> {
    protected final List<SlotAccessor> inputSlots;
    protected final List<SlotAccessor> inventorySlots;
    protected final List<InputIngredient<ItemStack>> inputs;
    
    public NewInputSlotCrafter(T container, List<SlotAccessor> inputSlots, List<SlotAccessor> inventorySlots, List<InputIngredient<ItemStack>> inputs) {
        super(container);
        this.inputSlots = inputSlots;
        this.inventorySlots = inventorySlots;
        this.inputs = inputs;
    }
    
    @Override
    protected Iterable<SlotAccessor> getInputSlots() {
        return this.inputSlots;
    }
    
    @Override
    protected Iterable<SlotAccessor> getInventorySlots() {
        return this.inventorySlots;
    }
    
    @Override
    protected List<InputIngredient<ItemStack>> getInputs() {
        return this.inputs;
    }
    
    @Override
    protected void populateRecipeFinder(RecipeFinder recipeFinder) {
        for (SlotAccessor slot : getInventorySlots()) {
            recipeFinder.addNormalItem(slot.getItemStack());
        }
    }
    
    @Override
    protected void markDirty() {
        player.getInventory().setChanged();
        container.sendAllDataToRemote();
    }
    
    @Override
    protected void cleanInputs() {
        for (SlotAccessor slot : getInputSlots()) {
            ItemStack stackToReturn = slot.getItemStack();
            if (!stackToReturn.isEmpty()) {
                for (; !(stackToReturn = slot.getItemStack()).isEmpty(); slot.takeStack(1)) {
                    ItemStack stackToInsert = stackToReturn.copy();
                    stackToInsert.setCount(1);
                    
                    if (!getDumpHandler().test(stackToInsert)) {
                        throw new IllegalStateException("rei.rei.no.slot.in.inv");
                    }
                }
            }
        }
    }
    
    private Predicate<ItemStack> getDumpHandler() {
        return (stackToDump) -> {
            Iterable<SlotAccessor> inventoryStacks = getInventorySlots();
            SlotAccessor occupiedSlotWithRoomForStack = getOccupiedSlotWithRoomForStack(stackToDump, inventoryStacks);
            SlotAccessor emptySlot = getEmptySlot(inventoryStacks);
            
            SlotAccessor nextSlot = occupiedSlotWithRoomForStack == null ? emptySlot : occupiedSlotWithRoomForStack;
            if (nextSlot == null) {
                return false;
            }
            
            ItemStack stack = stackToDump.copy();
            stack.setCount(nextSlot.getItemStack().getCount() + stack.getCount());
            nextSlot.setItemStack(stack);
            return true;
        };
    }
    
    static SlotAccessor getOccupiedSlotWithRoomForStack(ItemStack stack, Iterable<SlotAccessor> inventoryStacks) {
        for (SlotAccessor inventoryStack : inventoryStacks) {
            if (canStackAddMore(inventoryStack.getItemStack(), stack)) {
                return inventoryStack;
            }
        }
        
        return null;
    }
    
    static SlotAccessor getEmptySlot(Iterable<SlotAccessor> inventoryStacks) {
        for (SlotAccessor inventoryStack : inventoryStacks) {
            if (inventoryStack.getItemStack().isEmpty()) {
                return inventoryStack;
            }
        }
        
        return null;
    }
    
    static boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty() && ItemStack.isSameItemSameTags(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() + stack.getCount() <= existingStack.getMaxStackSize();
    }
}
