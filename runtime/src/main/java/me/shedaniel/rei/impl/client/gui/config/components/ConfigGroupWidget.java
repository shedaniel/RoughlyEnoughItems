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
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigGroups;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionGroup;
import me.shedaniel.rei.impl.client.gui.config.options.preview.AccessibilityDisplayPreviewer;
import me.shedaniel.rei.impl.client.gui.config.options.preview.InterfacePreviewer;
import me.shedaniel.rei.impl.client.gui.config.options.preview.TooltipPreviewer;
import me.shedaniel.rei.impl.client.gui.text.TextTransformations;
import net.minecraft.client.gui.GuiComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ConfigGroupWidget {
    private static final Map<String, Pair<PreviewLocation, SpecialGroupConstructor>> SPECIAL_GROUPS = new HashMap<>();
    
    static {
        addPreview(AllREIConfigGroups.APPEARANCE_INTERFACE, PreviewLocation.RIGHT, (access, entry, width, height) -> InterfacePreviewer.create(access, width, height));
        addPreview(AllREIConfigGroups.APPEARANCE_TOOLTIPS, PreviewLocation.RIGHT, (access, entry, width, height) -> TooltipPreviewer.create(access, width, height));
        addPreview(AllREIConfigGroups.ACCESSIBILITY_DISPLAY, PreviewLocation.BOTTOM, (access, entry, width, height) -> AccessibilityDisplayPreviewer.create(access, width));
    }
    
    public static void addPreview(OptionGroup group, PreviewLocation location, SpecialGroupConstructor constructor) {
        SPECIAL_GROUPS.put(group.getId(), Pair.of(location, constructor));
    }
    
    public static WidgetWithBounds create(ConfigAccess access, OptionGroup entry, int width, boolean applyPreview) {
        WidgetWithBounds groupTitle = Widgets.createLabel(new Point(0, 3), TextTransformations.highlightText(entry.getGroupName().copy(), entry.getGroupNameHighlight(), style -> style.withColor(0xFFC0C0C0).withUnderlined(true)))
                .leftAligned()
                .withPadding(0, 0, 0, 6);
        WidgetWithBounds contents;
        
        if (applyPreview && SPECIAL_GROUPS.containsKey(entry.getId())) {
            Pair<PreviewLocation, SpecialGroupConstructor> pair = SPECIAL_GROUPS.get(entry.getId());
            PreviewLocation location = pair.getLeft();
            int halfWidth = width * 6 / 10 - 2;
            if (halfWidth <= 200 && location == PreviewLocation.RIGHT) location = PreviewLocation.TOP;
            
            if (location == PreviewLocation.RIGHT) {
                WidgetWithBounds original = _create(access, entry, halfWidth);
                Widget background = createBackgroundSlot(() -> new Rectangle(halfWidth + 2, 0, width - halfWidth - 4, original.getBounds().height));
                Widget right = Widgets.withTranslate(pair.getRight().create(access, entry, width - halfWidth - 4, () -> original.getBounds().height), halfWidth + 2, 0, 0);
                contents = Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, original.getBounds().height), original, background, right);
            } else {
                WidgetWithBounds original = _create(access, entry, width);
                
                WidgetWithBounds widget = pair.getRight().create(access, entry, width, null);
                Widget background = createBackgroundSlot(widget::getBounds);
                
                if (location == PreviewLocation.TOP) {
                    WidgetWithBounds translatedOriginal = Widgets.withTranslate(original, () -> Matrix4f.createTranslateMatrix(0, widget.getBounds().height + 4, 0));
                    contents = Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, widget.getBounds().height + 4 + translatedOriginal.getBounds().height), translatedOriginal, background, widget);
                } else {
                    contents = Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, original.getBounds().getMaxY() + 2 + widget.getBounds().height), original,
                            Widgets.withTranslate(Widgets.concat(background, widget), () -> Matrix4f.createTranslateMatrix(0, original.getBounds().getMaxY() + 4, 0)));
                }
            }
        } else {
            contents = _create(access, entry, width);
        }
        
        return Widgets.concatWithBounds(
                () -> new Rectangle(0, 0, width, groupTitle.getBounds().getMaxY() + contents.getBounds().height),
                groupTitle,
                Widgets.withTranslate(contents, () -> Matrix4f.createTranslateMatrix(0, groupTitle.getBounds().getMaxY(), 0))
        );
    }
    
    private static WidgetWithBounds _create(ConfigAccess access, OptionGroup entry, int width) {
        List<WidgetComposite> widgets = new ArrayList<>();
        int[] height = {0};
        
        for (CompositeOption<?> option : entry.getOptions()) {
            widgets.add(WidgetComposite.of(ConfigOptionWidget.create(access, option, width)));
            
            if (entry.getOptions().get(entry.getOptions().size() - 1) != option) {
                Widget separator = Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                    for (int x = 0; x <= width; x += 4) {
                        GuiComponent.fill(matrices, Math.min(width, x), 1, Math.min(width, x + 2), 2, 0xFF757575);
                    }
                });
                widgets.add(WidgetComposite.of(Widgets.withBounds(separator, new Rectangle(0, 0, 1, 7))));
            }
        }
        
        widgets.add(WidgetComposite.ofNonAccounting(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            recalculateHeight(widgets, i -> height[0] = i);
        })));
        recalculateHeight(widgets, i -> height[0] = i);
        
        return Widgets.concatWithBounds(() -> new Rectangle(0, 0, width, height[0]), new AbstractList<>() {
            @Override
            public Widget get(int index) {
                return widgets.get(index).widget();
            }
            
            @Override
            public int size() {
                return widgets.size();
            }
        });
    }
    
    private record WidgetComposite(
            Widget widget,
            Supplier<Rectangle> bounds,
            Matrix4f translation
    ) {
        public static WidgetComposite of(WidgetWithBounds widget) {
            Matrix4f translation = new Matrix4f();
            return new WidgetComposite(Widgets.withTranslate(widget, translation),
                    () -> MatrixUtils.transform(translation, widget.getBounds()), translation);
        }
        
        public static WidgetComposite ofNonAccounting(Widget widget) {
            return new WidgetComposite(widget, Rectangle::new, new Matrix4f());
        }
    }
    
    private static void recalculateHeight(List<WidgetComposite> widgets, IntConsumer setHeight) {
        int height = 0;
        for (WidgetComposite widget : widgets) {
            widget.translation().load(Matrix4f.createTranslateMatrix(0, height, 0));
            height = Math.max(height, widget.bounds().get().getMaxY());
        }
        setHeight.accept(height);
    }
    
    @FunctionalInterface
    public interface SpecialGroupConstructor {
        WidgetWithBounds create(ConfigAccess access, OptionGroup entry, int width, @Nullable IntSupplier height);
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
