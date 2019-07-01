/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

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
    
}
