/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Point;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;

/**
 * The base class for a screen widget
 *
 * @see WidgetWithBounds for a widget with bounds
 */
public abstract class Widget extends AbstractParentElement implements Drawable {
    
    /**
     * The Minecraft Client instance
     */
    protected final MinecraftClient minecraft = MinecraftClient.getInstance();
    /**
     * The font for rendering text
     */
    protected final TextRenderer font = minecraft.textRenderer;
    
    public int getZ() {
        return this.getBlitOffset();
    }
    
    public void setZ(int z) {
        this.setBlitOffset(z);
    }
    
    public boolean containsMouse(double mouseX, double mouseY) {
        return false;
    }
    
    public final boolean containsMouse(int mouseX, int mouseY) {
        return containsMouse((double) mouseX, (double) mouseY);
    }
    
    public final boolean containsMouse(Point point) {
        return containsMouse(point.x, point.y);
    }
    
    @Override
    public final boolean isMouseOver(double double_1, double double_2) {
        return containsMouse(double_1, double_2);
    }
    
}
