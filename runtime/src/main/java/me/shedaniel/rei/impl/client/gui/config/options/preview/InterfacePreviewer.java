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

import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.config.AppearanceTheme;
import me.shedaniel.rei.api.client.gui.config.RecipeBorderType;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigOptions;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.ArrowWidget;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.PanelWidget;
import net.minecraft.Util;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public class InterfacePreviewer {
    public static WidgetWithBounds create(ConfigAccess access, int width, @Nullable IntSupplier height) {
        WidgetWithBounds widget = _create(access, width);
        if (height == null) {
            Widget background = Widgets.createCategoryBase(new Rectangle(2, 2, width - 4, widget.getBounds().height - 4));
            return Widgets.concatWithBounds(widget::getBounds, background, widget);
        }
        Panel base = Widgets.createCategoryBase(new Rectangle(2, 2, width - 4, height.getAsInt() - 4));
        ((PanelWidget) base).setDarkBackgroundAlpha(ValueAnimator.ofFloat()
                .withConvention(() -> access.get(AllREIConfigOptions.THEME) == AppearanceTheme.DARK ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
                .asFloat());
        return Widgets.concatWithBounds(() -> new Rectangle(width, height.getAsInt()),
                Widgets.delegate(() -> {
                    base.getBounds().setBounds(2, 2, width - 4, height.getAsInt() - 4);
                    return base;
                }),
                Widgets.withTranslate(widget, () -> new Matrix4f().translate(0, (height.getAsInt() - widget.getBounds().height) / 2, 0))
        );
    }
    
    private static WidgetWithBounds _create(ConfigAccess access, int width) {
        List<Widget> widgets = new ArrayList<>();
        int displayWidth = 124, displayHeight = 66;
        Rectangle displayBounds = new Rectangle((width - displayWidth) / 2, (80 - displayHeight) / 2, displayWidth, displayHeight);
        NumberAnimator<Float> themeAlpha = ValueAnimator.ofFloat()
                .withConvention(() -> access.get(AllREIConfigOptions.THEME) == AppearanceTheme.DARK ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
                .asFloat();
        widgets.add(Util.make(Widgets.createRecipeBase(displayBounds), panel -> {
            ((PanelWidget) panel).setDarkBackgroundAlpha(themeAlpha);
        }).rendering(panel -> {
            RecipeBorderType type = access.get(AllREIConfigOptions.RECIPE_BORDER);
            panel.yTextureOffset(type.getYOffset());
            return type.isRendering();
        }));
        Point startingPoint = new Point(displayBounds.x + 6, displayBounds.y + 6);
        widgets.add(Util.make(Widgets.createArrow(new Point(startingPoint.x + 58, startingPoint.y + 18)), arrow -> {
            ((ArrowWidget) arrow).setDarkBackgroundAlpha(themeAlpha);
        }));
        widgets.add(Util.make(Widgets.createResultSlotBackground(new Point(startingPoint.x + 91, startingPoint.y + 19)), panel -> {
            ((PanelWidget) panel).setDarkBackgroundAlpha(themeAlpha);
        }));
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                widgets.add(Util.make(Widgets.createSlot(new Point(startingPoint.x + 1 + x * 18, startingPoint.y + 1 + y * 18))
                        .notInteractable()
                        .entries(x == 1 && y == 1 ? List.of() : EntryIngredients.of(Items.COBBLESTONE)), slot -> {
                    ((EntryWidget) slot).setDarkBackgroundAlpha(themeAlpha);
                }));
        widgets.add(Util.make(Widgets.createSlot(new Point(startingPoint.x + 91, startingPoint.y + 19)).disableBackground()
                .notInteractable()
                .entry(EntryStacks.of(Items.FURNACE)), slot -> {
            ((EntryWidget) slot).setDarkBackgroundAlpha(themeAlpha);
        }));
        return Widgets.concatWithBounds(new Rectangle(width, 80), widgets);
    }
}
