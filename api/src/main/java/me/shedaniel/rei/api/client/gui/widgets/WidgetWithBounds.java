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

package me.shedaniel.rei.api.client.gui.widgets;

import me.shedaniel.math.Rectangle;
import org.jetbrains.annotations.ApiStatus;
import net.minecraft.client.gui.GuiGraphics;

public abstract class WidgetWithBounds extends Widget {
    public abstract Rectangle getBounds();
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Deprecated
    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Rectangle clone = getBounds().clone();
        getBounds().setBounds(bounds);
        render(graphics, mouseX, mouseY, delta);
        getBounds().setBounds(clone);
    }
    
    @ApiStatus.Experimental
    public final WidgetWithBounds withPadding(int padding) {
        return Widgets.padded(padding, this);
    }
    
    @ApiStatus.Experimental
    public final WidgetWithBounds withPadding(int padX, int padY) {
        return Widgets.padded(padX, padY, this);
    }
    
    @ApiStatus.Experimental
    public final WidgetWithBounds withPaddingHorizontal(int padX) {
        return Widgets.padded(padX, 0, this);
    }
    
    @ApiStatus.Experimental
    public final WidgetWithBounds withPaddingVertical(int padY) {
        return Widgets.padded(0, padY, this);
    }
    
    @ApiStatus.Experimental
    public final WidgetWithBounds withPadding(int padLeft, int padRight, int padTop, int padBottom) {
        return Widgets.padded(padLeft, padRight, padTop, padBottom, this);
    }
    
    @ApiStatus.Experimental
    public final WidgetWithBounds withScissors() {
        return Widgets.scissored(this);
    }
}
