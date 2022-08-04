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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import com.mojang.math.Matrix4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class WidgetsProvider implements me.shedaniel.rei.impl.client.provider.WidgetsProvider {
    @Override
    public boolean isRenderingPanel(Panel panel) {
        return PanelWidget.isRendering(panel);
    }
    
    @Override
    public Widget wrapVanillaWidget(GuiEventListener element) {
        if (element instanceof Widget) return (Widget) element;
        return new VanillaWrappedWidget(element);
    }
    
    @Override
    public WidgetWithBounds wrapRenderer(Supplier<Rectangle> bounds, Renderer renderer) {
        return new RendererWrappedWidget(renderer, bounds);
    }
    
    @Override
    public WidgetWithBounds withTranslate(WidgetWithBounds widget, Supplier<Matrix4f> translate) {
        return new DelegateWidgetWithTranslate(widget, translate);
    }
    
    @Override
    public Widget createDrawableWidget(DrawableConsumer drawable) {
        return new DrawableWidget(drawable);
    }
    
    @Override
    public Slot createSlot(Point point) {
        return new EntryWidget(point);
    }
    
    @Override
    public Slot createSlot(Rectangle bounds) {
        return new EntryWidget(bounds);
    }
    
    @Override
    public Button createButton(Rectangle bounds, Component text) {
        return new ButtonWidget(bounds, text);
    }
    
    @Override
    public Panel createPanelWidget(Rectangle bounds) {
        return new PanelWidget(bounds);
    }
    
    @Override
    public Label createLabel(Point point, FormattedText text) {
        return new LabelWidget(point, text);
    }
    
    @Override
    public Arrow createArrow(Rectangle rectangle) {
        return new ArrowWidget(rectangle);
    }
    
    @Override
    public BurningFire createBurningFire(Rectangle rectangle) {
        return new BurningFireWidget(rectangle);
    }
    
    @Override
    public DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return new TexturedDrawableConsumer(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
    }
    
    @Override
    public DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color) {
        return new FillRectangleDrawableConsumer(rectangle, color);
    }
    
    @Override
    public Widget createShapelessIcon(Point point) {
        int magnification;
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        if (scale >= 1 && scale <= 4 && scale == Math.floor(scale)) {
            magnification = (int) scale;
        } else if (scale > 4 && scale == Math.floor(scale)) {
            magnification = 1;
            for (int i = 4; i >= 1; i--) {
                if (scale % i == 0) {
                    magnification = i;
                    break;
                }
            }
        } else {
            magnification = 4;
        }
        Rectangle bounds = new Rectangle(point.getX() - 9, point.getY() + 1, 8, 8);
        Widget widget = Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/shapeless_icon_" + magnification + "x.png"), bounds.getX(), bounds.getY(), 0, 0, bounds.getWidth(), bounds.getHeight(), 1, 1, 1, 1);
        return Widgets.withTooltip(Widgets.withBounds(widget, bounds),
                new TranslatableComponent("text.rei.shapeless"));
    }
    
    @Override
    public Widget concatWidgets(List<Widget> widgets) {
        return new MergedWidget(widgets);
    }
    
    @Override
    public WidgetWithBounds noOp() {
        return NoOpWidget.INSTANCE;
    }
    
    @Override
    public WidgetWithBounds wrapOverflow(Rectangle bounds, WidgetWithBounds widget) {
        return new OverflowWidget(bounds, new PaddedCenterWidget(bounds, widget));
    }
    
    @Override
    public WidgetWithBounds wrapPadded(int padLeft, int padRight, int padTop, int padBottom, WidgetWithBounds widget) {
        return new PaddedWidget(padLeft, padRight, padTop, padBottom, widget);
    }
    
    @Override
    public TextField createTextField(Rectangle bounds) {
        return new TextFieldWidget(bounds);
    }
    
    @Override
    public BatchedSlots createBatchedSlots() {
        return new BatchedEntryRendererManager();
    }
}
