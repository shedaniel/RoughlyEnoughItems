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

package me.shedaniel.rei.impl.client.gui.widget.favorites.panel;

import me.shedaniel.clothconfig2.api.animator.ProgressValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public abstract class FavoritesPanel extends WidgetWithBounds {
    public final ProgressValueAnimator<Boolean> expendState = ValueAnimator.ofBoolean(0.1, false);
    protected final FavoritesListWidget parent;
    protected final Rectangle bounds = new Rectangle();
    protected final Rectangle innerBounds = new Rectangle();
    
    public FavoritesPanel(FavoritesListWidget parent) {
        this.parent = parent;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.bounds.setBounds(updatePanelArea(parent.favoritesBounds));
        this.expendState.update(delta);
    }
    
    private Rectangle updatePanelArea(Rectangle fullArea) {
        float progress = (float) this.expendState.progress();
        Rectangle buttonArea = getButtonArea();
        Rectangle targetArea = getTargetArea(fullArea);
        int x1 = Mth.lerpInt(progress, buttonArea.x, targetArea.x);
        int y1 = Mth.lerpInt(progress, buttonArea.y, targetArea.y);
        int x2 = Mth.lerpInt(progress, buttonArea.getMaxX(), targetArea.getMaxX());
        int y2 = Mth.lerpInt(progress, buttonArea.getMaxY(), targetArea.getMaxY());
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    public FavoritesListWidget getParent() {
        return parent;
    }
    
    public Rectangle getInnerBounds() {
        return innerBounds;
    }
    
    protected abstract Rectangle getButtonArea();
    
    protected abstract Rectangle getTargetArea(Rectangle fullArea);
}
