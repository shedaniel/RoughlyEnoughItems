package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.Entry;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class FluidEntry implements Entry {
    private Fluid fluid;
    
    @Deprecated
    public FluidEntry(Fluid fluid) {
        this.fluid = fluid;
    }
    
    @Override
    public Type getEntryType() {
        return Type.FLUID;
    }
    
    @Nullable
    @Override
    public ItemStack getItemStack() {
        return null;
    }
    
    @Nullable
    @Override
    public Fluid getFluid() {
        return fluid;
    }
}
