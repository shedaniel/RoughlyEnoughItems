/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface ContainerScreenHooks {
    
    int rei_getContainerLeft();
    
    int rei_getContainerTop();
    
    int rei_getContainerWidth();
    
    int rei_getContainerHeight();
    
    Slot rei_getHoveredSlot();
    
}
