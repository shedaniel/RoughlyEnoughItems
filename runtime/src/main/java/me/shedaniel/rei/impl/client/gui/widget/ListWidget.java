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

import com.mojang.math.Matrix4f;
import dev.architectury.utils.value.IntValue;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.gui.GuiComponent;

import java.util.ArrayList;
import java.util.List;

public class ListWidget {
    public static <T> Builder<T> builderOf(Rectangle bounds, List<T> entries, ListCellRenderer<T> cellRenderer) {
        return new Builder<>(bounds, entries, cellRenderer);
    }
    
    public static <T extends WidgetWithBounds> WidgetBuilder<T> builderOfWidgets(Rectangle bounds, List<T> entries) {
        return new WidgetBuilder<>(bounds, entries);
    }
    
    public static abstract class AbstractBuilder<T extends WidgetWithBounds, SELF extends AbstractBuilder<T, SELF>> {
        protected final Rectangle bounds;
        protected IntValue selected = new IntValue() {
            private int value = -1;
            
            @Override
            public void accept(int i) {
                this.value = i;
            }
            
            @Override
            public int getAsInt() {
                return value;
            }
        };
        protected int gap = 4;
        protected boolean calculateTotalHeightDynamically = false;
        
        protected AbstractBuilder(Rectangle bounds) {
            this.bounds = bounds;
        }
        
        public SELF selected(IntValue selected) {
            this.selected = selected;
            return (SELF) this;
        }
        
        public SELF selected(int selected) {
            this.selected.accept(selected);
            return (SELF) this;
        }
        
        public SELF gap(int gap) {
            this.gap = gap;
            return (SELF) this;
        }
        
        public SELF calculateTotalHeightDynamically(boolean calculateTotalHeightDynamically) {
            this.calculateTotalHeightDynamically = calculateTotalHeightDynamically;
            return (SELF) this;
        }
        
        public abstract WidgetWithBounds build();
    }
    
    public static class Builder<T> extends AbstractBuilder<WidgetWithBounds, Builder<T>> {
        protected final List<T> entries;
        protected final ListCellRenderer<T> cellRenderer;
        protected ListEntryPredicate<T> isSelectable = (index, entry) -> false;
        
        protected Builder(Rectangle bounds, List<T> entries, ListCellRenderer<T> cellRenderer) {
            super(bounds);
            this.entries = entries;
            this.cellRenderer = cellRenderer;
        }
        
        public Builder<T> isSelectable(ListEntryPredicate<T> isSelectable) {
            this.isSelectable = isSelectable;
            return this;
        }
        
        @Override
        public WidgetWithBounds build() {
            return ListWidget.create(bounds, entries, selected, gap, calculateTotalHeightDynamically,
                    cellRenderer, isSelectable);
        }
    }
    
    public static class WidgetBuilder<T extends WidgetWithBounds> extends AbstractBuilder<T, WidgetBuilder<T>> {
        protected final List<T> entries;
        protected ListEntryPredicate<T> isSelectable = (index, entry) -> false;
        
        protected WidgetBuilder(Rectangle bounds, List<T> entries) {
            super(bounds);
            this.entries = entries;
        }
        
        public WidgetBuilder<T> isSelectable(ListEntryPredicate<T> isSelectable) {
            this.isSelectable = isSelectable;
            return this;
        }
        
        @Override
        public WidgetWithBounds build() {
            return ListWidget.create(bounds, entries, selected, gap, calculateTotalHeightDynamically,
                    isSelectable);
        }
    }
    
    public static <T> WidgetWithBounds create(Rectangle bounds, List<T> entries, IntValue selected, int gap,
                                              boolean calculateTotalHeightDynamically, ListCellRenderer<T> cellRenderer,
                                              ListEntryPredicate<T> isSelectable) {
        int[] i = {0};
        return create(bounds, CollectionUtils.map(entries, entry -> cellRenderer.create(i[0]++, entry)),
                selected, gap, calculateTotalHeightDynamically, (index, entry) -> isSelectable.test(index, entries.get(index)));
    }
    
