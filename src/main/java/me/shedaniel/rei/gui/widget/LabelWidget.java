package me.shedaniel.rei.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class LabelWidget extends HighlightableWidget {
    
    public int x;
    public int y;
    public String text;
    protected TextRenderer textRenderer;
    
    public LabelWidget(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.textRenderer = minecraft.textRenderer;
    }
    
    @Override
    public Rectangle getBounds() {
        int width = textRenderer.getStringWidth(text);
        return new Rectangle(x - width / 2 - 1, y - 5, width + 2, 14);
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawCenteredString(textRenderer, text, x, y, -1);
    }
    
}
