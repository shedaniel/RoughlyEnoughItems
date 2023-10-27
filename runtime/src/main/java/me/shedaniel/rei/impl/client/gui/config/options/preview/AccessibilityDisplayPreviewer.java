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

import com.google.common.base.MoreObjects;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigOptions;
import me.shedaniel.rei.impl.client.gui.widget.TabContainerWidget;
import me.shedaniel.rei.impl.client.gui.widget.TabWidget;
import net.minecraft.world.item.Items;

public class AccessibilityDisplayPreviewer {
    public static WidgetWithBounds create(ConfigAccess access, int width) {
        int[] selected = {0};
        TabWidget[] tabs = new TabWidget[4];
        Widget[] buttons = {null};
        return Widgets.concatWithBounds(() -> new Rectangle(width, 36 + 17),
                Widgets.delegate(() -> MoreObjects.firstNonNull(tabs[0], Widgets.noOp())),
                Widgets.delegate(() -> MoreObjects.firstNonNull(tabs[1], Widgets.noOp())),
                Widgets.delegate(() -> MoreObjects.firstNonNull(tabs[2], Widgets.noOp())),
                Widgets.delegate(() -> MoreObjects.firstNonNull(tabs[3], Widgets.noOp())),
                Widgets.scissored(new Rectangle(1, 1, width - 2, 34 + 17), Widgets.createCategoryBase(new Rectangle(width / 2 - 28 * 3 / 2 - 10, 30 + 17, 28 * 3 + 20, 28))),
                Widgets.delegate(() -> MoreObjects.firstNonNull(selected[0] < 4 ? tabs[selected[0]] : null, Widgets.noOp())),
                Widgets.delegate(() -> MoreObjects.firstNonNull(buttons[0], Widgets.noOp())),
                Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    boolean largerTabs = access.get(AllREIConfigOptions.LARGER_TABS);
                    boolean largerArrowButtons = access.get(AllREIConfigOptions.LARGER_ARROW_BUTTONS);
                    int tabSize = largerTabs ? 28 : 24;
                    for (int i = 0; i < 4; i++) {
                        tabs[i] = null;
                    }
                    for (int i = 0; i < (largerTabs ? 3 : 4); i++) {
                        int finalI = i;
                        tabs[i] = TabWidget.create(i, tabSize, width / 2 - tabSize * (largerTabs ? 3 : 4) / 2, 30 + 17, 0, !largerTabs ? 166 : 192, tabWidget -> {
                            selected[0] = finalI;
                            return true;
                        });
                        EntryStack<?> stack = i == 0 ? EntryStacks.of(Items.CRAFTING_TABLE) :
                                i == 1 ? EntryStacks.of(Items.FURNACE) :
                                        i == 2 ? EntryStacks.of(Items.SMOKER) :
                                                EntryStacks.of(Items.BLAST_FURNACE);
                        tabs[i].setRenderer(null, stack, null, selected[0] == i);
                    }
                    if (selected[0] >= (largerTabs ? 3 : 4)) selected[0] = 0;
                    
                    buttons[0] = Widgets.concat(TabContainerWidget.getCategoryButtons(new Rectangle(width / 2 - 28 * 3 / 2 - 10, 2 + 16, 28 * 3 + 20, 28),
                            !largerArrowButtons, tabSize, largerArrowButtons ? 16 : 10, () -> {
                            }, () -> {
                            }));
                })
        );
    }
}
