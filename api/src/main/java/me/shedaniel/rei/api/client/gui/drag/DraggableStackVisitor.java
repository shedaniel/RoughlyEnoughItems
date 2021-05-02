/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A visitor for accepting {@link DraggableStack} to the screen.
 */
public interface DraggableStackVisitor<T extends Screen> extends Comparable<DraggableStackVisitor<T>> {
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
            public Optional<Acceptor> visitDraggedStack(DraggingContext<T> context, DraggableStack stack) {
                for (DraggableStackVisitor<T> visitor : visitors.get()) {
                    if (visitor.isHandingScreen(context.getScreen())) {
                        Optional<Acceptor> acceptor = visitor.visitDraggedStack(context, stack);
                        if (acceptor.isPresent()) return acceptor;
                    }
                }
                return Optional.empty();
            }
        };
    }
    
    Optional<Acceptor> visitDraggedStack(DraggingContext<T> context, DraggableStack stack);
    
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
    default int compareTo(DraggableStackVisitor<T> o) {
        return Double.compare(getPriority(), o.getPriority());
    }
    
    @FunctionalInterface
    interface Acceptor {
        void accept(DraggableStack stack);
    }
}
