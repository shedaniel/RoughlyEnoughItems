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

package me.shedaniel.rei.impl.client.gui.widget.region;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.EntryStacksRegionWidget;

public class RegionDraggableStack<T extends RegionEntry<T>> implements DraggableStack {
    private RealRegionEntry<T> entry;
    private EntryStack<?> stack;
    private WidgetWithBounds showcaseWidget;
    private int previousIndex = -1;
    
    public RegionDraggableStack(RealRegionEntry<T> entry, WidgetWithBounds showcaseWidget) {
        this.entry = entry;
        this.stack = entry.getEntry().toStack();
        this.showcaseWidget = showcaseWidget;
    }
    
    @Override
    public EntryStack<?> getStack() {
        return stack;
    }
    
    @Override
    public void drag() {
        if (showcaseWidget == null && entry.region.listener.removeOnDrag()) {
            previousIndex = entry.region.indexOf(entry);
            entry.region.remove(entry, EntryStacksRegionWidget.RemovalMode.MIGRATED);
        }
    }
    
    public RealRegionEntry<T> getEntry() {
        return entry;
    }
    
    @Override
    public void release(DraggedAcceptorResult result) {
        if (result != DraggedAcceptorResult.CONSUMED) {
            if (!entry.region.listener.removeOnDrag()) {
                DraggingContext.getInstance().renderBackToPosition(this, DraggingContext.getInstance().getCurrentPosition(),
                        () -> entry.pos.value().getLocation());
            } else if (showcaseWidget != null) {
                DraggingContext.getInstance().renderBackToPosition(this, DraggingContext.getInstance().getCurrentPosition(),
                        () -> new Point(showcaseWidget.getBounds().x, showcaseWidget.getBounds().y));
            } else if (result == DraggedAcceptorResult.ACCEPTED) {
                DraggingContext<?> context = DraggingContext.getInstance();
                double x = context.getCurrentPosition().x;
                double y = context.getCurrentPosition().y + entry.region.getScrollAmount();
                entry.region.drop(entry, x, y, previousIndex);
            } else {
                entry.region.drop(entry);
            }
        } else {
            entry.region.listener.onConsumed(entry);
        }
    }
    
    public WidgetWithBounds getShowcaseWidget() {
        return showcaseWidget;
    }
}
