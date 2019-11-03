/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.Element;

import java.util.Collections;
import java.util.List;

public class LabelWidget extends WidgetWithBounds {
    
    public int x;
    public int y;
    public String text;
    private int defaultColor;
    private boolean hasShadows = true;
    
    public LabelWidget(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.defaultColor = ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : -1;
    }
    
    public boolean isHasShadows() {
        return hasShadows;
    }
    
    public void setHasShadows(boolean hasShadows) {
        this.hasShadows = hasShadows;
    }
    
    public int getDefaultColor() {
        return defaultColor;
    }
    
    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }
    
    public LabelWidget color(int defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }
    
    @Override
    public Rectangle getBounds() {
        int width = font.getStringWidth(text);
        return new Rectangle(x - width / 2 - 1, y - 5, width + 2, 14);
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int width = font.getStringWidth(text);
        if (hasShadows)
            font.drawWithShadow(text, x - width / 2, y, defaultColor);
        else font.draw(text, x - width / 2, y, defaultColor);
    }
    
}
