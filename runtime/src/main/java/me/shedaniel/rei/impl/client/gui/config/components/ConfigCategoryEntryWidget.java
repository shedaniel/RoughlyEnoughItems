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

package me.shedaniel.rei.impl.client.gui.config.components;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.config.options.OptionCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class ConfigCategoryEntryWidget {
    public static WidgetWithBounds create(OptionCategory category) {
        Label label = Widgets.createLabel(new Point(21, 7), category.getName().copy().withStyle(style -> style.withColor(0xFFC0C0C0)))
                .leftAligned();
        Font font = Minecraft.getInstance().font;
        Rectangle bounds = new Rectangle(0, 0, label.getBounds().getMaxX(), 7 * 3);
        return Widgets.concatWithBounds(
                bounds,
                label,
                Widgets.createTexturedWidget(category.getIcon(), new Rectangle(3, 3, 16, 16), 0, 0, 1, 1, 1, 1)
        );
    }
    
    public static WidgetWithBounds createTiny(OptionCategory category) {
        Rectangle bounds = new Rectangle(0, 0, 16, 16);
        return Widgets.withTooltip(
                Widgets.withBounds(Widgets.createTexturedWidget(category.getIcon(), bounds, 0, 0, 1, 1, 1, 1), bounds),
                category.getName()
        );
    }
}
