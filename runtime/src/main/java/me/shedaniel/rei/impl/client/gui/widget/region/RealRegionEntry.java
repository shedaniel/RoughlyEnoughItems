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

import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.region.RegionEntry;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

public class RealRegionEntry<T extends RegionEntry<T>> {
    public EntryStacksRegionWidget<T> region;
    private T entry;
    private final RegionEntryWidget<T> widget;
    private boolean hidden;
    public ValueAnimator<FloatingPoint> pos = ValueAnimator.ofFloatingPoint();
    public NumberAnimator<Double> size = ValueAnimator.ofDouble();
    
    public RealRegionEntry(EntryStacksRegionWidget<T> region, T entry, int entrySize) {
        this.region = region;
        this.entry = entry;
        this.widget = (RegionEntryWidget<T>) new RegionEntryWidget<>(this, 0, 0, entrySize).noBackground();
    }
    
    public void remove() {
        if (!hidden) {
            this.hidden = true;
            if (!ConfigObject.getInstance().isFavoritesAnimated()) this.size.setAs(0);
            else this.size.setTo(0, 400);
        }
    }
    
    public void update(double delta) {
        this.pos.update(delta);
        this.size.update(delta);
        this.getWidget().getBounds().width = this.getWidget().getBounds().height = (int) Math.round(this.size.doubleValue() / 100);
        double offsetSize = (entrySize() - this.size.doubleValue() / 100) / 2;
        this.getWidget().getBounds().x = (int) Math.round(pos.value().x + offsetSize);
        this.getWidget().getBounds().y = (int) Math.round(pos.value().y + offsetSize) - (int) region.getScrollAmount();
    }
    
    public RegionEntryWidget<T> getWidget() {
        return widget;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    
    public int hashIgnoreAmount() {
        return entry.hashCode();
    }
    
    public T getEntry() {
        return entry;
    }
    
    public void moveTo(boolean animated, int xPos, int yPos) {
        pos.setTo(new FloatingPoint(xPos, yPos), animated && ConfigObject.getInstance().isFavoritesAnimated() ? 200 : -1);
    }
}