package me.shedaniel.rei.listeners;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BucketItem.class)
public interface BucketItemHooks {
    @Accessor("fluid")
    Fluid getFluid();
}
