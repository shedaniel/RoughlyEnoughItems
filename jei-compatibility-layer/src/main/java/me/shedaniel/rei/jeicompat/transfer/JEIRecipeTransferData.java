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

package me.shedaniel.rei.jeicompat.transfer;

import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class JEIRecipeTransferData<C extends AbstractContainerMenu, R> {
    /**
     * Return the recipe category that this container can handle.
     */
    private final ResourceLocation recipeCategoryUid;
    
    /**
     * Return a list of slots for the recipe area.
     */
    private final List<Slot> recipeSlots;
    
    /**
     * Return a list of slots that the transfer can use to get items for crafting, or place leftover items.
     */
    private final List<Slot> inventorySlots;
    
    /**
     * Return false if the recipe transfer should attempt to place as many items as possible for all slots, even if one slot has less.
     */
    private final boolean requireCompleteSets;
    
    public JEIRecipeTransferData(ResourceLocation recipeCategoryUid, List<Slot> recipeSlots, List<Slot> inventorySlots, boolean requireCompleteSets) {
        this.recipeCategoryUid = recipeCategoryUid;
        this.recipeSlots = recipeSlots;
        this.inventorySlots = inventorySlots;
        this.requireCompleteSets = requireCompleteSets;
    }
    
    @OnlyIn(Dist.CLIENT)
    public JEIRecipeTransferData(IRecipeTransferInfo<C, R> info, C container, R recipe) {
        this(info.getRecipeType().getUid(), info.getRecipeSlots(container, recipe),
                info.getInventorySlots(container, recipe), info.requireCompleteSets(container, recipe));
    }
    
    public static <C extends AbstractContainerMenu, R> JEIRecipeTransferData<C, R> read(C menu, CompoundTag tag) {
        ResourceLocation recipeCategoryUid = ResourceLocation.tryParse(tag.getString("recipeCategoryUid"));
        List<Slot> recipeSlots = readSlots(menu, tag.getList("recipeSlots", Tag.TAG_COMPOUND));
        List<Slot> inventorySlots = readSlots(menu, tag.getList("inventorySlots", Tag.TAG_COMPOUND));
        boolean requireCompleteSets = tag.getBoolean("requireCompleteSets");
        return new JEIRecipeTransferData<>(recipeCategoryUid, recipeSlots, inventorySlots, requireCompleteSets);
    }
    
    public CompoundTag save(CompoundTag tag) {
        tag.putString("recipeCategoryUid", recipeCategoryUid.toString());
        tag.put("recipeSlots", writeSlots(recipeSlots));
        tag.put("inventorySlots", writeSlots(inventorySlots));
        tag.putBoolean("requireCompleteSets", requireCompleteSets);
        return tag;
    }
    
    public static <C extends AbstractContainerMenu> List<Slot> readSlots(C menu, ListTag tag) {
        List<Slot> slots = new ArrayList<>();
        for (Tag entry : tag) {
            slots.add(menu.getSlot(((CompoundTag) entry).getInt("Index")));
        }
        return slots;
    }
    
    public ListTag writeSlots(List<Slot> slots) {
        ListTag listTag = new ListTag();
        for (Slot slot : slots) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Index", slot.index);
            listTag.add(entry);
        }
        return listTag;
    }
    
    public ResourceLocation getRecipeCategoryUid() {
        return recipeCategoryUid;
    }
    
    public List<Slot> getRecipeSlots() {
        return recipeSlots;
    }
    
    public List<Slot> getInventorySlots() {
        return inventorySlots;
    }
    
    public boolean isRequireCompleteSets() {
        return requireCompleteSets;
    }
}