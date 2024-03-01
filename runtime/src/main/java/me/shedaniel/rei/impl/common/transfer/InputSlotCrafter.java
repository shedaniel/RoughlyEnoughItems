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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public abstract class InputSlotCrafter<T extends AbstractContainerMenu, C extends Container> {
    protected T container;
    private Iterable<SlotAccessor> inputStacks;
    private Iterable<SlotAccessor> inventoryStacks;
    protected ServerPlayer player;
    
    protected InputSlotCrafter(T container) {
        this.container = container;
    }
    
    public void fillInputSlots(ServerPlayer player, boolean hasShift) {
        this.player = player;
        this.inventoryStacks = this.getInventorySlots();
        this.inputStacks = this.getInputSlots();
        
        // Return the already placed items on the grid
        this.cleanInputs();
        
        RecipeFinder recipeFinder = new RecipeFinder();
        this.populateRecipeFinder(recipeFinder);
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (InputIngredient<ItemStack> itemStacks : this.getInputs()) {
            ingredients.add(CollectionUtils.toIngredient(itemStacks.get()));
        }
        
        if (recipeFinder.findRecipe(ingredients, null)) {
            this.fillInputSlots(recipeFinder, ingredients, hasShift);
        } else {
            this.cleanInputs();
            this.markDirty();
            throw new NotEnoughMaterialsException();
        }
        
        this.markDirty();
    }
    
    protected abstract Iterable<SlotAccessor> getInputSlots();
    
    protected abstract Iterable<SlotAccessor> getInventorySlots();
    
    protected abstract List<InputIngredient<ItemStack>> getInputs();
    
    protected abstract void populateRecipeFinder(RecipeFinder recipeFinder);
    
    protected abstract void markDirty();
    
    public void alignRecipeToGrid(Iterable<SlotAccessor> inputStacks, Iterator<Integer> recipeItemIds, int craftsAmount) {
        for (SlotAccessor inputStack : inputStacks) {
            if (!recipeItemIds.hasNext()) {
                return;
            }
            
            this.acceptAlignedInput(recipeItemIds.next(), inputStack, craftsAmount);
        }
    }
    
    public void acceptAlignedInput(Integer recipeItemId, SlotAccessor inputStack, int craftsAmount) {
        ItemStack toBeTakenStack = RecipeFinder.getStackFromId(recipeItemId);
        if (!toBeTakenStack.isEmpty()) {
            for (int i = 0; i < craftsAmount; ++i) {
                this.fillInputSlot(inputStack, toBeTakenStack);
            }
        }
    }
    
    protected void fillInputSlot(SlotAccessor slot, ItemStack toBeTakenStack) {
        SlotAccessor takenSlot = this.takeInventoryStack(toBeTakenStack);
        if (takenSlot != null) {
            ItemStack takenStack = takenSlot.getItemStack().copy();
            if (!takenStack.isEmpty()) {
                if (takenStack.getCount() > 1) {
                    takenSlot.takeStack(1);
                } else {
                    takenSlot.setItemStack(ItemStack.EMPTY);
                }
                
                takenStack.setCount(1);
                if (slot.getItemStack().isEmpty()) {
                    slot.setItemStack(takenStack);
                } else {
                    slot.getItemStack().grow(1);
                }
            }
        }
    }
    
    protected void fillInputSlots(RecipeFinder recipeFinder, NonNullList<Ingredient> ingredients, boolean hasShift) {
        int recipeCrafts = recipeFinder.countRecipeCrafts(ingredients, null);
        int amountToFill = hasShift ? recipeCrafts : 1;
        IntList recipeItemIds = new IntArrayList();
        if (recipeFinder.findRecipe(ingredients, recipeItemIds, amountToFill)) {
            int finalCraftsAmount = amountToFill;
            
            for (int itemId : recipeItemIds) {
                finalCraftsAmount = Math.min(finalCraftsAmount, RecipeFinder.getStackFromId(itemId).getMaxStackSize());
            }
            
            if (recipeFinder.findRecipe(ingredients, recipeItemIds, finalCraftsAmount)) {
                this.cleanInputs();
                this.alignRecipeToGrid(inputStacks, recipeItemIds.iterator(), finalCraftsAmount);
            }
        }
    }
    
    protected abstract void cleanInputs();
    
    @Nullable
    public SlotAccessor takeInventoryStack(ItemStack itemStack) {
        for (SlotAccessor inventoryStack : inventoryStacks) {
            ItemStack itemStack1 = inventoryStack.getItemStack();
            if (!itemStack1.isEmpty() && areItemsEqual(itemStack, itemStack1) && !itemStack1.isDamaged() && !itemStack1.isEnchanted() && !itemStack1.has(DataComponents.CUSTOM_NAME)) {
                return inventoryStack;
            }
        }
        
        return null;
    }
    
    private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }
    
    public static class NotEnoughMaterialsException extends RuntimeException {
    }
}
