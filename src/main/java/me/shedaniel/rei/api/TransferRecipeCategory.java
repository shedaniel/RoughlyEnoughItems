/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.gui.widget.Widget;

import java.util.List;

public interface TransferRecipeCategory<T extends RecipeDisplay> extends RecipeCategory<T> {
    void renderRedSlots(List<Widget> widgets, Rectangle bounds, T display, IntList redSlots);
}
