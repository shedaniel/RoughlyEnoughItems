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

package me.shedaniel.rei.api.client.gui.drag;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The context of the current dragged stack on the overlay.
 * <p>
 * Widgets should implement {@link DraggableStackProvider} to submit applicable stacks to drag.
 * Widgets should implement {@link DraggableStackVisitor} to accept incoming dragged stacks.
 */
public interface DraggingContext {
    static DraggingContext getInstance() {
        return REIHelper.getInstance().getOverlay().get().getDraggingContext();
    }
    
    default boolean isDraggingStack() {
        return getCurrentStack() != null;
    }
    
    /**
     * Returns the current dragged stack, may be null.
     *
     * @return the current dragged stack, may be null
     */
    @Nullable
    DraggableStack getCurrentStack();
    
    /**
     * Returns the current position of the dragged stack, this is usually the position of the mouse pointer,
     * but you should use this regardless to account for future changes.
     *
     * @return the current position of the dragged stack
     */
    @Nullable
    Point getCurrentPosition();
    
    /**
     * Renders the draggable stack back to the position {@code position}.
     * This may be used to animate an unaccepted draggable stack returning to its initial position.
     *
     * @param stack           the stack to use for render
     * @param initialPosition the initial position of the stack
     * @param position        the position supplier of the destination
     */
    void renderBackToPosition(DraggableStack stack, Point initialPosition, Supplier<Point> position);
}
