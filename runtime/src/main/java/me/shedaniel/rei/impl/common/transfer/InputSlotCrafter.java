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

package me.shedaniel.rei.impl.common.transfer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class InputSlotCrafter<T extends AbstractContainerMenu, C extends Container, D extends Display> implements MenuInfoContext<T, ServerPlayer, D> {
    protected CategoryIdentifier<D> category;
    protected T container;
    protected MenuInfo<T, D> menuInfo;
    private Iterable<SlotAccessor> inputStacks;
    private Iterable<SlotAccessor> inventoryStacks;
    private ServerPlayer player;
    
    private InputSlotCrafter(CategoryIdentifier<D> category, T container) {
        this.category = category;
        this.container = container;
    }
    
    public void setMenuInfo(MenuInfo<T, D> menuInfo) {
        this.menuInfo = menuInfo;
    }
    
    public static <T extends AbstractContainerMenu, C extends Container, D extends Display> InputSlotCrafter<T, C, D> start(CategoryIdentifier<D> category, T menu, ServerPlayer player, CompoundTag display, boolean hasShift) {
        InputSlotCrafter<T, C, D> crafter = new InputSlotCrafter<>(category, menu);
        MenuInfo<T, D> menuInfo = Objects.requireNonNull(MenuInfoRegistry.getInstance().get(category, menu, crafter, display), "Container Info does not exist on the server!");
        crafter.setMenuInfo(menuInfo);
        crafter.fillInputSlots(player, hasShift);
        return crafter;
    }
    
    private void fillInputSlots(ServerPlayer player, boolean hasShift) {
        this.player = player;
        this.inventoryStacks = this.menuInfo.getInventorySlots(this);
        this.inputStacks = this.menuInfo.getInputSlots(this);
        
        // Return the already placed items on the grid
        this.cleanInputs();
        
        RecipeFinder recipeFinder = new RecipeFinder();
        this.menuInfo.getRecipeFinderPopulator().populate(this, recipeFinder);
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (List<ItemStack> itemStacks : this.menuInfo.getInputs(this, true)) {
            ingredients.add(CollectionUtils.toIngredient(itemStacks));
        }
        
        if (recipeFinder.findRecipe(ingredients, null)) {
            this.fillInputSlots(recipeFinder, ingredients, hasShift);
        } else {
            this.cleanInputs();
            this.menuInfo.markDirty(this);
            throw new NotEnoughMaterialsException();
        }
        
        this.menuInfo.markDirty(this);
    }
    
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
    
    protected void cleanInputs() {
        this.menuInfo.getInputCleanHandler().clean(this);
    }
    
    @Nullable
    public SlotAccessor takeInventoryStack(ItemStack itemStack) {
        for (SlotAccessor inventoryStack : inventoryStacks) {
            ItemStack itemStack1 = inventoryStack.getItemStack();
            if (!itemStack1.isEmpty() && areItemsEqual(itemStack, itemStack1) && !itemStack1.isDamaged() && !itemStack1.isEnchanted() && !itemStack1.hasCustomHoverName()) {
                return inventoryStack;
            }
        }
        
        return null;
    }
    
    private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && ItemStack.tagMatches(stack1, stack2);
    }
    
    @Override
    public T getMenu() {
        return container;
    }
    
    @Override
    public ServerPlayer getPlayerEntity() {
        return player;
    }
    
    @Override
    public D getDisplay() {
        return menuInfo.getDisplay();
    }
    
    @Override
    public CategoryIdentifier<D> getCategoryIdentifier() {
        return category;
    }
    
    public static class NotEnoughMaterialsException extends RuntimeException {}
}
