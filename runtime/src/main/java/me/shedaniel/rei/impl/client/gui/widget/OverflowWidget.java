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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;

@SuppressWarnings("UnstableApiUsage")
public class OverflowWidget extends DelegateWidgetWithTranslate {
    private final Rectangle bounds;
    private final NumberAnimator<Float> scale;
    private final ValueAnimator<FloatingPoint> translate;
    private final ValueAnimator<FloatingPoint> velocity;
    private boolean dragging;
    
    public OverflowWidget(Rectangle bounds, WidgetWithBounds widget) {
        super(widget, Matrix4f::new);
        this.bounds = bounds;
        this.scale = ValueAnimator.ofFloat()
                .setAs(1f);
        this.translate = ValueAnimator.ofFloatingPoint()
                .setAs(new FloatingPoint(-widget.getBounds().width / 2f, -bounds.height / 2f));
        this.velocity = ValueAnimator.ofFloatingPoint()
                .setAs(new FloatingPoint(0f, 0f));
    }
    
    @Override
    protected Matrix4f translate() {
        FloatingPoint translate = this.translate.value();
        float scale = 1 / Math.max(this.scale.floatValue(), 0.001f);
        Matrix4f matrix = Matrix4f.createTranslateMatrix(bounds.getCenterX() + (float) translate.x * scale, bounds.getCenterY() + (float) translate.y * scale, 0);
        matrix.multiply(Matrix4f.createScaleMatrix(scale, scale, 1));
        return matrix;
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        Rectangle widgetBounds = ((WidgetWithBounds) delegate()).getBounds();
        this.scale.update(delta);
        this.scale.setTarget(ScrollingContainer.handleBounceBack(this.scale.target() - 0.78,
                Math.min(widgetBounds.width * 1.0F / getBounds().width, widgetBounds.height * 1.0F / getBounds().height) - 0.78, delta, .001) + 0.78);
        this.translate.update(delta);
        for (int i = 0; i < 3; i++) {
            this.translate.setAs(new FloatingPoint(
                    ScrollingContainer.handleBounceBack(this.translate.target().x + widgetBounds.width - getBounds().width / 2 * scale.value(),
                            widgetBounds.width - getBounds().width * scale.value(), delta, .0001) - (widgetBounds.width - getBounds().width / 2 * scale.value()),
                    ScrollingContainer.handleBounceBack(this.translate.target().y + widgetBounds.height - getBounds().height / 2 * scale.value(),
                            widgetBounds.height - getBounds().height * scale.value(), delta, .0001) - (widgetBounds.height - getBounds().height / 2 * scale.value())
            ));
        }
        if (!RoughlyEnoughItemsCoreClient.isLeftMousePressed) {
            this.translate.setAs(new FloatingPoint(this.translate.value().x + this.velocity.value().x, this.translate.value().y + this.velocity.value().y));
        }
        this.velocity.update(delta);
        this.velocity.setTo(new FloatingPoint(
                ScrollingContainer.handleBounceBack(this.velocity.target().x, 0, delta, .0001),
                ScrollingContainer.handleBounceBack(this.velocity.target().y, 0, delta, .0001)
        ), ConfigObject.getInstance().isReducedMotion() ? 0 : 20);
        
        try (CloseableScissors scissors = scissor(poseStack, this.bounds)) {
            boolean containsMouse = this.bounds.contains(mouseX, mouseY);
            
            if (containsMouse) {
                super.render(poseStack, mouseX, mouseY, delta);
            } else {
                super.render(poseStack, Integer.MAX_VALUE, Integer.MAX_VALUE, delta);
            }
        }
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsMouse(mouseX, mouseY)) {
            this.scale.setTo(this.scale.target() + amount * -0.2f, ConfigObject.getInstance().isReducedMotion() ? 0 : 300);
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (containsMouse(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            double newXTranslate = translate.target().x;
            double newYTranslate = translate.target().y;
            newXTranslate += deltaX * scale.doubleValue();
            newYTranslate += deltaY * scale.doubleValue();
            
            translate.setAs(new FloatingPoint(newXTranslate, newYTranslate));
            velocity.setAs(new FloatingPoint(deltaX * scale.doubleValue(), deltaY * scale.doubleValue()));
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
