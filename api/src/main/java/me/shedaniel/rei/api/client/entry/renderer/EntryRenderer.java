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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A renderer to render a {@link EntryStack}.
 * Use {@link me.shedaniel.rei.api.client.util.ClientEntryStacks#setRenderer} to change the {@link EntryRenderer} for a {@link EntryStack}.
 *
 * @param <T> the entry type
 * @see BatchedEntryRenderer
 * @see EntryRendererRegistry
 */
@Environment(EnvType.CLIENT)
public interface EntryRenderer<T> extends EntryRendererProvider<T> {
    static <T> EntryRenderer<T> empty() {
        return ClientInternals.getEmptyEntryRenderer();
    }
    
    @Environment(EnvType.CLIENT)
    void render(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    @Nullable
    @Environment(EnvType.CLIENT)
    Tooltip getTooltip(EntryStack<T> entry, Point mouse);
    
    @ApiStatus.NonExtendable
    default <O> EntryRenderer<O> cast() {
        return (EntryRenderer<O>) this;
    }
    
    @Override
    default EntryRenderer<T> provide(EntryStack<T> entry, EntryRenderer<T> last) {
        return this;
    }
}