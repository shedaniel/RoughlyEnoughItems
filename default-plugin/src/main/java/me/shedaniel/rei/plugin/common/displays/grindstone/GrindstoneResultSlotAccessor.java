package me.shedaniel.rei.plugin.common.displays.grindstone;

import net.minecraft.world.item.ItemStack;

public interface GrindstoneResultSlotAccessor {
    int invokeGetExperienceFromItem(ItemStack stack);
}