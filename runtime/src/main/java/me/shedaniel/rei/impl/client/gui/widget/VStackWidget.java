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

import com.google.common.collect.ForwardingList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class VStackWidget {
    public static <T extends WidgetWithBounds> Builder<T> builder(int x, int y) {
        return builder(new Point(x, y));
    }
    
    public static <T extends WidgetWithBounds> Builder<T> builder(Point point) {
        return new Builder<>(point);
    }
    
    public static class Builder<T extends WidgetWithBounds> extends ForwardingList<T> {
        private final Point point;
        private final List<T> entries = new ArrayList<>();
        private int gap;
        private boolean calculateWidthDynamically = false;
        private boolean calculateTotalHeightDynamically = false;
        
        public Builder(Point point) {
            this.point = point;
        }
        
        @Override
        protected List<T> delegate() {
            return entries;
        }
        
        public Builder<T> gap(int gap) {
            this.gap = gap;
            return this;
        }
        
        public Builder<T> calculateWidthDynamically() {
            this.calculateWidthDynamically = true;
            return this;
        }
        
        public Builder<T> calculateTotalHeightDynamically() {
            this.calculateTotalHeightDynamically = true;
            return this;
        }
        
        public WidgetWithBounds build() {
            return VStackWidget.create(point, entries, gap, calculateWidthDynamically, calculateTotalHeightDynamically);
        }
    }
    
    public static <T extends WidgetWithBounds> WidgetWithBounds create(Point point, List<T> entries,
                                                                       int gap, boolean calculateWidthDynamically, boolean calculateTotalHeightDynamically) {
        Rectangle bounds = new Rectangle(point.x, point.y, collectMaximumWidth(entries), collectTotalHeight(entries, gap));
        
        List<CellWidget<T>> wrapped = CollectionUtils.map(entries, CellWidget::new);
        Widget update = Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            if (calculateWidthDynamically) {
                bounds.width = collectMaximumWidth(entries);
            }
            
            if (calculateTotalHeightDynamically) {
                bounds.height = collectTotalHeight(entries, gap);
            }
            
            bounds.move(point.x, point.y);
            
            int y = bounds.y;
            for (CellWidget<T> cell : wrapped) {
                cell.position.move(bounds.x, y);
                y += (calculateTotalHeightDynamically ? cell.getBounds().getHeight() : cell.height) + gap;
            }
        });
        List<Widget> innerWidgets = new ArrayList<>();
        innerWidgets.add(update);
        innerWidgets.addAll(wrapped);
        return Widgets.concatWithBounds(bounds, innerWidgets);
    }
    
    private static class CellWidget<T> extends DelegateWidgetWithTranslate {
        private final Point position = new Point();
        private final int height;
        
        public CellWidget(WidgetWithBounds widget) {
            super(widget, Matrix4f::new);
            this.height = widget.getBounds().getHeight();
        }
        
        @Override
        public WidgetWithBounds delegate() {
            return (WidgetWithBounds) super.delegate();
        }
        
        @Override
        protected Matrix4f translate() {
            Rectangle bounds = delegate().getBounds();
            return new Matrix4f().translate(position.x - bounds.x, position.y - bounds.y, 0);
        }
    }
    
    private static int collectMaximumWidth(List<? extends WidgetWithBounds> cells) {
        int width = 0;
        for (WidgetWithBounds cell : cells) {
            width = Math.max(width, cell.getBounds().getWidth());
        }
        return width;
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
