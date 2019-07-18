/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderers;

import me.shedaniel.rei.api.Renderer;

public abstract class RecipeRenderer extends Renderer {
    
    public abstract int getHeight();
    
    public final int getWidth() {
        return 100;
    }
    
}
