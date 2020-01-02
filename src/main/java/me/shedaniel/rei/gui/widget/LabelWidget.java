/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.Element;

import java.util.Collections;
import java.util.List;

public class LabelWidget extends WidgetWithBounds {
    
    private Point pos;
    private String text;
    private int defaultColor;
    private boolean hasShadows = true;
    private boolean centered = true;
    
    @Deprecated
    public LabelWidget(int x, int y, String text) {
        this(new Point(x, y), text);
    }
    
    public LabelWidget(Point point, String text) {
        this.pos = point;
        this.text = text;
        this.defaultColor = ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : -1;
    }
    
    public boolean isCentered() {
        return centered;
    }
    
    public void setCentered(boolean centered) {
        this.centered = centered;
    }
    
    public LabelWidget centered() {
        setCentered(true);
        return this;
    }
    
    public LabelWidget leftAligned() {
        setCentered(false);
        return this;
    }
    
    public boolean isHasShadows() {
        return hasShadows;
    }
    
    public void setHasShadows(boolean hasShadows) {
        this.hasShadows = hasShadows;
    }
    
    public LabelWidget noShadow() {
        setHasShadows(false);
        return this;
    }
    
    public int getDefaultColor() {
        return defaultColor;
    }
    
    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }
    
    public Point getPosition() {
        return pos;
    }
    
    public LabelWidget setPosition(Point position) {
        this.pos = position;
        return this;
    }
    
    public String getText() {
        return text;
    }
    
    public LabelWidget setText(String text) {
        this.text = text;
        return this;
    }
    
    public LabelWidget color(int defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }
    
    @Override
    public Rectangle getBounds() {
        int width = font.getStringWidth(text);
        Point pos = getPosition();
        if (isCentered())
            return new Rectangle(pos.x - width / 2 - 1, pos.y - 5, width + 2, 14);
        return new Rectangle(pos.x - 1, pos.y - 5, width + 2, 14);
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int width = font.getStringWidth(text);
        Point pos = getPosition();
        if (isCentered()) {
            if (hasShadows)
                font.drawWithShadow(text, pos.x - width / 2, pos.y, defaultColor);
            else
                font.draw(text, pos.x - width / 2, pos.y, defaultColor);
        } else {
            if (hasShadows)
                font.drawWithShadow(text, pos.x, pos.y, defaultColor);
            else
                font.draw(text, pos.x, pos.y, defaultColor);
        }
    }
    
}
