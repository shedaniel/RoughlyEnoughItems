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

package me.shedaniel.rei.impl.client.gui.widget.favorites.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class FadingFavoritesPanelButton extends WidgetWithBounds {
    protected final FavoritesListWidget parent;
    public boolean wasClicked = false;
    public final NumberAnimator<Double> alpha = ValueAnimator.ofDouble(0);
    
    public final Rectangle bounds = new Rectangle();
    
    public FadingFavoritesPanelButton(FavoritesListWidget parent) {
        this.parent = parent;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.bounds.setBounds(updateArea(parent.favoritesBounds));
        boolean hovered = containsMouse(mouseX, mouseY);
        switch (ConfigObject.getInstance().getFavoriteAddWidgetMode()) {
            case ALWAYS_INVISIBLE:
                this.alpha.setAs(0);
                break;
            case AUTO_HIDE:
                this.alpha.setTo(hovered ? 1f : isAvailable(mouseX, mouseY) ? 0.5f : 0f, 260);
                break;
            case ALWAYS_VISIBLE:
                this.alpha.setAs(hovered ? 1f : 0.5f);
                break;
        }
        this.alpha.update(delta);
        int buttonColor = 0xFFFFFF | (Math.round(0x74 * alpha.floatValue()) << 24);
        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
        if (isVisible()) {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            renderButtonText(matrices, bufferSource);
            bufferSource.endBatch();
        }
        if (hovered) {
            queueTooltip();
        }
    }
    
    protected abstract boolean isAvailable(int mouseX, int mouseY);
    
    protected abstract void renderButtonText(PoseStack matrices, MultiBufferSource.BufferSource bufferSource);
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isVisible() {
        return Math.round(0x12 * alpha.floatValue()) > 0;
    }
    
    protected boolean wasClicked() {
        boolean tmp = this.wasClicked;
        this.wasClicked = false;
        return tmp;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isVisible() && containsMouse(mouseX, mouseY)) {
            this.wasClicked = true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (wasClicked() && isVisible() && containsMouse(mouseX, mouseY)) {
            onClick();
            return true;
        }
        return false;
    }
    
    protected abstract void onClick();
    
    protected abstract void queueTooltip();
    
    protected abstract Rectangle updateArea(Rectangle fullArea);
}