/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.gui.widget;


import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

/**
 * @see Tooltip
 */
@ApiStatus.Internal
public class QueuedTooltip implements Tooltip {
    
    private Point location;
    private List<String> text;
    
    private QueuedTooltip(Point location, Collection<String> text) {
        this.location = location;
        this.text = Lists.newArrayList(text);
    }
    
    public static QueuedTooltip create(me.shedaniel.math.api.Point location, List<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(me.shedaniel.math.api.Point location, String... text) {
        return QueuedTooltip.create(location, Lists.newArrayList(text));
    }
    
    public static QueuedTooltip create(Point location, List<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(Point location, Collection<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(Point location, String... text) {
        return QueuedTooltip.create(location, Lists.newArrayList(text));
    }
    
    public static QueuedTooltip create(List<String> text) {
        return QueuedTooltip.create(PointHelper.ofMouse(), text);
    }
    
    public static QueuedTooltip create(Collection<String> text) {
        return QueuedTooltip.create(PointHelper.ofMouse(), text);
    }
    
    public static QueuedTooltip create(String... text) {
        return QueuedTooltip.create(PointHelper.ofMouse(), text);
    }
    
    /**
     * @return the absolute location of the tooltip.
     * @deprecated Use {@link #getX()} and {@link #getY()} instead.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public me.shedaniel.math.api.Point getLocation() {
        return new me.shedaniel.math.api.Point(location);
    }
    
    @Override
    public int getX() {
        return location.x;
    }
    
    @Override
    public int getY() {
        return location.y;
    }
    
    @Override
    public List<String> getText() {
        return text;
    }
    
    @Override
    public void queue() {
        Tooltip.super.queue();
    }
}
