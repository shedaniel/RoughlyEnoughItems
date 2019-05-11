/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContainerScreen.class)
public interface ContainerScreenHooks {
    
    @Accessor("left")
    int rei_getContainerLeft();
    
    @Accessor("top")
    int rei_getContainerTop();
    
    @Accessor("containerWidth")
    int rei_getContainerWidth();
    
    @Accessor("containerHeight")
    int rei_getContainerHeight();
    
    @Accessor("focusedSlot")
    Slot rei_getHoveredSlot();
    
}
