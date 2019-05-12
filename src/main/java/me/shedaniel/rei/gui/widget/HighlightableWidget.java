/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import java.awt.*;

public abstract class HighlightableWidget extends Widget {
    
    abstract public Shape getBounds();
    
    public final boolean isHighlighted(int mouseX, int mouseY) {
        return isHighlighted((double) mouseX, (double) mouseY);
    }
    
    public final boolean isHighlighted(Point point) {
        return isHighlighted(point.x, point.y);
    }
    
    public boolean isHighlighted(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Override
    public boolean isMouseOver(double double_1, double double_2) {
        return isHighlighted(double_1, double_2);
    }
    
}
