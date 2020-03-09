/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.api.widgets;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Label extends WidgetWithBounds {
    public static final int LEFT_ALIGNED = -1;
    public static final int CENTER = 0;
    public static final int RIGHT_ALIGNED = 1;
    
    public abstract boolean isClickable();
    
    public abstract void setClickable(boolean clickable);
    
    public final Label clickable() {
        return clickable(true);
    }
    
    public final Label clickable(boolean clickable) {
        setClickable(clickable);
        return this;
    }
    
    @Nullable
    public abstract Consumer<Label> getOnClick();
    
    public abstract void setOnClick(@Nullable Consumer<Label> onClick);
    
    public final Label onClick(@Nullable Consumer<Label> onClick) {
        setOnClick(onClick);
        return this;
    }
    
    @Nullable
    public abstract Consumer<Label> getOnRender();
    
    public abstract void setOnRender(@Nullable Consumer<Label> onRender);
    
    public final Label onRender(@Nullable Consumer<Label> onRender) {
        setOnRender(onRender);
        return this;
    }
    
    public abstract boolean isFocusable();
    
    public abstract void setFocusable(boolean focusable);
    
    public final Label focusable(boolean focusable) {
        setFocusable(focusable);
        return this;
    }
    
    @Nullable
    public abstract String getTooltip();
    
    public abstract void setTooltip(@Nullable Function<Label, @Nullable String> tooltip);
    
    public final Label tooltipLines(@NotNull String... tooltip) {
        return tooltipLine(String.join("\n", tooltip));
    }
    
    public final Label tooltipLine(@Nullable String tooltip) {
        return tooltipSupplier(label -> tooltip);
    }
    
    public final Label tooltipSupplier(@Nullable Function<Label, @Nullable String> tooltip) {
        setTooltip(tooltip);
        return this;
    }
    
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
    
    public final Label color(int lightModeColor, int darkModeColor) {
        return color(REIHelper.getInstance().isDarkThemeEnabled() ? darkModeColor : lightModeColor);
    }
    
    public final Label color(int color) {
        setColor(color);
        return this;
    }
    
    public abstract int getHoveredColor();
    
    public abstract void setHoveredColor(int hoveredColor);
    
    public final Label hoveredColor(int lightModeColor, int darkModeColor) {
        return hoveredColor(REIHelper.getInstance().isDarkThemeEnabled() ? darkModeColor : lightModeColor);
    }
    
    public final Label hoveredColor(int color) {
        setHoveredColor(color);
        return this;
    }
    
    @NotNull
    public abstract Point getPoint();
    
    public final int getX() {
        return getPoint().getX();
    }
    
    public final int getY() {
        return getPoint().getY();
    }
    
    public abstract void setPoint(@NotNull Point point);
    
    public final Label point(@NotNull Point point) {
        setPoint(point);
        return this;
    }
    
    @NotNull
    public abstract String getText();
    
    public abstract void setText(@NotNull String text);
    
    public final Label text(@NotNull String text) {
        setText(text);
        return this;
    }
}
