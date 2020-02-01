/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.widget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface LateRenderable {
    void lateRender(int mouseX, int mouseY, float delta);
}
