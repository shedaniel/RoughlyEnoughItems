/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;

public abstract class WidgetWithBounds extends Widget {
    
    public abstract Rectangle getBounds();
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
}
