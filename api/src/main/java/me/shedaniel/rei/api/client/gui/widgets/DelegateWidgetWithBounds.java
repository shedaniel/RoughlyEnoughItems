/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;

import java.util.function.Supplier;

public class DelegateWidgetWithBounds extends DelegateWidget {
    private final Supplier<Rectangle> bounds;
    
    public DelegateWidgetWithBounds(Widget widget, Supplier<Rectangle> bounds) {
        super(widget);
        this.bounds = bounds;
    }
    
    public DelegateWidgetWithBounds(Supplier<Rectangle> bounds) {
        this(Widgets.noOp(), bounds);
    }
    
    public DelegateWidgetWithBounds(Rectangle bounds) {
        this(Widgets.noOp(), () -> bounds);
    }
    
    public DelegateWidgetWithBounds() {
        this(new Rectangle());
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds.get();
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Deprecated
    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Rectangle clone = getBounds().clone();
        getBounds().setBounds(bounds);
        render(matrices, mouseX, mouseY, delta);
        getBounds().setBounds(clone);
    }
}