    public static <T extends WidgetWithBounds> WidgetWithBounds create(Rectangle bounds, List<T> entries, IntValue selected, int gap,
                                                                       boolean calculateTotalHeightDynamically, ListEntryPredicate<T> isSelectable) {
        int[] height = {collectTotalHeight(entries, gap)};
        
        Rectangle innerBounds = bounds.clone();
        if (height[0] > bounds.getHeight()) {
            innerBounds.width -= 6;
        }
        
        int[] i = {0};
        List<CellWidget<T>> wrapped = CollectionUtils.map(entries, cell -> new CellWidget<>(innerBounds, i[0]++, cell, selected, entries, isSelectable));
        Widget update = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            if (calculateTotalHeightDynamically) {
                height[0] = collectTotalHeight(entries, gap);
                innerBounds.width = height[0] > bounds.getHeight() ? bounds.width - 6 : bounds.width;
            }
            
            int y = bounds.y;
            for (CellWidget<T> cell : wrapped) {
                cell.position.move(bounds.x, y);
                y += (calculateTotalHeightDynamically ? cell.getBounds().getHeight() : cell.height) + gap;
            }
            
            if (selected.getAsInt() != -1) {
                CellWidget<T> cellWidget = wrapped.get(selected.getAsInt());
                int x1 = innerBounds.x, x2 = innerBounds.getMaxX();
                boolean contains = new Rectangle(x1 - 1, cellWidget.position.y - 1, x2 - x1 + 2, cellWidget.getBounds().height + 2).contains(mouseX, mouseY);
                GuiComponent.fill(matrices, x1 - 1, cellWidget.position.y - 1, x2 + 1,
                        cellWidget.position.y + cellWidget.getBounds().height + 1, contains ? 0xFFD0D0D0 : 0xFF8F8F8F);
                GuiComponent.fill(matrices, x1, cellWidget.position.y, x2,
                        cellWidget.position.y + cellWidget.getBounds().height, 0xFF000000);
            }
        });
        List<Widget> innerWidgets = new ArrayList<>();
        innerWidgets.add(update);
        innerWidgets.addAll(wrapped);
        return Widgets.concatWithBounds(() -> new Rectangle(bounds.x, bounds.y, bounds.width, height[0]),
                innerWidgets);
    }
    
    private static class CellWidget<T> extends DelegateWidgetWithTranslate {
        private final Rectangle bounds;
        private final int index;
        private final Point position = new Point();
        private final int height;
        private final IntValue selected;
        private final List<T> list;
        private final ListEntryPredicate<T> isSelectable;
        
        public CellWidget(Rectangle bounds, int index, WidgetWithBounds widget, IntValue selected, List<T> list, ListEntryPredicate<T> isSelectable) {
            super(widget, Matrix4f::new);
            this.bounds = bounds;
            this.index = index;
            this.height = widget.getBounds().getHeight();
            this.selected = selected;
            this.list = list;
            this.isSelectable = isSelectable;
        }
        
        @Override
        public WidgetWithBounds delegate() {
            return (WidgetWithBounds) super.delegate();
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean clicked = super.mouseClicked(mouseX, mouseY, button);
            Rectangle bounds = delegate().getBounds();
            
            if (clicked || new Rectangle(position.x, position.y, this.bounds.width, bounds.height).contains(mouseX, mouseY)) {
                if (isSelectable.test(index, list.get(index))) {
                    selected.accept(index);
                    if (!clicked) {
                        Widgets.produceClickSound();
                    }
                    return true;
                }
            }
            
            return clicked;
        }
        
        @Override
        protected Matrix4f translate() {
            Rectangle bounds = delegate().getBounds();
            return Matrix4f.createTranslateMatrix(position.x - bounds.x, position.y - bounds.y, 0);
        }
    }
    
    private static int collectTotalHeight(List<? extends WidgetWithBounds> cells, int gap) {
        int height = Math.max(0, (cells.size() - 1) * gap);
        for (WidgetWithBounds cell : cells) {
            height += cell.getBounds().getHeight();
        }
        return height;
    }
    
    @FunctionalInterface
    public interface ListCellRenderer<T> {
        WidgetWithBounds create(int index, T entry);
    }
    
    @FunctionalInterface
    public interface ListEntryPredicate<T> {
        boolean test(int index, T entry);
    }
}
