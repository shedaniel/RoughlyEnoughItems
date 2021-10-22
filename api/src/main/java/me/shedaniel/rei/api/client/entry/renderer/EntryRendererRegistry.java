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

package me.shedaniel.rei.api.client.entry.renderer;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Registry to transform {@link EntryRenderer} for stacks at a global level.
 * For specific stacks, you can use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setRenderer}
 */
@ApiStatus.Experimental
public interface EntryRendererRegistry extends Reloadable<REIClientPlugin> {
    static EntryRendererRegistry getInstance() {
        return PluginManager.getClientInstance().get(EntryRendererRegistry.class);
    }
    
    <T> void register(EntryType<T> type, EntryRendererProvider<T> provider);
    
    default <T> void transformTooltip(EntryType<T> type, TooltipTransformer<T> transformer) {
        register(type, (entry, last) -> {
            return new ForwardingEntryRenderer<T>(last) {
                @Override
                @Nullable
                public Tooltip getTooltip(EntryStack<T> entry, Point mouse) {
                    return transformer.transform(entry, mouse, super.getTooltip(entry, mouse));
                }
            };
        });
    }
    
    <T> EntryRenderer<T> get(EntryStack<T> stack);
    
    @FunctionalInterface
    interface TooltipTransformer<T> {
        @Nullable
        Tooltip transform(EntryStack<T> entry, Point mouse, @Nullable Tooltip tooltip);
    }
}
