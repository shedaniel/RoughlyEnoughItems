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

package me.shedaniel.rei.api.client.util;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.entry.type.BuiltinClientEntryTypes;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ClientEntryStacks {
    private ClientEntryStacks() {}
    
    public static EntryStack<?> of(Renderer renderer) {
        if (renderer instanceof EntryStack<?> stack) {
            return stack;
        }
        
        return EntryStack.of(BuiltinClientEntryTypes.RENDERING, renderer);
    }
    
    public static <T> EntryStack<T> setNotRenderer(EntryStack<? extends T> stack) {
        return setRenderer(stack, EntryRenderer.empty());
    }
    
    public static <T> EntryStack<T> setRenderer(EntryStack<? extends T> stack, EntryRenderer<? extends T> renderer) {
        return stack.setting(EntryStack.Settings.RENDERER, s -> renderer).cast();
    }
    
    @SuppressWarnings("rawtypes")
    public static <T> EntryStack<T> setRenderer(EntryStack<? extends T> stack, Function<EntryStack<T>, EntryRenderer<? extends T>> rendererProvider) {
        return stack.setting(EntryStack.Settings.RENDERER, (Function) rendererProvider).cast();
    }
    
    @SuppressWarnings("rawtypes")
    public static <T> EntryStack<T> setTooltipProcessor(EntryStack<? extends T> stack, BiFunction<EntryStack<T>, Tooltip, Tooltip> processor) {
        return stack.setting(EntryStack.Settings.TOOLTIP_PROCESSOR, (BiFunction) processor).cast();
    }
    
    public static EntryStack<FluidStack> setFluidRenderRatio(EntryStack<FluidStack> stack, float ratio) {
        return stack.setting(EntryStack.Settings.FLUID_RENDER_RATIO, ratio);
    }
}
