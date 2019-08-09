package me.shedaniel.rei.client;

import me.shedaniel.rei.api.Entry;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

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
}
