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

package me.shedaniel.rei.impl.client.gui.widget.favorites.trash;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesPanel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class TrashWidget extends WidgetWithBounds {
    private final Rectangle bounds = new Rectangle();
    private final FavoritesListWidget parent;
    private final NumberAnimator<Double> height;
    private double lastProgress;
    
    public TrashWidget(FavoritesListWidget parent) {
        this.parent = parent;
        this.height = ValueAnimator.ofDouble().withConvention(() -> {
            Rectangle fullBounds = this.parent.favoritesBounds;
            if (DraggingContext.getInstance().isDraggingComponent() && fullBounds.contains(DraggingContext.getInstance().getCurrentPosition())) {
                return Math.min(60D, fullBounds.height * 0.23D);
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
        if (updateBounds(delta)) {
            int alpha = 0x12 + (int) (0x22 * lastProgress * (Mth.cos((float) (System.currentTimeMillis() % 2000 / 1000F * Math.PI)) + 1) / 2);
            fillGradient(poses, this.bounds.x, this.bounds.y, this.bounds.getMaxX(), this.bounds.getMaxY(), 0xFFFFFF | (alpha << 24), 0xFFFFFF | (alpha << 24));
            int lineColor = (int) (0x60 * lastProgress) << 24 | 0xFFFFFF;
            fillGradient(poses, this.bounds.x, this.bounds.y, this.bounds.getMaxX(), this.bounds.y + 1, lineColor, lineColor);
            fillGradient(poses, this.bounds.x, this.bounds.getMaxY() - 1, this.bounds.getMaxX(), this.bounds.getMaxY(), lineColor, lineColor);
            
            fillGradient(poses, this.bounds.x, this.bounds.y + 1, this.bounds.x + 1, this.bounds.getMaxY() - 1, lineColor, lineColor);
            fillGradient(poses, this.bounds.getMaxX() - 1, this.bounds.y + 1, this.bounds.getMaxX(), this.bounds.getMaxY() - 1, lineColor, lineColor);
            
            Component text = Component.translatable("text.rei.dispose_here");
            if (0xAA * lastProgress > 0x4) {
                font.draw(poses, text, this.bounds.getCenterX() - font.width(text) / 2, this.bounds.getCenterY() - 4F, (int) (0xAA * lastProgress) << 24 | 0xFFFFFF);
            }
        }
    }
    
    public boolean updateBounds(float delta) {
        this.height.update(delta);
        double trashBoundsHeight = this.height.value();
        if (Math.round(trashBoundsHeight) > 0) {
            Rectangle fullBounds = parent.favoritesBounds;
            FavoritesPanel favoritePanel = parent.favoritePanel;
            double heightTarget = Math.min(150D, fullBounds.height * 0.23D);
            this.lastProgress = Math.pow(Mth.clamp(trashBoundsHeight / heightTarget, 0, 1), 7);
            int y = fullBounds.getMaxY() - 4 - favoritePanel.getBounds().height;
            bounds.setBounds(fullBounds.x + 4, (int) Math.round(y - trashBoundsHeight), fullBounds.width - 8, (int) Math.round(trashBoundsHeight - 4));
            return true;
        } else {
            bounds.setBounds(0, 0, 0, 0);
            this.lastProgress = 0.0D;
            return false;
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    public double getHeight() {
        return height.value();
    }
}
