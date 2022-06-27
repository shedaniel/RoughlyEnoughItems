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
import com.mojang.math.Vector4f;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Dimension;
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisplayEntry extends WidgetWithBounds {
    private final LazyResettable<List<Widget>> widgets = new LazyResettable<>(this::setupWidgets);
    private final DisplayHistoryWidget parent;
    private final Display display;
    private final Dimension size = new Dimension(1, 1);
    private boolean hasInitialBounds;
    private final ValueAnimator<FloatingRectangle> bounds = ValueAnimator.ofFloatingRectangle();
    private final Button plusButton;
    private double xOffset = 0;
    private boolean reachedStable = false;
    private UUID uuid = UUID.randomUUID();
    
    public DisplayEntry(DisplayHistoryWidget parent, Display display, @Nullable Rectangle initialBounds) {
        this.display = display;
        this.parent = parent;
        this.hasInitialBounds = initialBounds != null;
        if (this.hasInitialBounds) {
            this.bounds.setAs(initialBounds.getFloatingBounds());
            this.plusButton = Widgets.createButton(new Rectangle(initialBounds.getMaxX() - 16, initialBounds.getMaxY() - 16, 10, 10), Component.literal("+"));
        } else {
            this.plusButton = Widgets.createButton(new Rectangle(-1000, -1000, 10, 10), Component.literal("+"));
        }
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
        if (hasInitialBounds) {
            if (this.size.width == 1 && this.size.height == 1) {
                this.bounds.setTo(newBounds, 700);
            } else {
                this.bounds.setAs(newBounds);
            }
        } else {
            this.bounds.setAs(newBounds);
            hasInitialBounds = true;
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
        if (!stable || !target.equals(bounds)) {
            poses.translate(0, 0, 600);
        }
        poses.translate(xOffset(), yOffset(), 0);
        poses.scale(xScale(), yScale(), 1.0F);
        
        for (Widget widget : widgets.get()) {
            widget.render(poses, transformMouseX(mouseX), transformMouseY(mouseY), delta);
        }
        poses.popPose();
        
        {
            poses.pushPose();
            if (stable && target.equals(bounds)) {
                poses.translate(this.xOffset, 0, 200);
            } else {
                poses.translate(0, 0, 800);
            }
            Vector4f mouse = new Vector4f((float) mouseX, (float) mouseY, 0, 1);
            mouse.transform(poses.last().pose());
            
            AutoCraftingEvaluator.AutoCraftingResult result = AutoCraftingEvaluator.evaluateAutoCrafting(false, false, display, display::provideInternalDisplayIds);
            
            plusButton.setEnabled(result.successful);
            plusButton.setTint(result.tint);
            plusButton.getBounds().setBounds(new Rectangle(bounds.getMaxX() - 14, bounds.getMaxY() - 14, 10, 10));
            
            if (result.hasApplicable) {
                plusButton.setText(Component.literal("+"));
                plusButton.render(poses, Math.round(mouse.x()), Math.round(mouse.y()), delta);
                poses.popPose();
                
                if (plusButton.containsMouse(Math.round(mouse.x()), Math.round(mouse.y()))) {
                    result.tooltipRenderer.accept(new Point(mouseX, mouseY), Tooltip::queue);
                }
                
                if (result.renderer != null) {
                    poses.pushPose();
                    if (!stable || !target.equals(bounds)) {
                        poses.translate(0, 0, 600);
                    }
                    poses.translate(xOffset(), yOffset(), 0);
                    poses.scale(xScale(), yScale(), 1.0F);
                    
                    result.renderer.render(poses, mouseX, mouseY, delta, widgets.get(), getBounds(), display);
                    poses.popPose();
                }
            } else {
                poses.popPose();
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX + xOffset, mouseY)) {
            for (Widget widget : widgets.get()) {
                if (widget.mouseClicked(transformMouseX(mouseX), transformMouseY(mouseY), button)) {
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX + xOffset, mouseY)) {
            for (Widget widget : widgets.get()) {
                if (widget.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button)) {
                    return true;
                }
            }
            
            if (button == 0 && plusButton.containsMouse(mouseX + xOffset, mouseY)) {
                AutoCraftingEvaluator.evaluateAutoCrafting(true, Screen.hasShiftDown(), display, display::provideInternalDisplayIds);
                Widgets.produceClickSound();
                return true;
            }
            
            ClientHelperImpl.getInstance()
                    .openDisplayViewingScreen(Map.of(CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory(), List.of(display)),
                            null, List.of(), List.of());
            Widgets.produceClickSound();
            return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            Widget.pushMouse(new Point(transformMouseX(mouse().x), transformMouseY(mouse().y)));
            for (Widget widget : widgets.get()) {
                if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        } finally {
            Widget.popMouse();
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private float xOffset() {
        FloatingRectangle bounds = this.bounds.value();
        FloatingRectangle target = this.bounds.target();
        float xOffset = (float) bounds.x;
        if (isStable() && target.equals(bounds)) {
            xOffset += this.xOffset;
        }
        return xOffset;
    }
    
    private float yOffset() {
        FloatingRectangle bounds = this.bounds.value();
        return (float) bounds.y;
    }
    
    private float xScale() {
        FloatingRectangle bounds = this.bounds.value();
        return (float) bounds.width / size.width;
    }
    
    private float yScale() {
        FloatingRectangle bounds = this.bounds.value();
        return (float) bounds.height / size.height;
    }
    
    protected int transformMouseX(int mouseX) {
        return Math.round((mouseX - xOffset()) / xScale());
    }
    
    protected int transformMouseY(int mouseY) {
        return Math.round((mouseY - yOffset()) / yScale());
    }
    
    protected double transformMouseX(double mouseX) {
        return (mouseX - xOffset()) / xScale();
    }
    
    protected double transformMouseY(double mouseY) {
        return (mouseY - yOffset()) / yScale();
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
