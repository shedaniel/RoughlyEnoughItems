package me.shedaniel.rei.gui.widget;

import java.awt.*;

public interface HighlightableWidget extends IWidget {
    
    public Rectangle getBounds();
    
    default boolean isHighlighted(int mouseX, int mouseY) {
        return getBounds().contains(new Point(mouseX, mouseY));
    }
    
}
