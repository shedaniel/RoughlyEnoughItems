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

package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MergedWidgetWithBounds extends WidgetWithBounds {
    private final Supplier<Rectangle> bounds;
    private final List<Widget> widgets;
    
    public MergedWidgetWithBounds(Supplier<Rectangle> bounds, List<Widget> widgets) {
        this.bounds = bounds;
        this.widgets = widgets;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        for (Widget widget : widgets) {
            widget.render(graphics, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        for (Widget widget : this.widgets) {
            if (widget.mouseScrolled(mouseX, mouseY, amountX, amountY))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        for (Widget widget : this.widgets) {
            if (widget.keyPressed(event))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean keyReleased(KeyEvent event) {
        for (Widget widget : this.widgets) {
            if (widget.keyReleased(event))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        for (Widget widget : this.widgets) {
            if (widget.charTyped(event))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        for (Widget widget : this.widgets) {
            if (widget.mouseDragged(event, deltaX, deltaY))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (Widget widget : this.widgets) {
            if (widget.mouseReleased(event))
                return true;
        }
        return false;
    }
    
    @Override
    public double getZRenderingPriority() {
        return CollectionUtils.max(widgets, Comparator.comparingDouble(Widget::getZRenderingPriority))
                .map(Widget::getZRenderingPriority).orElse(0.0);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds.get();
    }
    
    @Deprecated
    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        Rectangle clone = getBounds().clone();
        getBounds().setBounds(bounds);
        render(graphics, mouseX, mouseY, delta);
        getBounds().setBounds(clone);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        // Reverse iteration
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.containsMouse(mouseX, mouseY)) {
                return Optional.of(widget);
            }
        }
        
        return Optional.empty();
    }
}