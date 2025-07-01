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
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.FavoriteAddWidgetMode;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class FavoritesTogglePanelButton extends FadingFavoritesPanelButton {
    private final Component tooltip;
    private final Runnable onClick;
    protected final ProgressValueAnimator<Boolean> progress;
    
    public FavoritesTogglePanelButton(FavoritesListWidget parent, Component tooltip, ProgressValueAnimator<Boolean> progress, Runnable onClick) {
        super(parent);
        this.tooltip = tooltip;
        this.progress = progress;
        this.onClick = onClick;
    }
    
    @Override
    protected void onClick() {
        this.onClick.run();
    }
    
    @Override
    protected void queueTooltip() {
        Tooltip.create(this.tooltip).queue();
    }
    
    @Override
    protected boolean isAvailable(int mouseX, int mouseY) {
        boolean expended = this.progress.value();
        return parent.fullBounds.contains(mouseX, mouseY) || REIRuntime.getInstance().getOverlay().orElseThrow().getEntryList().containsMouse(new Point(mouseX, mouseY)) || expended;
    }
    
    public static class ToggleAdd extends FavoritesTogglePanelButton {
        public ToggleAdd(FavoritesListWidget parent) {
            super(parent, Component.translatable("text.rei.add_favorite_widget"), parent.favoritePanel.expendState, () -> {
                parent.favoritePanel.expendState.setTo(!parent.favoritePanel.expendState.target(), ConfigObject.getInstance().isReducedMotion() ? 0 : 1500);
                parent.favoritePanel.resetRows();
            });
        }
        
        @Override
        protected boolean isOtherActive() {
            return parent.calculatorPanel.expendState.progress() > 0.1f;
        }
        
        @Override
        protected Rectangle updateArea(Rectangle fullArea) {
            return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - 16 - 4, 16, 16);
        }
        
        @Override
        protected void renderContents(GuiGraphics graphics) {
            graphics.drawSpecial(source -> {
                float expendProgress = (float) this.progress.progress();
                if (expendProgress < .9f) {
                    int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * (1 - expendProgress)) << 24);
                    font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880);
                }
                if (expendProgress > .1f) {
                    int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * expendProgress) << 24);
                    font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880);
                }
            });
        }
        
        @Override
        protected FavoriteAddWidgetMode getFavoriteAddWidgetMode() {
            return ConfigObject.getInstance().getFavoriteAddWidgetMode();
        }
    }
    
    public static class ToggleCalculator extends FavoritesTogglePanelButton {
        public ToggleCalculator(FavoritesListWidget parent) {
            super(parent, Component.translatable("text.rei.calculator_widget"), parent.calculatorPanel.expendState, () -> {
                parent.calculatorPanel.expendState.setTo(!parent.calculatorPanel.expendState.target(), ConfigObject.getInstance().isReducedMotion() ? 0 : 1500);
            });
        }
        
        @Override
        protected boolean isOtherActive() {
            return parent.favoritePanel.expendState.progress() > 0.1f;
        }
        
        @Override
        protected Rectangle updateArea(Rectangle fullArea) {
            int i = ConfigObject.getInstance().getFavoriteAddWidgetMode() == FavoriteAddWidgetMode.ALWAYS_INVISIBLE ? 0 : 1;
            return new Rectangle(fullArea.x + 4 + i * 20, fullArea.getMaxY() - 16 - 4, 16, 16);
        }
        
        @Override
        protected void renderContents(GuiGraphics graphics) {
            graphics.drawSpecial(source -> {
                float expendProgress = (float) this.progress.progress();
                if (expendProgress < .9f) {
                    int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * (1 - expendProgress)) << 24);
                    font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880);
                }
                if (expendProgress > .1f) {
                    int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * expendProgress) << 24);
                    font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880);
                }
            });
        }
        
        @Override
        protected FavoriteAddWidgetMode getFavoriteAddWidgetMode() {
            if (!ConfigObject.getInstance().isCalculatorPanelEnabled()) return FavoriteAddWidgetMode.ALWAYS_INVISIBLE;
            FavoriteAddWidgetMode addWidgetMode = ConfigObject.getInstance().getFavoriteAddWidgetMode();
            if (addWidgetMode == FavoriteAddWidgetMode.ALWAYS_INVISIBLE) return FavoriteAddWidgetMode.AUTO_HIDE;
            return addWidgetMode;
        }
    }
}