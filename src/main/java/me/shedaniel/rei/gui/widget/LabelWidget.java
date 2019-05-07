package me.shedaniel.rei.gui.widget;

import net.minecraft.client.gui.Element;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class LabelWidget extends HighlightableWidget {
    
    public int x;
    public int y;
    public String text;
    
    public LabelWidget(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
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
        drawCenteredString(font, text, x, y, -1);
    }
    
}
