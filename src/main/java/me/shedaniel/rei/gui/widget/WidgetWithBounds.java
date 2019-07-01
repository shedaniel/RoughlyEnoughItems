/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import java.awt.*;

public abstract class WidgetWithBounds extends Widget {
    
    abstract public Shape getBounds();
    
    public final boolean containsMouse(int mouseX, int mouseY) {
        return containsMouse((double) mouseX, (double) mouseY);
    }
    
    public final boolean containsMouse(Point point) {
        return containsMouse(point.x, point.y);
    }
    
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Override
    public boolean isMouseOver(double double_1, double double_2) {
        return containsMouse(double_1, double_2);
    }
    
}
