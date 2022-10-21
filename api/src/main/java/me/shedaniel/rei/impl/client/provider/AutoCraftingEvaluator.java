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

package me.shedaniel.rei.impl.client.provider;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface AutoCraftingEvaluator {
    Builder builder(Display display);
    
    interface Builder {
        Builder actuallyCraft();
    
        default Builder actuallyCraft(boolean build) {
            if (build) {
                return actuallyCraft();
            } else {
                return this;
            }
        }
        
        Builder stacked();
    
        default Builder stacked(boolean build) {
            if (build) {
                return stacked();
            } else {
                return this;
            }
        }
        
        Builder ids(@Nullable Collection<ResourceLocation> ids);
        
        Builder buildRenderer();
        
        default Builder buildRenderer(boolean build) {
            if (build) {
                return buildRenderer();
            } else {
                return this;
            }
        }
        
        Builder buildTooltipRenderer();
        
        default Builder buildTooltipRenderer(boolean build) {
            if (build) {
                return buildTooltipRenderer();
            } else {
                return this;
            }
        }
        
        Result get();
    }
    
    interface Result {
        int getTint();
        
        boolean isSuccessful();
    
        @Nullable
        TransferHandler getSuccessfulHandler();
        
        boolean isApplicable();
        
        @Nullable
        TransferHandlerRenderer getRenderer();
    
        @Nullable
        BiConsumer<Point, Consumer<Tooltip>> getTooltipRenderer();
    }
}
