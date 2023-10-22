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
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionGroup;
import net.minecraft.client.gui.GuiComponent;

import java.util.ArrayList;
import java.util.List;

public class ConfigGroupWidget {
    public static WidgetWithBounds create(OptionGroup entry, int width) {
        List<Widget> widgets = new ArrayList<>();
        int height = 0;
        WidgetWithBounds groupTitle = Widgets.createLabel(new Point(0, 3), entry.getGroupName().copy().withStyle(style -> style.withColor(0xFFC0C0C0).withUnderlined(true)))
                .leftAligned();
        groupTitle = groupTitle.withPadding(0, 0, 0, 6);
        widgets.add(groupTitle);
        height = Math.max(height, groupTitle.getBounds().getMaxY());
        
        for (CompositeOption<?> option : entry.getOptions()) {
            WidgetWithBounds widget = ConfigOptionWidget.create(option, width);
            widgets.add(Widgets.withTranslate(widget, 0, height, 0));
            height = Math.max(height, height + widget.getBounds().getMaxY());
            
            if (entry.getOptions().get(entry.getOptions().size() - 1) != option) {
                int y = height;
                widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    for (int x = 0; x <= width; x += 4) {
                        GuiComponent.fill(matrices, x, y + 1, x + 2, y + 2, 0xFF757575);
                    }
                }));
                height += 7;
            }
        }
        
        Rectangle bounds = new Rectangle(0, 0, width, height);
        return Widgets.concatWithBounds(bounds, widgets);
    }
}
