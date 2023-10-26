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

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.impl.client.gui.config.options.OptionGroup;
import me.shedaniel.rei.impl.client.gui.widget.ListWidget;
import me.shedaniel.rei.impl.client.gui.widget.ScrollableViewWidget;
import me.shedaniel.rei.impl.common.util.RectangleUtils;

import java.util.List;

public class ConfigEntriesListWidget {
    public static Widget create(Rectangle bounds, List<OptionGroup> groups) {
        WidgetWithBounds list = ListWidget.builderOf(RectangleUtils.inset(bounds, 6, 6), groups,
                        (index, entry) -> ConfigGroupWidget.create(entry, bounds.width - 12 - 6))
                .gap(7)
                .calculateTotalHeightDynamically(true)
                .build();
        return ScrollableViewWidget.create(bounds, list.withPadding(0, 5), true);
    }
}
