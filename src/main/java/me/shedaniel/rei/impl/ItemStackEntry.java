/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.Entry;
import me.shedaniel.rei.api.annotations.ToBeRemoved;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@ToBeRemoved
@Deprecated
public class ItemStackEntry implements Entry {
    private ItemStack itemStack;
    
    @Deprecated
    public ItemStackEntry(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @Override
    public Type getEntryType() {
        return Type.ITEM;
    }
    
    @Nullable
    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }
    
    @Nullable
    @Override
    public Fluid getFluid() {
        return null;
    }
    
    @Override
    public Entry clone() {
        return Entry.create(getItemStack().copy());
    }
    
    @Override
    public boolean equalsEntry(Entry other, boolean checkTags) {
        if (other.getEntryType() == Type.ITEM) {
            return checkTags ? ItemStack.areEqualIgnoreDamage(other.getItemStack(), getItemStack()) : other.getItemStack().isItemEqualIgnoreDamage(getItemStack());
        } else return false;
    }
}
