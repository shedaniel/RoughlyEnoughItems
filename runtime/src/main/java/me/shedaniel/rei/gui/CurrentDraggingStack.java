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

package me.shedaniel.rei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.gui.drag.DraggableStack;
import me.shedaniel.rei.api.gui.drag.DraggableStackProvider;
import me.shedaniel.rei.api.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.gui.drag.DraggingContext;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.gui.widget.LateRenderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CurrentDraggingStack extends Widget implements LateRenderable, DraggingContext {
    private DraggableStackProvider provider;
    private DraggableStackVisitor visitor;
    @Nullable
    private DraggableEntry entry;
    
    public void set(DraggableStackProvider provider, DraggableStackVisitor visitor) {
        this.provider = provider;
        this.visitor = visitor;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (entry != null && entry.dragging) {
            if (!RoughlyEnoughItemsCore.isLeftModePressed) {
                drop();
                return;
            }
            matrices.pushPose();
            matrices.translate(0, 0, 600);
            entry.stack.render(matrices, new Rectangle(mouseX - 8, mouseY - 8, 16, 16), mouseX, mouseY, delta);
            matrices.popPose();
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        drop();
        DraggableStack hoveredStack = provider.getHoveredStack(this, mouseX, mouseY);
        if (hoveredStack != null) {
            entry = new DraggableEntry(hoveredStack, new Point(mouseX, mouseY));
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
        if (entry != null && !entry.dragging) {
            Point startPoint = entry.start;
            double xDistance = Math.abs(startPoint.x - mouseX1);
            double yDistance = Math.abs(startPoint.y - mouseY1);
            double requiredDistance = 4;
            
            if (xDistance * xDistance + yDistance * yDistance > requiredDistance * requiredDistance) {
                entry.dragging = true;
                entry.stack.drag();
            }
        }
        
        return entry != null;
    }
    
    private boolean drop() {
        if (entry != null && entry.dragging) {
            Optional<DraggableStackVisitor.Acceptor> acceptor = visitor.visitDraggedStack(entry.stack);
            entry.stack.release(acceptor.isPresent());
            acceptor.ifPresent(a -> a.accept(entry.stack));
            entry = null;
            return true;
        }
        
        entry = null;
        return false;
    }
    
    @Override
    @Nullable
    public DraggableStack getCurrentStack() {
        return entry != null && entry.dragging ? entry.stack : null;
    }
    
    @Override
    @Nullable
    public Point getCurrentPosition() {
        return isDraggingStack() ? PointHelper.ofMouse() : null;
    }
    
    @Override
    public void renderBackToPosition(DraggableStack stack, Supplier<Point> position) {
        
    }
    
    private class DraggableEntry {
        private final DraggableStack stack;
        private final Point start;
        private boolean dragging = false;
        
        private DraggableEntry(DraggableStack stack, Point start) {
            this.stack = stack;
            this.start = start;
        }
    }
}
