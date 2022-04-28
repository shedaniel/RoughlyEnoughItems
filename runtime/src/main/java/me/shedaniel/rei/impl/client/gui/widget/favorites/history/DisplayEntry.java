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

package me.shedaniel.rei.impl.client.gui.widget.favorites.history;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Dimension;
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collections;
import java.util.List;

public class DisplayEntry extends WidgetWithBounds {
    private final LazyResettable<List<Widget>> widgets = new LazyResettable<>(this::setupWidgets);
    private final DisplayHistoryWidget parent;
    private final Display display;
    private final Dimension size = new Dimension(1, 1);
    private final ValueAnimator<FloatingRectangle> bounds = ValueAnimator.ofFloatingRectangle();
    private double xOffset = 0;
    private boolean reachedStable = false;
    
    public DisplayEntry(DisplayHistoryWidget parent, Display display, Rectangle initialBounds) {
        this.display = display;
        this.parent = parent;
        this.bounds.setAs(initialBounds.getFloatingBounds());
    }
    
    public void markBoundsDirty() {
        widgets.reset();
    }
    
    private List<Widget> setupWidgets() {
        Rectangle parentBounds = parent.getBounds();
        CategoryRegistry.CategoryConfiguration<Display> configuration = CategoryRegistry.getInstance().get((CategoryIdentifier<Display>) display.getCategoryIdentifier());
        DisplayCategory<Display> category = configuration.getCategory();
        Rectangle displayBounds = new Rectangle(0, 0, category.getDisplayWidth(display), category.getDisplayHeight());
        List<Widget> widgets = configuration.getView(display).setupDisplay(display, displayBounds);
        float scale = 1.0F;
        if (parentBounds.width * scale < displayBounds.width) {
            scale = Math.min(scale, parentBounds.width * scale / (float) displayBounds.width);
        }
        if (parentBounds.height * scale < displayBounds.height) {
            scale = Math.min(scale, parentBounds.height * scale / (float) displayBounds.height);
        }
        float x = parentBounds.getCenterX() - displayBounds.width / 2 * scale;
        float y = parentBounds.getCenterY() - displayBounds.height / 2 * scale;
        FloatingRectangle newBounds = new Rectangle(x, y, displayBounds.width * scale, displayBounds.height * scale).getFloatingBounds();
        if (this.size.width == 1 && this.size.height == 1) {
            this.bounds.setTo(newBounds, 700);
        } else {
            this.bounds.setAs(newBounds);
        }
        this.size.setSize(displayBounds.getSize());
        return widgets;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds.value().getBounds();
    }
    
    public Dimension getSize() {
        return size;
    }
    
    public boolean isStable() {
        widgets.get();
        FloatingRectangle target = this.bounds.target();
        FloatingRectangle value = this.bounds.value();
        return reachedStable || Math.abs(value.x - target.x) <= 0.5 && Math.abs(value.y - target.y) <= 0.5 && Math.abs(value.width - target.width) <= 0.5 && Math.abs(value.height - target.height) <= 0.5;
    }
    
    public void setReachedStable(boolean reachedStable) {
        this.reachedStable = reachedStable;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        boolean stable = isStable();
        this.bounds.update(delta);
        FloatingRectangle target = this.bounds.target();
        FloatingRectangle bounds = this.bounds.value();
        
        if (!reachedStable && Math.abs(bounds.x - target.x) <= 0.5 && Math.abs(bounds.y - target.y) <= 0.5 && Math.abs(bounds.width - target.width) <= 0.5 && Math.abs(bounds.height - target.height) <= 0.5) {
            reachedStable = true;
        }
        
        if (stable && (bounds.getMaxX() + xOffset < parent.getBounds().x || bounds.x + xOffset > parent.getBounds().getMaxX())) {
            return;
        }
        
        poses.pushPose();
        poses.translate(bounds.x, bounds.y, 0);
        if (stable && target.equals(bounds)) {
            poses.translate(xOffset, 0, 0);
        } else {
            poses.translate(0, 0, 600);
        }
        poses.scale((float) bounds.width / size.width, (float) bounds.height / size.height, 1.0F);
        for (Widget widget : widgets.get()) {
            widget.render(poses, -1000, -1000, delta);
        }
        poses.popPose();
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    public void setScrolled(double xOffset) {
        this.xOffset = xOffset;
    }
    
    public List<Widget> getWidgets() {
        return widgets.get();
    }
    
    public Display getDisplay() {
        return display;
    }
}
