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

package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import org.joml.Matrix4f;

public class PaddedWidget extends DelegateWidgetWithTranslate {
    private final int padLeft, padRight, padTop, padBottom;
    
    public PaddedWidget(int padLeft, int padRight, int padTop, int padBottom, WidgetWithBounds widget) {
        super(widget, Matrix4f::new);
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;
    }
    
    @Override
    protected Matrix4f translate() {
        return new Matrix4f().translate(padLeft, padRight, 0);
    }
    
    @Override
    public Rectangle getBounds() {
        Rectangle widgetBounds = ((WidgetWithBounds) delegate()).getBounds();
        return new Rectangle(widgetBounds.x - padLeft, widgetBounds.y - padTop, widgetBounds.width + padLeft + padRight, widgetBounds.height + padTop + padBottom);
    }
}
