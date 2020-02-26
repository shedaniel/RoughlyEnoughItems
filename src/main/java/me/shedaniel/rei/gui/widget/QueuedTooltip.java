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
import me.shedaniel.math.api.Point;
import me.shedaniel.math.impl.PointHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;

public class QueuedTooltip {
    
    private Point location;
    private List<String> text;
    private Consumer<QueuedTooltip> consumer = null;
    
    private QueuedTooltip(Point location, List<String> text) {
        this.location = location;
        this.text = Lists.newArrayList(text);
    }
    
    public static QueuedTooltip create(Point location, List<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(Point location, String... text) {
        return QueuedTooltip.create(location, Lists.newArrayList(text));
    }
    
    public static QueuedTooltip create(List<String> text) {
        return QueuedTooltip.create(PointHelper.fromMouse(), text);
    }
    
    public static QueuedTooltip create(String... text) {
        return QueuedTooltip.create(PointHelper.fromMouse(), text);
    }
    
    @ApiStatus.Internal
    public QueuedTooltip setSpecialRenderer(Consumer<QueuedTooltip> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    @ApiStatus.Internal
    public Consumer<QueuedTooltip> getConsumer() {
        return consumer;
    }
    
    public Point getLocation() {
        return location;
    }
    
    public int getX() {
        return getLocation().x;
    }
    
    public int getY() {
        return getLocation().y;
    }
    
    public List<String> getText() {
        return text;
    }
    
}
