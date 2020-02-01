/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.gui.widget.Widget;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface TransferRecipeCategory<T extends RecipeDisplay> extends RecipeCategory<T> {
    @ApiStatus.OverrideOnly
    void renderRedSlots(List<Widget> widgets, Rectangle bounds, T display, IntList redSlots);
}
