/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.entries;

import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.RenderingEntry;

public abstract class RecipeEntry extends RenderingEntry {
    public abstract QueuedTooltip getTooltip(int mouseX, int mouseY);
    
    public abstract int getHeight();
    
    public final int getWidth() {
        return 100;
    }
}
