/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.gui.dragging;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackProvider;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.Animator;
import me.shedaniel.rei.impl.client.gui.widget.LateRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class CurrentDraggingStack extends Widget implements LateRenderable, DraggingContext<Screen> {
    private DraggableStackProvider<Screen> provider;
    private DraggableStackVisitor<Screen> visitor;
    @Nullable
    private DraggableEntry entry;
    private final List<RenderBackEntry> backToOriginals = new ArrayList<>();
    
    public void set(DraggableStackProvider<Screen> provider, DraggableStackVisitor<Screen> visitor) {
        this.provider = provider;
        this.visitor = visitor;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (entry != null) {
            if (!entry.dragging) {
                Point startPoint = entry.start;
                double xDistance = Math.abs(startPoint.x - mouseX);
                double yDistance = Math.abs(startPoint.y - mouseY);
                double requiredDistance = 8;
                
                if (xDistance * xDistance + yDistance * yDistance > requiredDistance * requiredDistance) {
                    entry.dragging = true;
                    entry.stack.drag();
                }
            }
            if (!RoughlyEnoughItemsCoreClient.isLeftMousePressed) {
                drop();
            } else if (entry.dragging) {
                matrices.pushPose();
                matrices.translate(0, 0, 600);
                entry.stack.render(matrices, new Rectangle(mouseX - 8, mouseY - 8, 16, 16), mouseX, mouseY, delta);
                matrices.popPose();
            }
        }
        
        Iterator<RenderBackEntry> iterator = backToOriginals.iterator();
        while (iterator.hasNext()) {
            RenderBackEntry renderBackEntry = iterator.next();
            renderBackEntry.update(delta);
            if (Math.abs(renderBackEntry.x.doubleValue() - renderBackEntry.x.target()) <= 2 && Math.abs(renderBackEntry.y.doubleValue() - renderBackEntry.y.target()) <= 2) {
                iterator.remove();
            } else {
                matrices.pushPose();
                matrices.translate(0, 0, 600);
                renderBackEntry.stack.render(matrices, new Rectangle(renderBackEntry.x.intValue(), renderBackEntry.y.intValue(), 16, 16), mouseX, mouseY, delta);
                matrices.popPose();
            }
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
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
        return entry != null && entry.dragging;
    }
    
    private boolean drop() {
        if (entry != null && entry.dragging) {
            Optional<DraggableStackVisitor.Acceptor> acceptor = visitor.visitDraggedStack(this, entry.stack);
            entry.stack.release(acceptor.isPresent());
            acceptor.ifPresent(a -> a.accept(entry.stack));
            entry = null;
            return true;
        }
        
        entry = null;
        return false;
    }
    
    @Override
    public Screen getScreen() {
        return Minecraft.getInstance().screen;
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
    public void renderBackToPosition(DraggableStack stack, Point initialPosition, Supplier<Point> position) {
        backToOriginals.add(new RenderBackEntry(stack, initialPosition, position));
    }
    
    private static class DraggableEntry {
        private final DraggableStack stack;
        private final Point start;
        private boolean dragging = false;
        
        private DraggableEntry(DraggableStack stack, Point start) {
            this.stack = stack;
            this.start = start;
        }
    }
    
    private static class RenderBackEntry {
        private final DraggableStack stack;
        private final Supplier<Point> position;
        private Animator x = new Animator();
        private Animator y = new Animator();
        private int lastDestination = -1;
        
        public RenderBackEntry(DraggableStack stack, Point initialPosition, Supplier<Point> position) {
            this.stack = stack;
            this.x.setAs(initialPosition.x - 8);
            this.y.setAs(initialPosition.y - 8);
            this.position = position;
        }
        
        public Point getPosition() {
            return position.get();
        }
        
        public void update(double delta) {
            this.x.update(delta);
            this.y.update(delta);
            Point position = getPosition();
            if (lastDestination != position.hashCode()) {
                lastDestination = position.hashCode();
                this.x.setTo(position.x, 200);
                this.y.setTo(position.y, 200);
            }
        }
    }
}
