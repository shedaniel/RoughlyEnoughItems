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

import dev.architectury.utils.value.IntValue;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.impl.client.gui.config.options.OptionCategory;
import me.shedaniel.rei.impl.client.gui.widget.ListWidget;
import me.shedaniel.rei.impl.client.gui.widget.ScrollableViewWidget;
import me.shedaniel.rei.impl.common.util.RectangleUtils;

import java.util.List;

public class ConfigCategoriesListWidget {
    public static Widget create(Rectangle bounds, List<OptionCategory> categories, IntValue selected) {
        WidgetWithBounds list = ListWidget.builderOf(RectangleUtils.inset(bounds, 3, 5), categories,
                        (index, entry) -> ConfigCategoryEntryWidget.create(entry))
                .gap(3)
                .isSelectable((index, entry) -> true)
                .selected(selected)
                .build();
        return ScrollableViewWidget.create(bounds, list.withPadding(0, 5), true);
    }
    
    public static Widget createTiny(Rectangle bounds, List<OptionCategory> categories, IntValue selected) {
        WidgetWithBounds list = ListWidget.builderOf(RectangleUtils.inset(bounds, (bounds.width - 6 - 16) / 2, 9), categories,
                        (index, entry) -> ConfigCategoryEntryWidget.createTiny(entry))
                .gap(7)
                .isSelectable((index, entry) -> true)
                .selected(selected)
                .build();
        return ScrollableViewWidget.create(bounds, list.withPadding(0, 9), true);
    }
}
