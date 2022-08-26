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

package me.shedaniel.rei.impl.client.provider;

import com.mojang.math.Matrix4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Internal
public interface WidgetsProvider {
    boolean isRenderingPanel(Panel panel);
    
    Widget wrapVanillaWidget(GuiEventListener element);
    
    WidgetWithBounds wrapRenderer(Supplier<Rectangle> bounds, Renderer renderer);
    
    WidgetWithBounds withTranslate(WidgetWithBounds widget, Supplier<Matrix4f> translate);
    
    Widget createDrawableWidget(DrawableConsumer drawable);
    
    Slot createSlot(Point point);
    
    Slot createSlot(Rectangle bounds);
    
    Button createButton(Rectangle bounds, Component text);
    
    Panel createPanelWidget(Rectangle bounds);
    
    Label createLabel(Point point, FormattedText text);
    
    Arrow createArrow(Rectangle rectangle);
    
    BurningFire createBurningFire(Rectangle rectangle);
    
    DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight);
    
    DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color);
    
    Widget createShapelessIcon(Point point);
    
    Widget concatWidgets(List<Widget> widgets);
    
    WidgetWithBounds noOp();
    
    WidgetWithBounds wrapOverflow(Rectangle bounds, WidgetWithBounds widget);
    
    WidgetWithBounds wrapPadded(int padLeft, int padRight, int padTop, int padBottom, WidgetWithBounds widget);
    
    TextField createTextField(Rectangle bounds);
    
    BatchedSlots createBatchedSlots();
}
