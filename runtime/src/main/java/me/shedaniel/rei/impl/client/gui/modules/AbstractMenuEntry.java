/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.gui.modules;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class AbstractMenuEntry extends FavoriteMenuEntry {
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    
    @Override
    public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
        this.x = xPos;
        this.y = yPos;
        this.selected = selected;
        this.containsMouse = containsMouse;
        this.rendering = rendering;
        this.width = width;
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (containsMouse(event)) {
            if (onClick(event, doubled)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }
    
    protected boolean onClick(MouseButtonEvent event, boolean doubled) {
        return false;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public boolean containsMouse() {
        return containsMouse;
    }
    
    public boolean isRendering() {
        return rendering;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return isRendering() && new Rectangle(getX(), getY(), getWidth(), getEntryHeight()).contains(mouseX, mouseY);
    }
}
