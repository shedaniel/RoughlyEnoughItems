/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenHooks {

    @Accessor("x")
    int rei_getContainerLeft();

    @Accessor("y")
    int rei_getContainerTop();

    @Accessor("containerWidth")
    int rei_getContainerWidth();

    @Accessor("containerHeight")
    int rei_getContainerHeight();

    @Accessor("focusedSlot")
    Slot rei_getHoveredSlot();

}
