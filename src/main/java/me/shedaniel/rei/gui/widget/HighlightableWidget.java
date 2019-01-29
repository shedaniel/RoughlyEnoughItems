package me.shedaniel.rei.gui.widget;

import java.awt.*;

public interface HighlightableWidget extends IWidget {
    
    public Rectangle getBounds();
    
    default boolean isHighlighted(int mouseX, int mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    default boolean isHighlighted(Point point) {
        return this.isHighlighted(point.x, point.y);
    }
    
    default boolean isHighlighted(double mouseX, double mouseY) {
        return this.isHighlighted((int) mouseX, (int) mouseY);
    }
    
}
