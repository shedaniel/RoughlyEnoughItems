package me.shedaniel.rei.gui.widget;

import java.awt.*;

public abstract class HighlightableWidget extends Widget {
    
    abstract public Shape getBounds();
    
    public boolean isHighlighted(int mouseX, int mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    public boolean isHighlighted(Point point) {
        return this.isHighlighted(point.x, point.y);
    }
    
    public boolean isHighlighted(double mouseX, double mouseY) {
        return this.isHighlighted((int) mouseX, (int) mouseY);
    }
    
}
