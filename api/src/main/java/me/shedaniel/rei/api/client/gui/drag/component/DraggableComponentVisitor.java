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

package me.shedaniel.rei.api.client.gui.drag.component;

import me.shedaniel.rei.api.client.gui.drag.DraggableBoundsProvider;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A visitor for accepting {@link DraggableComponent} to the screen.
 */
public interface DraggableComponentVisitor<T extends Screen> extends Comparable<DraggableComponentVisitor<T>> {
    static <T extends Screen> DraggableComponentVisitor<T> from(Supplier<? extends Iterable<? extends DraggableComponentVisitor<T>>> visitors) {
        return new DraggableComponentVisitor<T>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                for (DraggableComponentVisitor<T> visitor : visitors.get()) {
                    if (visitor.isHandingScreen(screen)) {
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public DraggedAcceptorResult acceptDragged(DraggingContext<T> context, DraggableComponent<?> component) {
                for (DraggableComponentVisitor<T> visitor : visitors.get()) {
                    if (visitor.isHandingScreen(context.getScreen())) {
                        DraggedAcceptorResult result = Objects.requireNonNull(visitor.acceptDragged(context, component));
                        if (result != DraggedAcceptorResult.PASS) return result;
                    }
                }
                return DraggedAcceptorResult.PASS;
            }
            
            @Override
            public Stream<DraggableBoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context, DraggableComponent<?> component) {
                return StreamSupport.stream(visitors.get().spliterator(), false)
                        .filter(visitor -> visitor.isHandingScreen(context.getScreen()))
                        .flatMap(visitor -> visitor.getDraggableAcceptingBounds(context, component));
            }
        };
    }
    
    /**
     * Accepts a dragged component, implementations of this function should check if the {@code context} is within
     * boundaries of the accepting boundaries.
     *
     * @param context   the context of the current dragged component on the overlay
     * @param component the component being dragged
     * @return the result of the visitor
     */
    default DraggedAcceptorResult acceptDragged(DraggingContext<T> context, DraggableComponent<?> component) {
        return DraggedAcceptorResult.PASS;
    }
    
    /**
     * Returns the accepting bounds for the dragging component, this should only be called once on drag.
     * The bounds are used to overlay to indicate to the users that the area is accepting entries.
     *
     * @param context   the context of the current dragged component on the overlay
     * @param component the component being dragged
     * @return the accepting bounds for the dragging component in a stream
     */
    default Stream<DraggableBoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context, DraggableComponent<?> component) {
        return Stream.empty();
    }
    
    <R extends Screen> boolean isHandingScreen(R screen);
    
    default DraggingContext<T> getContext() {
        return DraggingContext.getInstance().cast();
    }
    
    /**
     * Gets the priority of the handler, the higher the priority, the earlier this is called.
     *
     * @return the priority
     */
    default double getPriority() {
        return 0.0;
    }
    
    @Override
    default int compareTo(DraggableComponentVisitor<T> o) {
        return Double.compare(getPriority(), o.getPriority());
    }
}
