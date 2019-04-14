package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.client.ScreenHelper;

import java.awt.*;
import java.util.Optional;

public abstract class ClickableLabelWidget extends LabelWidget {
    
    public static final int hoveredColor = (new Color(102, 255, 204)).getRGB();
    public boolean focused;
    public boolean clickable;
    
    public ClickableLabelWidget(int x, int y, String text, boolean clickable) {
        super(x, y, text);
        this.clickable = clickable;
    }
    
    public ClickableLabelWidget(int x, int y, String text) {
        this(x, y, text, true);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int colour = -1;
        if (clickable && isHovered(mouseX, mouseY))
            colour = hoveredColor;
        drawCenteredString(font, (isHovered(mouseX, mouseY) ? "§n" : "") + text, x, y, colour);
        if (clickable && getTooltips().isPresent())
            if (!focused && isHighlighted(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
            else if (focused)
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x, y), getTooltips().get().split("\n")));
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickable && isHighlighted(mouseX, mouseY)) {
            onLabelClicked();
            return true;
        }
        return false;
    }
    
    public Optional<String> getTooltips() {
        return Optional.empty();
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!clickable || !focused)
            return false;
        if (int_1 != 257 && int_1 != 32 && int_1 != 335)
            return false;
        this.onLabelClicked();
        return true;
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!clickable)
            return false;
        this.focused = !this.focused;
        return true;
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return clickable && (isHighlighted(mouseX, mouseY) || focused);
    }
    
    public abstract void onLabelClicked();
    
}
