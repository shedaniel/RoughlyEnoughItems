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

import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A provider for supplying {@link DraggableStack} to the screen.
 */
public interface DraggableStackProvider<T extends Screen> extends DraggableComponentProvider<T, EntryStack<?>> {
    static <T extends Screen> DraggableStackProvider<T> from(Supplier<Iterable<DraggableStackProvider<T>>> providers) {
        return new DraggableStackProvider<T>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                for (DraggableStackProvider<T> provider : providers.get()) {
                    if (provider.isHandingScreen(screen)) {
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            @Nullable
            public DraggableStack getHoveredStack(DraggingContext<T> context, double mouseX, double mouseY) {
                for (DraggableStackProvider<T> provider : providers.get()) {
                    if (provider.isHandingScreen(context.getScreen())) {
                        DraggableStack stack = provider.getHoveredStack(context, mouseX, mouseY);
                        if (stack != null) return stack;
                    }
                }
                return null;
            }
        };
    }
    
    @Nullable
    DraggableStack getHoveredStack(DraggingContext<T> context, double mouseX, double mouseY);
    
    @Override
    @Nullable
    default DraggableComponent<EntryStack<?>> getHovered(DraggingContext<T> context, double mouseX, double mouseY) {
        return getHoveredStack(context, mouseX, mouseY);
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
        return 0f;
    }
    
    default int compareTo(DraggableStackProvider<T> o) {
        return Double.compare(getPriority(), o.getPriority());
    }
}