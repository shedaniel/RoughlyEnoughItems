package me.shedaniel.gui;

import me.shedaniel.api.IDrawable;
import me.shedaniel.gui.widget.Control;

import java.awt.*;

/**
 * Created by James on 7/28/2018.
 */
public abstract class Drawable implements IDrawable {
    protected Rectangle rect;
    
    public Drawable(int x, int y, int width, int height) {
        rect = new Rectangle(x, y, width, height);
    }
    
    public Drawable(Rectangle rect) {
        this.rect = rect;
    }
    
    public abstract void draw();
    
    public boolean isHighlighted() {
        Point mousePoint = REIRenderHelper.getMouseLoc();
        if (rect.contains(mousePoint.x, mousePoint.y)) {
            if (this instanceof Control)
                REIRenderHelper.reiGui.setLastHovered((Control) this);
            return true;
        }
        return false;
    }
}
