package me.shedaniel.rei.api.entry;

import me.shedaniel.architectury.fluid.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface VanillaEntryTypes {
    EntryType<ItemStack> ITEM = EntryType.deferred(new ResourceLocation("item"));
    EntryType<FluidStack> FLUID = EntryType.deferred(new ResourceLocation("fluid"));
}
