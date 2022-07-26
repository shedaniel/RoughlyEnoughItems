/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Label extends WidgetWithBounds {
    public static final int LEFT_ALIGNED = -1;
    public static final int CENTER = 0;
    public static final int RIGHT_ALIGNED = 1;
    
    /**
     * @return whether the label is clickable, ignores if onClick is set.
     */
    public abstract boolean isClickable();
    
    /**
     * Sets whether the label is clickable, ignores if onClick is set.
     *
     * @param clickable whether the label is clickable.
     */
    public abstract void setClickable(boolean clickable);
    
    /**
     * Sets the label as clickable, ignores if onClick is set.
     *
     * @return the label itself.
     */
    public final Label clickable() {
        return clickable(true);
    }
    
    /**
     * Sets whether the label is clickable, ignores if onClick is set.
     *
     * @param clickable whether the label is clickable.
     * @return the label itself.
     */
    public final Label clickable(boolean clickable) {
        setClickable(clickable);
        return this;
    }
    
    /**
     * @return the consumer on click, only applicable if the label is clickable, null if not set.
     */
    @Nullable
    public abstract Consumer<Label> getOnClick();
    
    /**
     * Sets the on click consumer, only applicable if the label is clickable.
     *
     * @param onClick the on click consumer, only applicable if the label is clickable.
     */
    public abstract void setOnClick(@Nullable Consumer<Label> onClick);
    
    /**
     * Sets the on click consumer, only applicable if the label is clickable.
     *
     * @param onClick the on click consumer, only applicable if the label is clickable.
     * @return the label itself.
     */
    public final Label onClick(@Nullable Consumer<Label> onClick) {
        setOnClick(onClick);
        return this;
    }
    
    /**
     * @return the consumer before render, null if not set.
     */
    @Nullable
    public abstract BiConsumer<PoseStack, Label> getOnRender();
    
    /**
     * Sets the consumer before render.
     *
     * @param onRender the consumer before render.
     */
    public abstract void setOnRender(@Nullable BiConsumer<PoseStack, Label> onRender);
    
    /**
     * Sets the consumer before render.
     *
     * @param onRender the consumer before render.
     * @return the label itself.
     */
    public final Label onRender(@Nullable BiConsumer<PoseStack, Label> onRender) {
        setOnRender(onRender);
        return this;
    }
    
    /**
     * @return whether the label is focusable by pressing tab, ignored if not clickable.
     */
    public abstract boolean isFocusable();
    
    /**
     * Sets whether the label is focusable by pressing tab, ignored if not clickable.
     *
     * @param focusable whether the label is focusable by pressing tab, ignored if not clickable.
     */
    public abstract void setFocusable(boolean focusable);
    
    /**
     * Sets whether the label is focusable by pressing tab, ignored if not clickable.
     *
     * @param focusable whether the label is focusable by pressing tab, ignored if not clickable.
     * @return the label itself.
     */
    public final Label focusable(boolean focusable) {
        setFocusable(focusable);
        return this;
    }
    
    /**
     * @return the tooltip from the current tooltip function, null if no tooltip.
     */
    @Nullable
    public abstract Component[] getTooltipLines();
    
    /**
     * Sets the tooltip function used to get the tooltip.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     */
    public abstract void setTooltipFunction(@Nullable Function<Label, @Nullable Component[]> tooltip);
    
    /**
     * Sets the tooltip.
     *
     * @param tooltip the lines of tooltip.
     */
    public void setTooltip(Component... tooltip) {
        setTooltipFunction((label) -> tooltip);
    }
    
    /**
     * Sets the tooltip.
     *
     * @param tooltip the lines of tooltip.
     * @return the label itself.
     */
    public final Label tooltip(Component... tooltip) {
        return tooltipFunction(label -> tooltip);
    }
    
    /**
     * Sets the tooltip function.
     *
     * @param tooltip the tooltip function used to get the tooltip.
     * @return the label itself.
     */
    public final Label tooltipFunction(@Nullable Function<Label, @Nullable Component[]> tooltip) {
        setTooltipFunction(tooltip);
        return this;
    }
    
    /**
     * Gets the horizontal alignment of the label, defaulted as centered.
     *
     * @return {@link Label#LEFT_ALIGNED} if left aligned, {@link Label#CENTER} if centered or {@link Label#RIGHT_ALIGNED} if right aligned}.
     */
    public abstract int getHorizontalAlignment();
    
    public final Label centered() {
        return horizontalAlignment(CENTER);
    }
    
    public final Label leftAligned() {
        return horizontalAlignment(LEFT_ALIGNED);
    }
    
    public final Label rightAligned() {
        return horizontalAlignment(RIGHT_ALIGNED);
    }
    
    public abstract void setHorizontalAlignment(int horizontalAlignment);
    
    public final Label horizontalAlignment(int horizontalAlignment) {
        setHorizontalAlignment(horizontalAlignment);
        return this;
    }
    
    public abstract boolean hasShadow();
    
    public final Label noShadow() {
        return shadow(false);
    }
    
    public final Label shadow() {
        return shadow(true);
    }
    
    public abstract void setShadow(boolean hasShadow);
    
    public final Label shadow(boolean hasShadow) {
        setShadow(hasShadow);
        return this;
    }
    
    public abstract int getColor();
    
    public abstract void setColor(int color);
    
    public Label color(int lightModeColor, int darkModeColor) {
        return color(REIRuntime.getInstance().isDarkThemeEnabled() ? darkModeColor : lightModeColor);
    }
    
    public final Label color(int color) {
        setColor(color);
        return this;
    }
    
    public abstract int getHoveredColor();
    
    public abstract void setHoveredColor(int hoveredColor);
    
    public final Label hoveredColor(int lightModeColor, int darkModeColor) {
        return hoveredColor(REIRuntime.getInstance().isDarkThemeEnabled() ? darkModeColor : lightModeColor);
    }
    
    public final Label hoveredColor(int color) {
        setHoveredColor(color);
        return this;
    }
    
    public abstract Point getPoint();
    
    public final int getX() {
        return getPoint().getX();
    }
    
    public final int getY() {
        return getPoint().getY();
    }
    
    public abstract void setPoint(Point point);
    
    public final Label point(Point point) {
        setPoint(point);
        return this;
    }
    
    public abstract FormattedText getMessage();
    
    public abstract void setMessage(FormattedText message);
    
    public abstract void setRainbow(boolean rainbow);
    
    public final Label message(FormattedText message) {
        setMessage(message);
        return this;
    }
    
    public final Label rainbow(boolean rainbow) {
        setRainbow(rainbow);
        return this;
    }
}
