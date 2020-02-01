/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.entries;

import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.RenderingEntry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.OverrideOnly
public abstract class RecipeEntry extends RenderingEntry {
    public abstract QueuedTooltip getTooltip(int mouseX, int mouseY);
    
    public abstract int getHeight();
    
    public final int getWidth() {
        return 100;
    }
}
