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

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Function;

/**
 * An interface to be implemented on {@link me.shedaniel.rei.api.client.gui.widgets.Widget} to accept
 * incoming {@link DraggableStack}.
 */
@FunctionalInterface
public interface DraggableStackVisitorWidget {
    static DraggableStackVisitorWidget from(Function<DraggingContext<Screen>, Iterable<DraggableStackVisitorWidget>> providers) {
        return (context, stack) -> {
            for (DraggableStackVisitorWidget visitor : providers.apply(context)) {
                Optional<DraggableStackVisitor.Acceptor> acceptor = visitor.visitDraggedStack(context, stack);
                if (acceptor.isPresent()) return acceptor;
            }
            return Optional.empty();
        };
    }
    
    Optional<DraggableStackVisitor.Acceptor> visitDraggedStack(DraggingContext<Screen> context, DraggableStack stack);
    
    static DraggableStackVisitor<Screen> toVisitor(DraggableStackVisitorWidget widget) {
        return toVisitor(widget, 0.0);
    }
    
    static DraggableStackVisitor<Screen> toVisitor(DraggableStackVisitorWidget widget, double priority) {
        return new DraggableStackVisitor<Screen>() {
            @Override
            public Optional<Acceptor> visitDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
                return widget.visitDraggedStack(context, stack);
            }
            
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return true;
            }
            
            @Override
            public double getPriority() {
                return priority;
            }
        };
    }
}
