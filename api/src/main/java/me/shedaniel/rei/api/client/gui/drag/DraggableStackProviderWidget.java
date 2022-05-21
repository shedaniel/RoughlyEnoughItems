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

package me.shedaniel.rei.api.client.gui.drag;

import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An interface to be implemented on {@link me.shedaniel.rei.api.client.gui.widgets.Widget} to provide
 * {@link DraggableStack}.
 */
@FunctionalInterface
public interface DraggableStackProviderWidget extends DraggableComponentProviderWidget<EntryStack<?>> {
    static DraggableStackProviderWidget from(Function<DraggingContext<Screen>, Iterable<DraggableStackProviderWidget>> providers) {
        return (context, mouseX, mouseY) -> {
            for (DraggableStackProviderWidget provider : providers.apply(context)) {
                DraggableStack stack = provider.getHoveredStack(context, mouseX, mouseY);
                if (stack != null) return stack;
            }
            return null;
        };
    }
    
    @Nullable
    DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY);
    
    @Override
    @Nullable
    default DraggableComponent<EntryStack<?>> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        return getHoveredStack(context, mouseX, mouseY);
    }
    
    static DraggableStackProvider<Screen> toProvider(DraggableStackProviderWidget widget) {
        return toProvider(widget, 0D);
    }
    
    static DraggableStackProvider<Screen> toProvider(DraggableStackProviderWidget widget, double priority) {
        return new DraggableStackProvider<Screen>() {
            @Override
            @Nullable
            public DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY) {
                return widget.getHoveredStack(context, mouseX, mouseY);
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
