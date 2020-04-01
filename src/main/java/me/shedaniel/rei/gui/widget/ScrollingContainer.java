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

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import static me.shedaniel.rei.gui.widget.EntryListWidget.entrySize;

@ApiStatus.Internal
public abstract class ScrollingContainer {
    public double scrollAmount;
    public double scrollTarget;
    public long start;
    public long duration;
    public boolean draggingScrollBar = false;
    
    public abstract Rectangle getBounds();
    
    public Rectangle getScissorBounds() {
        Rectangle bounds = getBounds();
        if (hasScrollBar()) {
            return new Rectangle(bounds.x, bounds.y, bounds.width - 6, bounds.height);
        }
        return bounds;
    }
    
    public int getScrollBarX() {
        return hasScrollBar() ? getBounds().getMaxX() - 6 : getBounds().getMaxX();
    }
    
    public boolean hasScrollBar() {
        return getMaxScrollHeight() > getBounds().height;
    }
    
    public abstract int getMaxScrollHeight();
    
    public final int getMaxScroll() {
        return Math.max(0, getMaxScrollHeight() - getBounds().height);
    }
    
    public final double clamp(double v) {
        return this.clamp(v, 200.0D);
    }
    
    public final double clamp(double v, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, (double) this.getMaxScroll() + clampExtension);
    }
    
    public final void offset(double value, boolean animated) {
        scrollTo(scrollTarget + value, animated);
    }
    
    public final void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    public final void scrollTo(double value, boolean animated, long duration) {
        scrollTarget = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scrollAmount = scrollTarget;
    }
    
    public void updatePosition(float delta) {
        double[] target = new double[]{this.scrollTarget};
        this.scrollAmount = ClothConfigInitializer.handleScrollingPosition(target, this.scrollAmount, this.getMaxScroll(), delta, this.start, this.duration);
        this.scrollTarget = target[0];
    }
    
    public void renderScrollBar() {
        renderScrollBar(0, 1);
    }
    
    public void renderScrollBar(int background, float alpha) {
        if (hasScrollBar()) {
            Rectangle bounds = getBounds();
            int maxScroll = getMaxScroll();
            int height = bounds.height * bounds.height / getMaxScrollHeight();
            height = MathHelper.clamp(height, 32, bounds.height);
            height -= Math.min((scrollAmount < 0 ? (int) -scrollAmount : scrollAmount > maxScroll ? (int) scrollAmount - maxScroll : 0), height * .95);
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) scrollAmount * (bounds.height - height) / maxScroll + bounds.y, bounds.y), bounds.getMaxY() - height);
            
            int scrollbarPositionMinX = getScrollBarX();
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            boolean hovered = (new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height)).contains(PointHelper.ofMouse());
            float bottomC = (hovered ? .67f : .5f) * (REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            float topC = (hovered ? .87f : .67f) * (REIHelper.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            {
                float a = (background >> 24 & 255) / 255.0F;
                float r = (background >> 16 & 255) / 255.0F;
                float g = (background >> 8 & 255) / 255.0F;
                float b = (background & 255) / 255.0F;
                buffer.begin(7, VertexFormats.POSITION_COLOR);
                buffer.vertex(scrollbarPositionMinX, bounds.getMaxY(), 0.0D).color(r, g, b, a).next();
                buffer.vertex(scrollbarPositionMaxX, bounds.getMaxY(), 0.0D).color(r, g, b, a).next();
                buffer.vertex(scrollbarPositionMaxX, bounds.y, 0.0D).color(r, g, b, a).next();
                buffer.vertex(scrollbarPositionMinX, bounds.y, 0.0D).color(r, g, b, a).next();
            }
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            buffer.vertex(scrollbarPositionMinX, minY + height, 0.0D).color(bottomC, bottomC, bottomC, alpha).next();
            buffer.vertex(scrollbarPositionMaxX, minY + height, 0.0D).color(bottomC, bottomC, bottomC, alpha).next();
            buffer.vertex(scrollbarPositionMaxX, minY, 0.0D).color(bottomC, bottomC, bottomC, alpha).next();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).color(bottomC, bottomC, bottomC, alpha).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            buffer.vertex(scrollbarPositionMinX, (minY + height - 1), 0.0D).color(topC, topC, topC, alpha).next();
            buffer.vertex((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).color(topC, topC, topC, alpha).next();
            buffer.vertex((scrollbarPositionMaxX - 1), minY, 0.0D).color(topC, topC, topC, alpha).next();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).color(topC, topC, topC, alpha).next();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return mouseDragged(mouseX, mouseY, button, dx, dy, false);
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy, boolean careSnapping) {
        if (button == 0 && draggingScrollBar) {
            float height = getMaxScrollHeight();
            Rectangle bounds = getBounds();
            int actualHeight = bounds.height;
            if (mouseY >= bounds.y && mouseY <= bounds.getMaxY()) {
                double maxScroll = Math.max(1, getMaxScroll());
                double int_3 = MathHelper.clamp(((double) (actualHeight * actualHeight) / (double) height), 32, actualHeight - 8);
                double double_6 = Math.max(1.0D, maxScroll / (actualHeight - int_3));
                float to = MathHelper.clamp((float) (scrollAmount + dy * double_6), 0, getMaxScroll());
                if (careSnapping && ConfigObject.getInstance().doesSnapToRows()) {
                    double nearestRow = Math.round(to / (double) entrySize()) * (double) entrySize();
                    scrollTo(nearestRow, false);
                } else
                    scrollTo(to, false);
            }
            return true;
        }
        return false;
    }
    
    public boolean updateDraggingState(double mouseX, double mouseY, int button) {
        if (!hasScrollBar())
            return false;
        double height = getMaxScroll();
        Rectangle bounds = getBounds();
        int actualHeight = bounds.height;
        if (height > actualHeight && mouseY >= bounds.y && mouseY <= bounds.getMaxY()) {
            double scrollbarPositionMinX = getScrollBarX();
            if (mouseX >= scrollbarPositionMinX - 1 & mouseX <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                return true;
            }
        }
        this.draggingScrollBar = false;
        return false;
    }
}
