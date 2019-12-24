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
    private boolean clickable = true;
    private int hoveredColor;

    @Deprecated
    public ClickableLabelWidget(int x, int y, String text, boolean clickable) {
        this(new Point(x, y), text, clickable);
    }

    @Deprecated
    public ClickableLabelWidget(int x, int y, String text) {
        this(new Point(x, y), text, true);
    }

    @Deprecated
    public ClickableLabelWidget(Point point, String text, boolean clickable) {
        this(point, text);
        clickable(clickable);
    }

    public ClickableLabelWidget(Point point, String text) {
        super(point, text);
        this.hoveredColor = ScreenHelper.isDarkModeEnabled() ? -1 : 0xFF66FFCC;
    }

    public LabelWidget hoveredColor(int hoveredColor) {
        this.hoveredColor = hoveredColor;
        return this;
    }

    public LabelWidget clickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    public boolean isClickable() {
        return clickable;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        int color = getDefaultColor();
        if (isClickable() && isHovered(mouseX, mouseY))
            color = getHoveredColor();
        Point pos = getPosition();
        int width = font.getStringWidth(getText());
        if (isCentered()) {
            if (isHasShadows())
                font.drawWithShadow(getText(), pos.x - width / 2, pos.y, color);
            else
                font.draw(getText(), pos.x - width / 2, pos.y, color);
        } else {
            if (isHasShadows())
                font.drawWithShadow(getText(), pos.x, pos.y, color);
            else
                font.draw(getText(), pos.x, pos.y, color);
        }
        if (isClickable() && getTooltips().isPresent())
            if (!focused && containsMouse(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
            else if (focused)
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(pos, getTooltips().get().split("\n")));
    }

    public int getHoveredColor() {
        return hoveredColor;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isClickable() && containsMouse(mouseX, mouseY)) {
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
        if (!isClickable() || !focused)
            return false;
        if (int_1 != 257 && int_1 != 32 && int_1 != 335)
            return false;
        this.onLabelClicked();
        return true;
    }

    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!isClickable())
            return false;
        this.focused = !this.focused;
        return true;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return isClickable() && (containsMouse(mouseX, mouseY) || focused);
    }

    public abstract void onLabelClicked();

}
