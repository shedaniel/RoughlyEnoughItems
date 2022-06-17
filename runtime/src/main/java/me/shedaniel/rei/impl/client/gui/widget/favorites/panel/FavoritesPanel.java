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
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.ProgressValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.rows.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FavoritesPanel extends WidgetWithBounds {
    private final FavoritesListWidget parent;
    public final ProgressValueAnimator<Boolean> expendState = ValueAnimator.ofBoolean(0.1, false);
    private final Rectangle bounds = new Rectangle();
    private final Rectangle innerBounds = new Rectangle();
    private final LazyResettable<List<FavoritesPanelRow>> rows = new LazyResettable<>(() -> {
        List<FavoritesPanelRow> rows = new ArrayList<>();
        for (FavoriteEntryType.Section section : FavoriteEntryType.registry().sections()) {
            rows.add(new FavoritesPanelSectionRow(section.getText(), section.getText().copy().withStyle(style -> style.withUnderlined(true))));
            rows.add(new FavoritesPanelEntriesRow(this, CollectionUtils.map(section.getEntries(), FavoriteEntry::copy)));
            rows.add(new FavoritesPanelSeparatorRow());
        }
        if (!rows.isEmpty()) rows.remove(rows.size() - 1);
        rows.add(new FavoritesPanelEmptyRow(4));
        return rows;
    });
    private final ScrollingContainer scroller = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return innerBounds;
        }
        
        @Override
        public int getMaxScrollHeight() {
            return Math.max(1, rows.get().stream().mapToInt(FavoritesPanelRow::getRowHeight).sum());
        }
    };
    
    public FavoritesPanel(FavoritesListWidget parent) {
        this.parent = parent;
    }
    
    public void resetRows() {
        this.rows.reset();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.bounds.setBounds(updatePanelArea(parent.favoritesBounds));
        this.innerBounds.setBounds(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 20);
        this.expendState.update(delta);
        int buttonColor = 0xFFFFFF | (Math.round(0x34 * Math.min((float) expendState.progress() * 2, 1)) << 24);
        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
        scroller.updatePosition(delta);
        
        if (expendState.value()) {
            ScissorsHandler.INSTANCE.scissor(innerBounds);
            matrices.pushPose();
            matrices.translate(0, -scroller.scrollAmount(), 0);
            int y = innerBounds.y;
            for (FavoritesPanelRow row : rows.get()) {
                row.render(matrices, innerBounds, innerBounds.x, y, innerBounds.width, row.getRowHeight(), mouseX, mouseY + scroller.scrollAmountInt(), delta);
                y += row.getRowHeight();
            }
            matrices.popPose();
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
    }
    
    private Rectangle updatePanelArea(Rectangle fullArea) {
        int currentWidth = 16 + Math.round(Math.min((float) expendState.progress(), 1) * (fullArea.getWidth() - 16 - 8));
        int currentHeight = 16 + Math.round((float) expendState.progress() * (fullArea.getHeight() * 0.4f - 16 - 8 + 4));
        return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - currentHeight - 4, currentWidth, currentHeight);
    }
    
    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (innerBounds.contains(d, e)) {
            scroller.offset(ClothConfigInitializer.getScrollStep() * -f, true);
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return rows.get();
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
    
    public double getScrolledAmount() {
        return scroller.scrollAmount();
    }
    
    public int getScrolledAmountInt() {
        return scroller.scrollAmountInt();
    }
    
    @Nullable
    public DraggableStack getHoveredStack(double mouseX, double mouseY) {
        for (FavoritesPanelRow row : rows.get()) {
            if (row instanceof FavoritesPanelEntriesRow entriesRow) {
                DraggableStack hoveredStack = entriesRow.getHoveredStack(mouseX, mouseY);
                
                if (hoveredStack != null) {
                    return hoveredStack;
                }
            }
        }
        
        return null;
    }
    
    @Nullable
    public EntryStack<?> getFocusedStack(Point mouse) {
        for (FavoritesPanelRow row : rows.get()) {
            if (row instanceof FavoritesPanelEntriesRow entriesRow) {
                EntryStack<?> focusedStack = entriesRow.getFocusedStack(mouse);
                
                if (focusedStack != null) {
                    return focusedStack;
                }
            }
        }
        
        return null;
    }
}