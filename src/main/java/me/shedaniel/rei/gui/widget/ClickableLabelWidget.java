package me.shedaniel.rei.gui.widget;

import java.awt.*;

public abstract class ClickableLabelWidget extends LabelWidget {
    
    public static final int hoveredColor = (new Color(102, 255, 204)).getRGB();
    public boolean focused;
    
    public ClickableLabelWidget(int x, int y, String text) {
        super(x, y, text);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int colour = -1;
        if (isHovered(mouseX, mouseY))
            colour = hoveredColor;
        drawCenteredString(textRenderer, (isHovered(mouseX, mouseY) ? "§n" : "") + text, x, y, colour);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHighlighted(mouseX, mouseY)) {
            onLabelClicked();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 != 257 && int_1 != 32 && int_1 != 335) {
            return false;
        } else {
            this.onLabelClicked();
            return true;
        }
    }
    
    @Override
    public boolean isPartOfFocusCycle() {
        return true;
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return isHighlighted(mouseX, mouseY) || focused;
    }
    
    @Override
    public void onFocusChanged(boolean boolean_1, boolean boolean_2) {
        if (boolean_2)
            focused = boolean_1;
    }
    
    public abstract void onLabelClicked();
    
}
