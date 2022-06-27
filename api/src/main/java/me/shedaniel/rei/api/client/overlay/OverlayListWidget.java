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

package me.shedaniel.rei.api.client.overlay;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public interface OverlayListWidget {
    /**
     * Returns the mouse hovered stack within the overlay list widget.
     *
     * @return the mouse hovered stack, returns {@link EntryStack#empty()} if none is hovered
     */
    EntryStack<?> getFocusedStack();
    
    /**
     * Returns the currently visible stacks in the overlay list widget.
     *
     * @return the currently visible stacks
     */
    Stream<EntryStack<?>> getEntries();
    
    /**
     * Returns whether the mouse is within the overlay list widget,
     * accounting for excluded areas.
     *
     * @param point the mouse position
     * @return whether the mouse is within the overlay list widget
     */
    boolean containsMouse(Point point);
}
