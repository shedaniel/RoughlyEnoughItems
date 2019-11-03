/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Point;
import me.shedaniel.rei.impl.ScreenHelper;

import java.util.Optional;

public abstract class ClickableLabelWidget extends LabelWidget {
    
    public boolean focused;
    public boolean clickable;
    public int hoveredColor;
    
    public ClickableLabelWidget(int x, int y, String text, boolean clickable) {
        super(x, y, text);
        this.clickable = clickable;
        this.hoveredColor = ScreenHelper.isDarkModeEnabled() ? -1 : 0xFF66FFCC;
    }
    
    public ClickableLabelWidget(int x, int y, String text) {
        this(x, y, text, true);
    }
    
    public LabelWidget hoveredColor(int hoveredColor) {
        this.hoveredColor = hoveredColor;
        return this;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int color = getDefaultColor();
        if (clickable && isHovered(mouseX, mouseY))
            color = getHoveredColor();
        int width = font.getStringWidth(text);
        if (isHasShadows())
            font.drawWithShadow(text, x - width / 2, y, color);
        else
            font.draw(text, x - width / 2, y, color);
        if (clickable && getTooltips().isPresent())
            if (!focused && containsMouse(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
            else if (focused)
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x, y), getTooltips().get().split("\n")));
    }
    
    public int getHoveredColor() {
        return hoveredColor;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickable && containsMouse(mouseX, mouseY)) {
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
        return clickable && (containsMouse(mouseX, mouseY) || focused);
    }
    
    public abstract void onLabelClicked();
    
}
