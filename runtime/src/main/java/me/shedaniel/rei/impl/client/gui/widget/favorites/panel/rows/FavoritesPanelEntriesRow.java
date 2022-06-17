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

package me.shedaniel.rei.impl.client.gui.widget.favorites.panel.rows;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.widget.DisplayedEntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesPanel;
import me.shedaniel.rei.impl.client.gui.widget.region.RealRegionEntry;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionDraggableStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;
import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.notSteppingOnExclusionZones;

@SuppressWarnings("UnstableApiUsage")
public class FavoritesPanelEntriesRow extends FavoritesPanelRow {
    private final FavoritesPanel panel;
    private final List<FavoriteEntry> entries;
    private final List<SectionFavoriteWidget> widgets;
    private int blockedCount;
    private int lastY;
    
    public FavoritesPanelEntriesRow(FavoritesPanel panel, List<FavoriteEntry> entries) {
        this.panel = panel;
        this.entries = entries;
        int entrySize = entrySize();
        this.widgets = CollectionUtils.map(this.entries, entry -> new SectionFavoriteWidget(new Point(0, 0), entrySize, entry));
        
        for (SectionFavoriteWidget widget : this.widgets) {
            widget.size.setTo(entrySize * 100, 300);
        }
        
        this.lastY = panel.getInnerBounds().y;
        
        updateEntriesPosition(widget -> false);
    }
    
    @Override
    public int getRowHeight() {
        return Mth.ceil((entries.size() + blockedCount) / (panel.getInnerBounds().width / (float) entrySize())) * entrySize();
    }
    
    @Override
    public void render(PoseStack matrices, Rectangle innerBounds, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
        this.lastY = y;
        int entrySize = entrySize();
        boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
        updateEntriesPosition(entry -> true);
        for (SectionFavoriteWidget widget : widgets) {
            widget.update(delta);
            
            if (widget.getBounds().getMaxY() > lastY && widget.getBounds().getY() <= lastY + rowHeight) {
                if (widget.getCurrentEntry().isEmpty())
                    continue;
                widget.render(matrices, mouseX, mouseY, delta);
            }
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY + panel.getScrolledAmount(), button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY + panel.getScrolledAmount(), button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY + panel.getScrolledAmount(), button);
    }
    
    @Nullable
    public DraggableStack getHoveredStack(double mouseX, double mouseY) {
        for (SectionFavoriteWidget widget : widgets) {
            if (widget.containsMouse(mouseX, mouseY + panel.getScrolledAmount())) {
                RealRegionEntry<FavoriteEntry> entry = new RealRegionEntry<>(panel.getParent().getRegion(), widget.entry.copy(), entrySize());
                entry.size.setAs(entrySize() * 100);
                return new RegionDraggableStack<>(entry, widget);
            }
        }
        
        return null;
    }
    
    @Nullable
    public EntryStack<?> getFocusedStack(Point mouse) {
        for (SectionFavoriteWidget widget : widgets) {
            if (widget.containsMouse(mouse)) {
                return ClientEntryStacks.of(widget.entry.getRenderer(false)).copy();
            }
        }
        
        return null;
    }
    
    private class SectionFavoriteWidget extends DisplayedEntryWidget {
        private ValueAnimator<FloatingPoint> pos = ValueAnimator.ofFloatingPoint();
        private NumberAnimator<Double> size = ValueAnimator.ofDouble();
        private FavoriteEntry entry;
        
        protected SectionFavoriteWidget(Point point, int entrySize, FavoriteEntry entry) {
            super(point, entrySize);
            this.entry = entry;
            entry(ClientEntryStacks.of(entry.getRenderer(true)));
            noBackground();
        }
        
        public void moveTo(boolean animated, int xPos, int yPos) {
            pos.setTo(new FloatingPoint(xPos, yPos), animated ? 200 : -1);
        }
        
        public void update(float delta) {
            this.pos.update(delta);
            this.size.update(delta);
            this.getBounds().width = this.getBounds().height = (int) Math.round(this.size.doubleValue() / 100);
            double offsetSize = (entrySize() - this.size.doubleValue() / 100) / 2;
            this.getBounds().x = (int) Math.round(pos.value().x + offsetSize);
            this.getBounds().y = (int) Math.round(pos.value().y + offsetSize) + lastY;
        }
        
        @Override
        @Nullable
        public Tooltip getCurrentTooltip(TooltipContext context) {
            context = TooltipContext.ofMouse();
            if (!panel.getInnerBounds().contains(context.getPoint())) return null;
            Tooltip tooltip = super.getCurrentTooltip(context);
            if (tooltip != null) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("tooltip.rei.drag_to_add_favorites"));
            }
            return tooltip;
        }
    }
    
    public void updateEntriesPosition(Predicate<SectionFavoriteWidget> animated) {
        int entrySize = entrySize();
        this.blockedCount = 0;
        int width = panel.getInnerBounds().width / entrySize;
        int currentX = 0;
        int currentY = 0;
        
        int slotIndex = 0;
        for (SectionFavoriteWidget widget : this.widgets) {
            while (true) {
                int xPos = currentX * entrySize + panel.getInnerBounds().x - 1;
                int yPos = currentY * entrySize;
                
                currentX++;
                if (currentX >= width) {
                    currentX = 0;
                    currentY++;
                }
                
                if (notSteppingOnExclusionZones(xPos, yPos + lastY - panel.getScrolledAmountInt(), entrySize, entrySize)) {
                    widget.moveTo(animated.test(widget), xPos, yPos);
                    break;
                } else {
                    blockedCount++;
                }
            }
        }
    }
}