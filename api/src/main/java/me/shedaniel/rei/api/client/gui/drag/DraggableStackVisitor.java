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

package me.shedaniel.rei.api.client.gui.drag;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitor;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A visitor for accepting {@link DraggableStack} to the screen.
 */
public interface DraggableStackVisitor<T extends Screen> extends DraggableComponentVisitor<T> {
    static <T extends Screen> DraggableStackVisitor<T> from(Supplier<Iterable<DraggableStackVisitor<T>>> visitors) {
        return new DraggableStackVisitor<T>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                for (DraggableStackVisitor<T> visitor : visitors.get()) {
                    if (visitor.isHandingScreen(screen)) {
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public DraggedAcceptorResult acceptDraggedStack(DraggingContext<T> context, DraggableStack stack) {
                for (DraggableStackVisitor<T> visitor : visitors.get()) {
                    if (visitor.isHandingScreen(context.getScreen())) {
                        DraggedAcceptorResult result = Objects.requireNonNull(visitor.acceptDraggedStack(context, stack));
                        if (result != DraggedAcceptorResult.PASS) return result;
                    }
                }
                return DraggedAcceptorResult.PASS;
            }
            
            @Override
            public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context, DraggableStack stack) {
                return StreamSupport.stream(visitors.get().spliterator(), false)
                        .filter(visitor -> visitor.isHandingScreen(context.getScreen()))
                        .flatMap(visitor -> visitor.getDraggableAcceptingBounds(context, stack));
            }
        };
    }
    
    /**
     * Accepts a dragged stack, implementations of this function should check if the {@code context} is within
     * boundaries of the accepting boundaries.
     *
     * @param context the context of the current dragged stack on the overlay
     * @param stack   the stack being dragged
     * @return the result of the visitor
     */
    default DraggedAcceptorResult acceptDraggedStack(DraggingContext<T> context, DraggableStack stack) {
        return DraggedAcceptorResult.PASS;
    }
    
    @Override
    default DraggedAcceptorResult acceptDragged(DraggingContext<T> context, DraggableComponent<?> component) {
        return component.<EntryStack<?>>getIf()
                .map(comp -> acceptDraggedStack(context, DraggableStack.from(comp)))
                .orElse(DraggedAcceptorResult.PASS);
    }
    
    /**
     * Returns the accepting bounds for the dragging stack, this should only be called once on drag.
     * The bounds are used to overlay to indicate to the users that the area is accepting entries.
     *
     * @param context the context of the current dragged stack on the overlay
     * @param stack   the stack being dragged
     * @return the accepting bounds for the dragging stack in a stream
     */
    default Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context, DraggableStack stack) {
        return Stream.empty();
    }
    
    @Override
    default Stream<DraggableBoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context, DraggableComponent<?> component) {
        return component.<EntryStack<?>>getIf()
                .map(comp -> getDraggableAcceptingBounds(context, DraggableStack.from(comp)))
                .orElse(Stream.empty())
                .map(Function.identity());
    }
    
    @Override
    <R extends Screen> boolean isHandingScreen(R screen);
    
    @Override
    default DraggingContext<T> getContext() {
        return DraggingContext.getInstance().cast();
    }
    
    /**
     * Gets the priority of the handler, the higher the priority, the earlier this is called.
     *
     * @return the priority
     */
    @Override
    default double getPriority() {
        return 0.0;
    }
    
    default int compareTo(DraggableStackVisitor<T> o) {
        return Double.compare(getPriority(), o.getPriority());
    }
    
    @FunctionalInterface
    interface Acceptor {
        void accept(DraggableStack stack);
    }
    
    @FunctionalInterface
    interface BoundsProvider extends DraggableBoundsProvider {
        static VoxelShape fromRectangle(Rectangle bounds) {
            return DraggableBoundsProvider.fromRectangle(bounds);
        }
        
        static BoundsProvider ofRectangle(Rectangle bounds) {
            return DraggableBoundsProvider.ofRectangle(bounds)::bounds;
        }
        
        static BoundsProvider ofRectangles(Iterable<Rectangle> bounds) {
            return DraggableBoundsProvider.ofRectangles(bounds)::bounds;
        }
        
        static BoundsProvider ofShape(VoxelShape shape) {
            return DraggableBoundsProvider.ofShape(shape)::bounds;
        }
        
        static BoundsProvider ofShapes(Iterable<VoxelShape> shapes) {
            return DraggableBoundsProvider.ofShapes(shapes)::bounds;
        }
        
        static BoundsProvider empty() {
            return Shapes::empty;
        }
        
        static BoundsProvider concat(Iterable<BoundsProvider> providers) {
            return DraggableBoundsProvider.concat((List<DraggableBoundsProvider>)
                    (List<? extends DraggableBoundsProvider>) providers)::bounds;
        }
        
        @Override
        VoxelShape bounds();
    }
}
