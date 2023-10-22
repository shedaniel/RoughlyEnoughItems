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

package me.shedaniel.rei.impl.client.gui.config.options.preview;

import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.config.AppearanceTheme;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.config.options.ConfigPreviewer;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.PanelWidget;

import java.util.List;
import java.util.function.Supplier;

public enum ThemePreviewer implements ConfigPreviewer<AppearanceTheme> {
    INSTANCE;
    
    @Override
    public WidgetWithBounds preview(int width, Supplier<AppearanceTheme> value) {
        Panel base = Widgets.createCategoryBase(new Rectangle(width * 10 / 2, 3, width * 10 / 8, 20));
        ((PanelWidget) base).setDarkBackgroundAlpha(ValueAnimator.ofFloat()
                .withConvention(() -> value.get() == AppearanceTheme.DARK ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
                .asFloat());
        return Widgets.concatWithBounds(new Rectangle(0, 0, width, 26), List.of(base));
    }
}
