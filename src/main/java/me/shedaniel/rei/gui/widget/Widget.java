/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiEventHandler;

/**
 * The base class for a screen widget
 *
 * @see WidgetWithBounds for a widget with bounds
 */
public abstract class Widget extends GuiEventHandler {
    
    /**
     * The Minecraft Client instance
     */
    protected final Minecraft minecraft = Minecraft.getInstance();
    /**
     * The font for rendering text
     */
    protected final FontRenderer font = minecraft.fontRenderer;
    
    public abstract void render(int mouseX, int mouseY, float delta);
    
}
