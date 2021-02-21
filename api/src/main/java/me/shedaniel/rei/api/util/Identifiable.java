package me.shedaniel.rei.api.util;

import net.minecraft.resources.ResourceLocation;

public interface Identifiable {
    /**
     * Returns the unique identifier.
     *
     * @return the unique identifier
     */
    ResourceLocation getIdentifier();
}
