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
    
    @Override
    public Entry clone() {
        return this;
    }
    
    @Override
    public boolean equalsEntry(Entry other, boolean checkTags) {
        if (other.getEntryType() == Type.FLUID) {
            return other.getFluid().matchesType(getFluid());
        } else return false;
    }
}
