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

package me.shedaniel.rei.impl.client.gui.dragging;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.gui.drag.DraggableBoundsProvider;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProvider;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitor;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.impl.client.gui.widget.LateRenderable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class CurrentDraggingStack extends Widget implements LateRenderable, DraggingContext<Screen> {
    private DraggableComponentProvider<Screen, ?> provider;
    private DraggableComponentVisitor<Screen> visitor;
    @Nullable
    private DraggableEntry entry;
    private final List<RenderBackEntry> backToOriginals = new ArrayList<>();
    private final Set<ShapeBounds> bounds = new HashSet<>();
    
    public void set(DraggableComponentProvider<Screen, ?> provider, DraggableComponentVisitor<Screen> visitor) {
        this.provider = provider;
        this.visitor = visitor;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Integer hash = null;
        
        if (entry != null) {
            if (!entry.dragging) {
                Point startPoint = entry.start;
                double xDistance = Math.abs(startPoint.x - mouseX);
                double yDistance = Math.abs(startPoint.y - mouseY);
                double requiredDistance = 8;
                
                if (xDistance * xDistance + yDistance * yDistance > requiredDistance * requiredDistance) {
                    entry.dragging = true;
                    entry.startDragging = Util.getMillis();
                    entry.component.drag();
                }
            }
            
            if (entry.dragging) {
                matrices.pushPose();
                matrices.translate(0, 0, 600);
                entry.bounds.update(delta);
                int width = entry.component.getWidth();
                int height = entry.component.getHeight();
                Vec2 mouseStartOffset = entry.mouseStartOffset;
                entry.bounds.setTo(new FloatingRectangle(mouseX - width / 2 - mouseStartOffset.x, mouseY - height / 2 - mouseStartOffset.y, width, height),
                        30);
                entry.component.render(matrices, entry.bounds.value().getBounds(), mouseX, mouseY, delta);
                matrices.popPose();
                
                VoxelShape shape = entry.getBoundsProvider().bounds();
                ShapeBounds shapeBounds = new ShapeBounds(shape);
                shapeBounds.alpha.setTo(60, 300);
                bounds.add(shapeBounds);
                hash = shapeBounds.hash;
            }
            
            if (!RoughlyEnoughItemsCoreClient.isLeftMousePressed) {
                drop();
            }
        }
        
        for (ShapeBounds bound : bounds) {
            if ((hash == null || hash != bound.hash) && bound.alpha.target() != 0) {
                bound.alpha.setTo(0, 300);
            }
        }
        
        {
            Iterator<ShapeBounds> iterator = bounds.iterator();
            while (iterator.hasNext()) {
                ShapeBounds bounds = iterator.next();
                bounds.update(delta);
                if (bounds.alpha.target() == 0 && bounds.alpha.value() <= 2) {
                    iterator.remove();
                } else {
                    bounds.shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
                        matrices.pushPose();
                        matrices.translate(0, 0, 500);
                        fillGradient(matrices, (int) x1, (int) y1, (int) x2, (int) y2, 0xfdff6b | (bounds.alpha.intValue() << 24), 0xfdff6b | (bounds.alpha.intValue() << 24));
                        matrices.popPose();
                    });
                }
            }
        }
        
        Iterator<RenderBackEntry> iterator = backToOriginals.iterator();
        while (iterator.hasNext()) {
            RenderBackEntry renderBackEntry = iterator.next();
            renderBackEntry.update(delta);
            FloatingRectangle value = renderBackEntry.bounds.value();
            FloatingRectangle target = renderBackEntry.bounds.target();
            if (value.width < 2 || value.height < 2 || (Math.abs(value.x - target.x) <= 1.3 && Math.abs(value.y - target.y) <= 1.3 && Math.abs(value.width - target.width) <= 1 && Math.abs(value.height - target.height) <= 1)) {
                iterator.remove();
            } else {
                matrices.pushPose();
                matrices.translate(0, 0, 600);
                renderBackEntry.component.render(matrices, value.getBounds(), mouseX, mouseY, delta);
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
        if (button != 0) {
            return false;
        }
        drop();
        DraggableComponent<?> hoveredStack = provider.getHovered(this, mouseX, mouseY);
        if (hoveredStack != null) {
            entry = new DraggableEntry(hoveredStack, new Point(mouseX, mouseY));
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (i != 0) {
            return false;
        }
        drop();
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
        return button == 0 && entry != null && entry.dragging;
    }
    
    private boolean drop() {
        if (entry != null && entry.dragging) {
            DraggedAcceptorResult result = visitor.acceptDragged(this, entry.component);
            entry.component.release(result);
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
        DraggableComponent<?> dragged = getDragged();
        return dragged instanceof DraggableStack ? (DraggableStack) dragged : null;
    }
    
    @Override
    @Nullable
    public DraggableComponent<?> getDragged() {
        return entry != null && entry.dragging ? entry.component : null;
    }
    
    @Override
    @Nullable
    public Point getCurrentPosition() {
        if (!isDraggingComponent()) return null;
        Vec2 mouseStartOffset = entry.mouseStartOffset;
        FloatingRectangle rectangle = entry.bounds.value();
        return new Point(rectangle.getCenterX() + mouseStartOffset.x, rectangle.getCenterY() + mouseStartOffset.y);
    }
    
    @Override
    @Nullable
    public Rectangle getCurrentBounds() {
        if (!isDraggingComponent()) return null;
        FloatingRectangle rectangle = entry.bounds.value();
        return rectangle.getBounds();
    }
    
    @Override
    public void renderBack(DraggableComponent<?> component, Point initialPosition, Supplier<Point> position) {
        int width = component.getWidth();
        int height = component.getHeight();
        backToOriginals.add(new RenderBackEntry(component, new Rectangle(initialPosition.x - width / 2, initialPosition.y - height / 2, width, height), () -> {
            Point point = position.get();
            return new Rectangle(point.x, point.y, width, height);
        }));
    }
    
    @Override
    public void renderBack(DraggableComponent<?> component, Rectangle initialPosition, Supplier<Rectangle> bounds) {
        backToOriginals.add(new RenderBackEntry(component, initialPosition, bounds));
    }
    
    private class DraggableEntry {
        private final DraggableComponent<?> component;
        private final Point start;
        private long startDragging = -1;
        private final ValueAnimator<FloatingRectangle> bounds;
        private final Vec2 mouseStartOffset;
        private boolean dragging = false;
        private DraggableBoundsProvider boundsProvider;
        
        private DraggableEntry(DraggableComponent<?> component, Point start) {
            this.component = component;
            this.start = start;
            this.bounds = ValueAnimator.ofFloatingRectangle()
                    .setAs(component.getOriginBounds(start).getFloatingBounds());
            this.mouseStartOffset = new Vec2((float) (start.x - bounds.value().getCenterX()), (float) (start.y - bounds.value().getCenterY()));
        }
        
        public DraggableBoundsProvider getBoundsProvider() {
            if (boundsProvider == null) {
                boundsProvider = DraggableBoundsProvider.concat(visitor.getDraggableAcceptingBounds(CurrentDraggingStack.this, component).toList());
            }
            
            return boundsProvider;
        }
    }
    
    private static class ShapeBounds {
        private VoxelShape shape;
        private NumberAnimator<Double> alpha;
        private int hash;
        
        public ShapeBounds(VoxelShape shape) {
            this.shape = shape;
            this.alpha = ValueAnimator.ofDouble(0);
            this.hash = shape.toAabbs().hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShapeBounds shapeBounds)) return false;
            return hash == shapeBounds.hash;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        public void update(double delta) {
            this.alpha.update(delta);
        }
    }
    
    private static class RenderBackEntry {
        private final DraggableComponent<?> component;
        private final Supplier<Rectangle> position;
        private ValueAnimator<FloatingRectangle> bounds = ValueAnimator.ofFloatingRectangle();
        private int lastDestination = -1;
        
        public RenderBackEntry(DraggableComponent<?> component, Rectangle initialPosition, Supplier<Rectangle> position) {
            this.component = component;
            this.bounds.setAs(new FloatingRectangle(initialPosition));
            this.position = position;
        }
        
        public Rectangle getPosition() {
            return position.get();
        }
        
        public void update(double delta) {
            this.bounds.update(delta);
            this.bounds.setTo(new FloatingRectangle(getPosition()), 200);
        }
    }
}
