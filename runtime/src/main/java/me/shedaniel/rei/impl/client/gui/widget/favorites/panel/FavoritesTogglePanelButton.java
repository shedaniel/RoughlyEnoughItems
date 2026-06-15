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

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.network.chat.Component;

@SuppressWarnings("UnstableApiUsage")
public class FavoritesTogglePanelButton extends FadingFavoritesPanelButton {
    public FavoritesTogglePanelButton(FavoritesListWidget parent) {
        super(parent);
    }
    
    @Override
    protected void onClick() {
        parent.favoritePanel.expendState.setTo(!parent.favoritePanel.expendState.target(), ConfigObject.getInstance().isReducedMotion() ? 0 : 1500);
        parent.favoritePanel.resetRows();
    }
    
    @Override
    protected void queueTooltip() {
        Tooltip.create(Component.translatable("text.rei.add_favorite_widget")).queue();
    }
    
    @Override
    protected Rectangle updateArea(Rectangle fullArea) {
        return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - 16 - 4, 16, 16);
    }
    
    @Override
    protected boolean isAvailable(int mouseX, int mouseY) {
        boolean expended = parent.favoritePanel.expendState.value();
        return parent.fullBounds.contains(mouseX, mouseY) || REIRuntime.getInstance().getOverlay().orElseThrow().getEntryList().containsMouse(new Point(mouseX, mouseY)) || expended;
    }
    
    @Override
    protected void renderButtonText(GuiGraphics graphics) {
        float expendProgress = (float) parent.favoritePanel.expendState.progress();
        if (expendProgress < .9f) {
            int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * (1 - expendProgress)) << 24);
            graphics.pose().pushMatrix();
            graphics.pose().translate(bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3);
            graphics.drawString(font, "+", 0, 0, textColor, false);
            graphics.pose().popMatrix();
        }
        if (expendProgress > .1f) {
            int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * expendProgress) << 24);
            graphics.pose().pushMatrix();
            graphics.pose().translate(bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3);
            graphics.drawString(font, "+", 0, 0, textColor, false);
            graphics.pose().popMatrix();
        }
    }
}