/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        return this.getZOffset();
    }
    
    public void setZ(int z) {
        this.setZOffset(z);
    }
    
    public boolean containsMouse(double mouseX, double mouseY) {
        return false;
    }
    
    @SuppressWarnings("RedundantCast")
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
