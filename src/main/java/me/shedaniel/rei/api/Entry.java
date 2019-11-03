/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.api.annotations.ToBeRemoved;
import me.shedaniel.rei.impl.FluidEntry;
import me.shedaniel.rei.impl.ItemStackEntry;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@Deprecated
@ToBeRemoved
public interface Entry extends Cloneable {
    @SuppressWarnings("deprecation")
    static Entry create(ItemStack itemStack) {
        return new ItemStackEntry(itemStack);
    }
    
    @SuppressWarnings("deprecation")
    static Entry create(Fluid fluid) {
        return new FluidEntry(fluid);
    }
    
    Type getEntryType();
    
    @Nullable
    ItemStack getItemStack();
    
    @Nullable
    Fluid getFluid();
    
    Entry clone();
    
    default EntryStack toEntryStack() {
        if (getEntryType() == Type.ITEM)
            return EntryStack.create(getItemStack());
        if (getEntryType() == Type.FLUID)
            return EntryStack.create(getFluid());
        return EntryStack.empty();
    }
    
    boolean equalsEntry(Entry other, boolean checkTags);
    
    public static enum Type {
        ITEM, FLUID
    }
}
