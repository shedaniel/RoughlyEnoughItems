/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.server;

import net.minecraft.container.Container;

public interface ContainerInfo<T extends Container> {
    Class<? extends Container> getContainerClass();

    int getCraftingResultSlotIndex(T container);

    int getCraftingWidth(T container);

    int getCraftingHeight(T container);

    void clearCraftingSlots(T container);

    void populateRecipeFinder(T container, RecipeFinder var1);
}
