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

package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TooltipContextImpl implements TooltipContext {
    private final Point point;
    @Nullable
    private final TooltipFlag flag;
    private final boolean isSearch;
    
    public TooltipContextImpl(Point point, @Nullable TooltipFlag flag, boolean isSearch) {
        this.point = Objects.requireNonNull(point);
        this.flag = flag;
        this.isSearch = isSearch;
    }
    
    @Override
    public TooltipFlag getFlag() {
        return flag == null ? (Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL)
                : flag;
    }
    
    @Override
    public Point getPoint() {
        return point;
    }
    
    @Override
    public boolean isSearch() {
        return isSearch;
    }
}
