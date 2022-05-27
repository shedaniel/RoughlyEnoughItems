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

package me.shedaniel.rei.api.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.jetbrains.annotations.Nullable;

public interface Renderer {
    @Environment(EnvType.CLIENT)
    void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    @Nullable
    @Environment(EnvType.CLIENT)
    default Tooltip getTooltip(TooltipContext context) {
        return null;
    }
    
    @Environment(EnvType.CLIENT)
    int getZ();
    
    @Environment(EnvType.CLIENT)
    void setZ(int z);
    
    default void fillCrashReport(CrashReport report, CrashReportCategory category) {
        category.setDetail("Renderer name", () -> getClass().getCanonicalName());
        category.setDetail("Z level", () -> String.valueOf(getZ()));
        if (this instanceof WidgetWithBounds widget) {
            category.setDetail("Bounds", () -> String.valueOf(widget.getBounds()));
        }
    }
}
