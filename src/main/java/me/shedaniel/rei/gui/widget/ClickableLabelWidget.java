/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.shedaniel.rei.client.ScreenHelper;

import java.util.Optional;

public abstract class ClickableLabelWidget extends LabelWidget {
    
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
        int colour = getDefaultColor();
        if (clickable && isHovered(mouseX, mouseY))
            colour = getHoveredColor();
        drawCenteredString(font, (isHovered(mouseX, mouseY) ? ChatFormatting.UNDERLINE.toString() : "") + text, x, y, colour);
        if (clickable && getTooltips().isPresent())
            if (containsMouse(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
    }
    
    public int getDefaultColor() {
        return ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : -1;
    }
    
    public int getHoveredColor() {
        return ScreenHelper.isDarkModeEnabled() ? -1 : 0xFF66FFCC;
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
    
    public boolean isHovered(int mouseX, int mouseY) {
        return clickable && containsMouse(mouseX, mouseY);
    }
    
    public abstract void onLabelClicked();
    
}
