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

import com.mojang.math.Matrix4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigGroups;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionGroup;
import me.shedaniel.rei.impl.client.gui.config.options.preview.TooltipPreview;
import net.minecraft.client.gui.GuiComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ConfigGroupWidget {
    private static final Map<OptionGroup, Pair<PreviewLocation, SpecialGroupConstructor>> SPECIAL_GROUPS = new HashMap<>();
    
    static {
        addPreview(AllREIConfigGroups.APPEARANCE_TOOLTIPS, PreviewLocation.RIGHT, (entry, width, height) -> TooltipPreview.create(width, height));
    }
    
    public static void addPreview(OptionGroup group, PreviewLocation location, SpecialGroupConstructor constructor) {
        SPECIAL_GROUPS.put(group, Pair.of(location, constructor));
    }
    
    public static WidgetWithBounds create(OptionGroup entry, int width) {
        WidgetWithBounds groupTitle = Widgets.createLabel(new Point(0, 3), entry.getGroupName().copy().withStyle(style -> style.withColor(0xFFC0C0C0).withUnderlined(true)))
                .leftAligned()
                .withPadding(0, 0, 0, 6);
        WidgetWithBounds contents;
        
        if (SPECIAL_GROUPS.containsKey(entry)) {
            Pair<PreviewLocation, SpecialGroupConstructor> pair = SPECIAL_GROUPS.get(entry);
            PreviewLocation location = pair.getLeft();
            int halfWidth = width * 6 / 10 - 2;
            if (halfWidth <= 200) location = PreviewLocation.TOP;
            
            if (location == PreviewLocation.RIGHT) {
                WidgetWithBounds original = _create(entry, halfWidth);
                Widget background = createBackgroundSlot(() -> new Rectangle(halfWidth + 2, 0, width - halfWidth - 4, original.getBounds().height));
                Widget right = Widgets.withTranslate(pair.getRight().create(entry, () -> width - halfWidth - 4, () -> original.getBounds().height), halfWidth + 2, 0, 0);
                contents = Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, original.getBounds().height), original, background, right);
            } else {
                WidgetWithBounds original = _create(entry, width);
                
                if (location == PreviewLocation.TOP) {
                    WidgetWithBounds widget = pair.getRight().create(entry, () -> width, null);
                    Widget background = createBackgroundSlot(widget::getBounds);
                    WidgetWithBounds translatedOriginal = Widgets.withTranslate(original, () -> Matrix4f.createTranslateMatrix(0, widget.getBounds().height + 4, 0));
                    contents = Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, widget.getBounds().height + 4 + translatedOriginal.getBounds().height), translatedOriginal, background, widget);
                } else {
                    WidgetWithBounds widget = pair.getRight().create(entry, () -> width, null);
                    Widget background = createBackgroundSlot(widget::getBounds);
                    contents = Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, original.getBounds().getMaxY() + 4 + widget.getBounds().height), original,
                            Widgets.withTranslate(Widgets.concat(background, widget), () -> Matrix4f.createTranslateMatrix(0, original.getBounds().getMaxY() + 4, 0)));
                }
            }
        } else {
            contents = _create(entry, width);
        }
        
        return Widgets.concatWithBounds(
                () -> new Rectangle(0, 0, width, groupTitle.getBounds().getMaxY() + contents.getBounds().height),
                groupTitle,
                Widgets.withTranslate(contents, () -> Matrix4f.createTranslateMatrix(0, groupTitle.getBounds().getMaxY(), 0))
        );
    }
    
    private static WidgetWithBounds _create(OptionGroup entry, int width) {
        List<Triple<Widget, Supplier<Rectangle>, Matrix4f[]>> widgets = new ArrayList<>();
        int[] height = {0};
        
        for (CompositeOption<?> option : entry.getOptions()) {
            Matrix4f[] translation = new Matrix4f[]{Matrix4f.createTranslateMatrix(0, height[0], 0)};
            WidgetWithBounds widget = Widgets.withTranslate(ConfigOptionWidget.create(option, width), () -> translation[0]);
            widgets.add(Triple.of(widget, () -> MatrixUtils.transform(translation[0], widget.getBounds()), translation));
            height[0] = Math.max(height[0], widget.getBounds().getMaxY());
            
            if (entry.getOptions().get(entry.getOptions().size() - 1) != option) {
                Matrix4f[] translationDrawable = new Matrix4f[]{Matrix4f.createTranslateMatrix(0, height[0], 0)};
                widgets.add(Triple.of(Widgets.withTranslate(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    for (int x = 0; x <= width; x += 4) {
                        GuiComponent.fill(matrices, Math.min(width, x), 1, Math.min(width, x + 2), 2, 0xFF757575);
                    }
                }), () -> translationDrawable[0]), () ->
                        MatrixUtils.transform(translationDrawable[0], new Rectangle(0, 0, 1, 7)), translationDrawable));
                height[0] += 7;
            }
        }
        
        widgets.add(Triple.of(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            int h = 0;
            for (Triple<Widget, Supplier<Rectangle>, Matrix4f[]> widget : widgets) {
                widget.getRight()[0] = Matrix4f.createTranslateMatrix(0, h, 0);
                h = Math.max(h, widget.getMiddle().get().getMaxY());
            }
            height[0] = h;
        }), Rectangle::new, new Matrix4f[]{new Matrix4f()}));
        
        return Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, height[0]), new AbstractList<>() {
            @Override
            public Widget get(int index) {
                return widgets.get(index).getLeft();
            }
            
            @Override
            public int size() {
                return widgets.size();
            }
        });
    }
    
    @FunctionalInterface
    public interface SpecialGroupConstructor {
        WidgetWithBounds create(OptionGroup entry, IntSupplier width, @Nullable IntSupplier height);
    }
    
    public enum PreviewLocation {
        RIGHT, TOP, BOTTOM
    }
    
    private static Widget createBackgroundSlot(Supplier<Rectangle> bounds) {
        return Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Rectangle rectangle = bounds.get();
            GuiComponent.fill(matrices, rectangle.x, rectangle.y, rectangle.getMaxX(), rectangle.getMaxY(), 0xFF333333);
            GuiComponent.fill(matrices, rectangle.x + 1, rectangle.y + 1, rectangle.getMaxX() - 1, rectangle.getMaxY() - 1, 0xFF000000);
        });
    }
}
