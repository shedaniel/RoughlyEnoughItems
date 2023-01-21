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

package me.shedaniel.rei.api.client.registry.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.TransferDisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
@FunctionalInterface
@Deprecated
@ApiStatus.ScheduledForRemoval
public interface TransferHandlerErrorRenderer {
    void render(PoseStack matrices, int mouseX, int mouseY, float delta, List<Widget> widgets, Rectangle bounds, Display display);
    
    @ApiStatus.Internal
    static TransferHandlerErrorRenderer forRedSlots(IntList redSlots) {
        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            DisplayCategory<?> category = Objects.requireNonNull(CategoryRegistry.getInstance().get(display.getCategoryIdentifier()))
                    .getCategory();
            if (category instanceof TransferDisplayCategory) {
                ((TransferDisplayCategory<Display>) category).renderRedSlots(matrices, widgets, bounds, display, redSlots);
            }
        };
    }
    
    default TransferHandlerRenderer asNew() {
        return this::render;
    }
}
