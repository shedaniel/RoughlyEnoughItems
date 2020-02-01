/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import me.shedaniel.math.api.Rectangle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface OptimalEntryStack {
    default void optimisedRenderStart(float delta) {
    }
    
    default void optimisedRenderBase(Rectangle bounds, int mouseX, int mouseY, float delta) {
    }
    
    default void optimisedRenderOverlay(Rectangle bounds, int mouseX, int mouseY, float delta) {
    }
    
    default void optimisedRenderEnd(float delta) {
    }
}
