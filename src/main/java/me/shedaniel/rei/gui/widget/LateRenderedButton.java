/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class LateRenderedButton extends ButtonWidget implements LateRenderable {
    protected LateRenderedButton(Rectangle rectangle, Text text) {
        super(rectangle, text);
    }
}
