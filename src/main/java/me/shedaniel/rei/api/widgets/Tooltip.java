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

package me.shedaniel.rei.api.widgets;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Tooltip {
    @SuppressWarnings("deprecation")
    @NotNull
    static Tooltip create(Point point, Collection<String> texts) {
        return QueuedTooltip.create(point, texts);
    }
    
    @SuppressWarnings("deprecation")
    @NotNull
    static Tooltip create(Point point, String... texts) {
        return QueuedTooltip.create(point, texts);
    }
    
    @SuppressWarnings("deprecation")
    @NotNull
    static Tooltip create(Collection<String> texts) {
        return QueuedTooltip.create(texts);
    }
    
    @SuppressWarnings("deprecation")
    @NotNull
    static Tooltip create(String... texts) {
        return QueuedTooltip.create(texts);
    }
    
    int getX();
    
    int getY();
    
    List<String> getText();
    
    default void queue() {
        REIHelper.getInstance().queueTooltip(this);
    }
}
