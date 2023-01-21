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

package me.shedaniel.rei.api.client.registry.display;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

/**
 * A view that displays a {@link Display}.
 * <p>
 * This view can be modified externally with {@link me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider}.
 *
 * @param <T> the type of display
 */
public interface DisplayCategoryView<T extends Display> {
    /**
     * Returns the recipe renderer for the display, used in composite display viewing screen.
     *
     * @param display the display to render
     * @return the display renderer
     */
    @ApiStatus.OverrideOnly
    DisplayRenderer getDisplayRenderer(T display);
    
    /**
     * Returns the list of widgets for displaying the display.
     * <p>
     * For consistency, the widgets list should start with a {@link Widgets#createRecipeBase(Rectangle)} widget.
     * <p>
     * Slots may be added with {@link Widgets#createSlot(Point)}, the content of the slot
     * can be set with either {@link Slot#entry(EntryStack)} or {@link Slot#entries(Collection)}.
     * <p>
     * To configure the tooltips of the slots, you may take a look at {@link EntryStack#getTooltip(TooltipContext, boolean)}
     * for how the tooltip is resolved.
     * <p>
     * It is recommended to mark these slots for I/O using {@link Slot#markInput()} and {@link Slot#markOutput()},
     * and the background of the slots may be disabled using {@link Slot#disableBackground()}.
     * <p>
     * Arbitrary text may be added to the widgets list with {@link Widgets#createLabel(Point, Component)},
     * you may configure the horizontal alignment of the text using {@link Label#centered()},
     * {@link Label#leftAligned()} and {@link Label#rightAligned()}.<br>
     * It is recommended to remove the shadow of the label with {@link Label#noShadow()}, and
     * set the color of the label to {@code 0xFF404040} under light mode and {@code 0xFFBBBBBB} under dark mode,
     * you may use {@link Label#color(int, int)} for setting the label color depending on the color theme.
     * <p>
     * Lastly, {@link Widgets} contains many methods for the most common widgets,
     * such as adding tooltips, creating texture rectangles, arrows, burning fire, buttons, etc.
     * <p>
     * You may use {@link Widgets#createDrawableWidget(DrawableConsumer)} for rendering anything you want, or
     * wrap vanilla widgets using {@link Widgets#wrapVanillaWidget(GuiEventListener)}.
     *
     * @param display the display
     * @param bounds  the bounds of the display, configurable with overriding the width, height methods.
     * @return the list of widgets
     */
    @ApiStatus.OverrideOnly
    List<Widget> setupDisplay(T display, Rectangle bounds);
}
