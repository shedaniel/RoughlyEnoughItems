package me.shedaniel.rei.gui.widget;

import java.awt.*;

public abstract class ClickableLabelWidget extends LabelWidget implements HighlightableWidget {
    
    public ClickableLabelWidget(int x, int y, String text) {
        super(x, y, text);
    }
    
    @Override
    public Rectangle getBounds() {
        int width = textRenderer.getStringWidth(text);
        return new Rectangle(x - width / 2 - 1, y - 5, width + 2, 14);
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        int colour = -1;
        if (isHighlighted(mouseX, mouseY))
            colour = 16777120;
        drawStringCentered(textRenderer, (isHighlighted(mouseX, mouseY) ? "§n" : "") + text, x, y, colour);
    }
    
    @Override
    public boolean onMouseClick(int button, double mouseX, double mouseY) {
        if (button == 0 && isHighlighted(mouseX, mouseY)) {
            onLabelClicked();
            return true;
        }
        return false;
    }
    
    public abstract void onLabelClicked();
    
}
