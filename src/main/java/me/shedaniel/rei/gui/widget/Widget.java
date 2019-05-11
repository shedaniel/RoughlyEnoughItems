/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;

public abstract class Widget extends AbstractParentElement implements Drawable {
    
    protected final MinecraftClient minecraft = MinecraftClient.getInstance();
    protected final TextRenderer font = minecraft.textRenderer;
    
}
