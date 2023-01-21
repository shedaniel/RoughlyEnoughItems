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

package me.shedaniel.rei.impl.client.gui.widget.favorites.history;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitorWidget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.gui.widget.DisplayCompositeWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class DisplayHistoryWidget extends WidgetWithBounds implements DraggableComponentVisitorWidget, DraggableComponentProviderWidget<Object> {
    private final FavoritesListWidget parent;
    private final Rectangle bounds = new Rectangle();
    private final NumberAnimator<Double> height;
    private boolean ignoreNextMouse;
    
    private final NumberAnimator<Double> scroll = ValueAnimator.ofDouble();
    
    public DisplayHistoryWidget(FavoritesListWidget parent) {
        this.parent = parent;
        this.height = ValueAnimator.ofDouble().withConvention(() -> {
            boolean draggingDisplay = DraggingContext.getInstance().isDraggingComponent()
                                      && DraggingContext.getInstance().getDragged().get() instanceof Display;
            if (draggingDisplay) {
                return Math.min(parent.excludedBounds.height, 80D);
            }
            return 0D;
        }, ValueAnimator.typicalTransitionTime());
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        Rectangle fullBounds = parent.excludedBounds;
        List<DisplayEntry> entries = new ArrayList<>(DisplayHistoryManager.INSTANCE.getEntries(this));
        if (updateBounds(fullBounds)) {
            for (DisplayEntry entry : entries) {
                entry.markBoundsDirty();
            }
        }
        
        updatePosition(delta);
        double xOffset = -scroll.doubleValue() + entries.size() * getBounds().getWidth();
        for (int i = entries.size() - 1; i >= 0; i--) {
            xOffset -= getBounds().getWidth();
            DisplayEntry entry = entries.get(i);
            entry.setScrolled(xOffset);
            if (entry.isStable()) {
                ScissorsHandler.INSTANCE.scissor(getBounds());
            }
            entry.render(poses, mouseX, mouseY, delta);
            if (entry.isStable()) {
                ScissorsHandler.INSTANCE.removeLastScissor();
            }
        }
        
        boolean draggingDisplay = DraggingContext.getInstance().isDraggingComponent()
                                  && DraggingContext.getInstance().getDragged().get() instanceof Display;
        double onBoardingHeight = this.height.value();
        
        if (entries.isEmpty() && draggingDisplay && Math.round(onBoardingHeight) > 0) {
            double lastProgress = Math.pow(Mth.clamp(onBoardingHeight / Math.min(parent.excludedBounds.height, 80D), 0, 1), 7);
            int alpha = (int) (0x50 * lastProgress) + (int) (0x42 * lastProgress * (Mth.cos((float) (System.currentTimeMillis() % 1000 / 500F * Math.PI)) + 1) / 2);
            int lineColor = alpha << 24 | 0xFFFFFF;
            Rectangle bounds = this.bounds.clone();
            bounds.y += 10;
            bounds.height -= 20;
            drawHorizontalDashedLine(poses, bounds.x, bounds.getMaxX(), bounds.y, lineColor, false);
            drawHorizontalDashedLine(poses, bounds.x, bounds.getMaxX(), bounds.getMaxY() - 1, lineColor, true);
            
            drawVerticalDashedLine(poses, bounds.x, bounds.y, bounds.getMaxY(), lineColor, true);
            drawVerticalDashedLine(poses, bounds.getMaxX() - 1, bounds.y, bounds.getMaxY(), lineColor, false);
        }
    }
    
    private void drawHorizontalDashedLine(PoseStack poses, int x1, int x2, int y, int color, boolean reverse) {
        float offset = (System.currentTimeMillis() % 700) / 100.0F;
        if (!reverse) offset = 7 - offset;
        
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Matrix4f pose = poses.last().pose();
        
        for (float x = x1 - offset; x < x2; x += 7) {
            builder.vertex(pose, Mth.clamp(x + 4, x1, x2), y, getZ()).color(r, g, b, a).endVertex();
            builder.vertex(pose, Mth.clamp(x, x1, x2), y, getZ()).color(r, g, b, a).endVertex();
            builder.vertex(pose, Mth.clamp(x, x1, x2), y + 1, getZ()).color(r, g, b, a).endVertex();
            builder.vertex(pose, Mth.clamp(x + 4, x1, x2), y + 1, getZ()).color(r, g, b, a).endVertex();
        }
        
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
    
    private void drawVerticalDashedLine(PoseStack poses, int x, int y1, int y2, int color, boolean reverse) {
        float offset = (System.currentTimeMillis() % 700) / 100.0F;
        if (!reverse) offset = 7 - offset;
        
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Matrix4f pose = poses.last().pose();
        
        for (float y = y1 - offset; y < y2; y += 7) {
            builder.vertex(pose, x + 1, Mth.clamp(y, y1, y2), getZ()).color(r, g, b, a).endVertex();
            builder.vertex(pose, x, Mth.clamp(y, y1, y2), getZ()).color(r, g, b, a).endVertex();
            builder.vertex(pose, x, Mth.clamp(y + 4, y1, y2), getZ()).color(r, g, b, a).endVertex();
            builder.vertex(pose, x + 1, Mth.clamp(y + 4, y1, y2), getZ()).color(r, g, b, a).endVertex();
        }
        
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
    
    private boolean updateBounds(Rectangle fullBounds) {
        int prevHash = bounds.hashCode();
        this.bounds.setBounds(createBounds(fullBounds));
        return prevHash != bounds.hashCode();
    }
    
    public Rectangle createBounds(Rectangle fullBounds) {
        return createBounds(fullBounds, height.value());
    }
    
    public Rectangle createBounds(Rectangle fullBounds, @Nullable Double height) {
        return new Rectangle(fullBounds.x + 2, fullBounds.y, fullBounds.width - 4, Math.min(fullBounds.height, 80));
    }
    
    public void updatePosition(float delta) {
        this.height.update(delta);
        this.scroll.setTarget(ScrollingContainer.handleBounceBack(scroll.target(), this.getMaxScrollDist(), delta, .08));
        this.scroll.update(delta);
        
        if (this.scroll.target() >= 0 && this.scroll.target() <= getMaxScrollDist()) {
            if (DisplayHistoryManager.INSTANCE.getEntries(this).size() > 1) {
                int before = (int) (Math.floor(this.scroll.target() / getBounds().getWidth()) * getBounds().getWidth());
                int after = (int) (Math.ceil(this.scroll.target() / getBounds().getWidth()) * getBounds().getWidth());
                if (before <= this.scroll.target() && after >= this.scroll.target()) {
                    // check closer
                    if (Math.abs(before - this.scroll.target()) < Math.abs(after - this.scroll.target())) {
                        // move to before
                        this.scroll.setTarget(this.scroll.target() - (this.scroll.target() - before) * delta / 2.0);
                    } else {
                        this.scroll.setTarget(this.scroll.target() + (after - this.scroll.target()) * delta / 2.0);
                    }
                }
            }
        }
    }
    
    public int getContentHeight() {
        Collection<DisplayEntry> entries = DisplayHistoryManager.INSTANCE.getEntries(this);
        if (entries.isEmpty()) return 0;
        return getBounds().getWidth() * entries.size();
    }
    
    public final int getMaxScrollDist() {
        return Math.max(0, this.getContentHeight() - this.getBounds().width);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        Collection<DisplayEntry> entries = DisplayHistoryManager.INSTANCE.getEntries(this);
        
        if (containsMouse(mouseX, mouseY)) {
            for (DisplayEntry entry : entries) {
                if (!entry.isStable()) {
                    entry.setReachedStable(true);
                }
            }
            
            scroll.setTo(scroll.target() + ClothConfigInitializer.getScrollStep() * amount * (getBounds().getWidth() / -50.0), ClothConfigInitializer.getScrollDuration());
            return true;
        }
        
        for (DisplayEntry entry : entries) {
            if (entry.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }
        
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (DisplayEntry entry : DisplayHistoryManager.INSTANCE.getEntries(this)) {
            if (!ignoreNextMouse && entry.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ignoreNextMouse) {
            ignoreNextMouse = false;
            return true;
        }
        
        Collection<DisplayEntry> entries = DisplayHistoryManager.INSTANCE.getEntries(this);
        
        for (DisplayEntry entry : entries) {
            if (entry.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        if (ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
            Point mouse = PointHelper.ofMouse();
            
            if (containsMouse(mouse)) {
                double xOffset = -this.scroll.value();
                for (DisplayEntry entry : entries) {
                    if (entry.isStable() && entry.getBounds().contains(mouse.x + xOffset, mouse.y)) {
                        DisplayHistoryManager.INSTANCE.removeEntry(entry);
                        scroll.setAs(scroll.target() - getBounds().getWidth());
                        scroll.setTo(scroll.target() + getBounds().getWidth(), 800);
                        DisplayCompositeWidget.DisplayDraggableComponent component = new DisplayCompositeWidget.DisplayDraggableComponent(Widgets.concat(entry.getWidgets()), entry.getDisplay(),
                                entry.getBounds().clone(),
                                new Rectangle(0, 0, entry.getSize().width, entry.getSize().height));
                        DraggingContext.getInstance().renderToVoid(component);
                        return true;
                    }
                }
            }
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Collection<DisplayEntry> entries = DisplayHistoryManager.INSTANCE.getEntries(this);
        
        for (DisplayEntry entry : entries) {
            if (entry.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
        if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
            Point mouse = PointHelper.ofMouse();
            
            if (containsMouse(mouse)) {
                double xOffset = -this.scroll.value();
                for (DisplayEntry entry : entries) {
                    if (entry.isStable() && entry.getBounds().contains(mouse.x + xOffset, mouse.y)) {
                        DisplayHistoryManager.INSTANCE.removeEntry(entry);
                        scroll.setAs(scroll.target() - getBounds().getWidth());
                        scroll.setTo(scroll.target() + getBounds().getWidth(), 800);
                        DisplayCompositeWidget.DisplayDraggableComponent component = new DisplayCompositeWidget.DisplayDraggableComponent(Widgets.concat(entry.getWidgets()), entry.getDisplay(),
                                entry.getBounds().clone(),
                                new Rectangle(0, 0, entry.getSize().width, entry.getSize().height));
                        DraggingContext.getInstance().renderToVoid(component);
                        return true;
                    }
                }
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public DraggedAcceptorResult acceptDragged(DraggingContext<Screen> context, DraggableComponent<?> component) {
        return component.<Display>ifMatches(display -> {
            Point pos = context.getCurrentPosition();
            if (containsMouse(pos)) {
                addDisplay(context.getCurrentBounds().clone(), display);
                ignoreNextMouse = true;
                return true;
            } else {
                return false;
            }
        }) ? DraggedAcceptorResult.CONSUMED : DraggedAcceptorResult.PASS;
    }
    
    public void addDisplay(@Nullable Rectangle bounds, Display display) {
        DisplayHistoryManager.INSTANCE.addEntry(this, bounds, display);
        this.scroll.setAs(this.scroll.target() + getBounds().getWidth());
        this.scroll.setTo(0, 800);
    }
    
    @Override
    @Nullable
    public DraggableComponent<Object> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        if (containsMouse(mouseX, mouseY)) {
            double xOffset = -this.scroll.value();
            Collection<DisplayEntry> entries = DisplayHistoryManager.INSTANCE.getEntries(this);
            
            for (DisplayEntry entry : entries) {
                if (entry.isStable() && entry.getBounds().contains(mouseX + xOffset, mouseY)) {
                    for (DraggableComponentProviderWidget<Object> widget : Widgets.<DraggableComponentProviderWidget<Object>>walk(entry.getWidgets(), child -> child instanceof DraggableComponentProviderWidget)) {
                        DraggableComponent<Object> hovered = widget.getHovered(context, entry.transformMouseX(mouseX), entry.transformMouseY(mouseY));
                        
                        if (hovered != null) {
                            return hovered;
                        }
                    }
                    
                    return (DraggableComponent<Object>) (DraggableComponent<?>) new DisplayCompositeWidget.DisplayDraggableComponent(Widgets.concat(entry.getWidgets()), entry.getDisplay(),
                            entry.getBounds().clone(),
                            new Rectangle(0, 0, entry.getSize().width, entry.getSize().height)) {
                        @Override
                        public void drag() {
                            DisplayHistoryManager.INSTANCE.removeEntry(entry);
                            scroll.setAs(scroll.target() - getBounds().getWidth());
                            scroll.setTo(scroll.target() + getBounds().getWidth(), 800);
                        }
                        
                        @Override
                        public void release(DraggedAcceptorResult result) {
                            if (result == DraggedAcceptorResult.PASS) {
                                addDisplay(DraggingContext.getInstance().getCurrentBounds().clone(), entry.getDisplay());
                            }
                        }
                    };
                }
                xOffset += getBounds().getWidth();
            }
        }
        
        return null;
    }
}
