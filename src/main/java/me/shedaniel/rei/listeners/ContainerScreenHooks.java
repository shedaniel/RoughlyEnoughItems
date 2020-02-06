/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@ApiStatus.Internal
@Mixin(ContainerScreen.class)
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
