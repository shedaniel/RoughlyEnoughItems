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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class InputSlotCrafter<C extends Container> implements RecipeGridAligner<Integer>, ContainerContext {
    
    protected AbstractContainerMenu container;
    protected ContainerInfo containerInfo;
    private List<StackAccessor> gridStacks;
    private List<StackAccessor> inventoryStacks;
    private ServerPlayer player;
    
    private InputSlotCrafter(AbstractContainerMenu container, ContainerInfo<? extends AbstractContainerMenu> containerInfo) {
        this.container = container;
        this.containerInfo = containerInfo;
    }
    
    public static <C extends Container> void start(ResourceLocation category, AbstractContainerMenu craftingContainer_1, ServerPlayer player, NonNullList<List<ItemStack>> map, boolean hasShift) {
        ContainerInfo<? extends AbstractContainerMenu> containerInfo = Objects.requireNonNull(ContainerInfoHandler.getContainerInfo(category, craftingContainer_1.getClass()), "Container Info does not exist on the server!");
        new InputSlotCrafter<C>(craftingContainer_1, containerInfo).fillInputSlots(player, map, hasShift);
    }
    
    private void fillInputSlots(ServerPlayer player, NonNullList<List<ItemStack>> map, boolean hasShift) {
        this.player = player;
        this.inventoryStacks = this.containerInfo.getInventoryStacks(this);
        this.gridStacks = this.containerInfo.getGridStacks(this);
        
        player.ignoreSlotUpdateHack = true;
        // Return the already placed items on the grid
        this.returnInputs();
        
        RecipeFinder recipeFinder = new RecipeFinder();
        this.containerInfo.getRecipeFinderPopulator().populate(this).accept(recipeFinder);
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (List<ItemStack> itemStacks : map) {
            ingredients.add(Ingredient.of(CollectionUtils.map(itemStacks, ItemStack::getItem).toArray(new ItemLike[0])));
        }
        
        if (recipeFinder.findRecipe(ingredients, null)) {
            this.fillInputSlots(recipeFinder, ingredients, hasShift);
        } else {
            this.returnInputs();
            player.ignoreSlotUpdateHack = false;
            this.containerInfo.markDirty(this);
            throw new NotEnoughMaterialsException();
        }
        
        player.ignoreSlotUpdateHack = false;
        this.containerInfo.markDirty(this);
    }
    
    @Override
    public void acceptAlignedInput(Iterator<Integer> iterator_1, StackAccessor gridSlot, int craftsAmount) {
        ItemStack toBeTakenStack = RecipeFinder.getStackFromId(iterator_1.next());
        if (!toBeTakenStack.isEmpty()) {
            for (int i = 0; i < craftsAmount; ++i) {
                this.fillInputSlot(gridSlot, toBeTakenStack);
            }
        }
    }
    
    protected void fillInputSlot(StackAccessor slot, ItemStack toBeTakenStack) {
        int takenSlotIndex = this.method_7371(toBeTakenStack);
        if (takenSlotIndex != -1) {
            ItemStack takenStack = this.inventoryStacks.get(takenSlotIndex).getItemStack().copy();
            if (!takenStack.isEmpty()) {
                if (takenStack.getCount() > 1) {
                    this.inventoryStacks.get(takenSlotIndex).takeStack(1);
                } else {
                    this.inventoryStacks.get(takenSlotIndex).setItemStack(ItemStack.EMPTY);
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
        int amountToFill = this.getAmountToFill(hasShift, recipeCrafts, false);
        IntList intList_1 = new IntArrayList();
        if (recipeFinder.findRecipe(ingredients, intList_1, amountToFill)) {
            int finalCraftsAmount = amountToFill;
            
            for (int itemId : intList_1) {
                finalCraftsAmount = Math.min(finalCraftsAmount, RecipeFinder.getStackFromId(itemId).getMaxStackSize());
            }
            
            if (recipeFinder.findRecipe(ingredients, intList_1, finalCraftsAmount)) {
                this.returnInputs();
                this.alignRecipeToGrid(gridStacks, intList_1.iterator(), finalCraftsAmount);
            }
        }
        
    }
    
    protected int getAmountToFill(boolean hasShift, int recipeCrafts, boolean boolean_2) {
        int amountToFill = 1;
        if (hasShift) {
            amountToFill = recipeCrafts;
        } else if (boolean_2) {
            amountToFill = 64;
            for (StackAccessor stackAccessor : gridStacks) {
                ItemStack itemStack = stackAccessor.getItemStack();
                if (!itemStack.isEmpty() && amountToFill > itemStack.getCount()) {
                    amountToFill = itemStack.getCount();
                }
            }
            if (amountToFill < 64) {
                ++amountToFill;
            }
        }
        return amountToFill;
    }
    
    protected void returnInputs() {
        this.containerInfo.getGridCleanHandler().clean(this);
    }
    
    public int method_7371(ItemStack itemStack) {
        for (int i = 0; i < inventoryStacks.size(); i++) {
            ItemStack itemStack1 = this.inventoryStacks.get(i).getItemStack();
            if (!itemStack1.isEmpty() && areItemsEqual(itemStack, itemStack1) && !itemStack1.isDamaged() && !itemStack1.isEnchanted() && !itemStack1.hasCustomHoverName()) {
                return i;
            }
        }
        
        return -1;
    }
    
    private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && ItemStack.tagMatches(stack1, stack2);
    }
    
    private int getFreeInventorySlots() {
        int int_1 = 0;
        for (StackAccessor inventoryStack : inventoryStacks) {
            if (inventoryStack.getItemStack().isEmpty()) {
                ++int_1;
            }
        }
        return int_1;
    }
    
    @Override
    public AbstractContainerMenu getContainer() {
        return container;
    }
    
    @Override
    public Player getPlayerEntity() {
        return player;
    }
    
    @Override
    public ContainerInfo getContainerInfo() {
        return containerInfo;
    }
    
    public static class NotEnoughMaterialsException extends RuntimeException {}
    
}
